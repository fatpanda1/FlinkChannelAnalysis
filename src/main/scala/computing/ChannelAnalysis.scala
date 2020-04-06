package computing

import java.text.SimpleDateFormat
import java.util.Properties

import com.google.gson.{Gson, JsonParser}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}
import org.apache.flink.table.api._
import org.apache.flink.util.StringUtils
import redis.clients.jedis.Jedis
import watermarks.{ChannelWaterMark, FirstLoginWatermark, GetCardWatermark}


object ChannelAnalysis {
  //获取redis服务器信息
  private val host: String = "120.55.43.230"
  private val port: Int = 6379

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //设置事件时间
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    //配置kafka信息
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "120.55.43.230:9092")
    properties.setProperty("group.id", "consumer-group")
    properties.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    properties.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    properties.setProperty("auto.offset.reset", "latest")

    //获取流
    val channelStream = env.addSource(new FlinkKafkaConsumer011[String](
      "CA_CHANNEL",
      new SimpleStringSchema(),
      properties
    ))
    val firstLoginStream = env.addSource(new FlinkKafkaConsumer011[String](
      "CA_LOGIN",
      new SimpleStringSchema(),
      properties
    ))
    val getCardStream = env.addSource(new FlinkKafkaConsumer011[String](
      "CA_CARD",
      new SimpleStringSchema(),
      properties
    ))

    /**
      * 处理渠道流
      * 数据源(JSON)字段 tel,channel,time
      * 需要将time转为时间戳
      */
    val channelMap: DataStream[Channel] = channelStream.map(line => {
      val df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss")
      var timeStamp = 1l
      val data = new JsonParser().parse(line).getAsJsonObject

      //从数据源获取字段
      val tel = data.get("tel").getAsString.trim
      val channel = data.get("channel").getAsString.trim
      val time = data.get("time").getAsString.trim

      //将时间转换为时间戳
      if (!StringUtils.isNullOrWhitespaceOnly(time)){
        timeStamp = df.parse(time).getTime
      }
      Channel(tel, channel, timeStamp)
    })
      //为channelMap设置watermark
      .assignTimestampsAndWatermarks(new ChannelWaterMark)

    /**
      * 处理首次点击流
      * 数据源(JSON)字段 uid,time
      * 需要从背景表中取出tel  uid->tel
      * 需要将time转为时间戳
      */
    val firstLoginMap = firstLoginStream.map(line => {
      val df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss")
      var timeStamp = 1l
      val jedis = new Jedis(host,port)

      //从数据源获取字段信息
      val data = new JsonParser().parse(line).getAsJsonObject
      val uid = data.get("uid").getAsString.trim
      val time = data.get("time").getAsString.trim

      //通过背景表取出tel字段
      var tel = jedis.get(uid)

      //将时间转为时间戳
      if (!StringUtils.isNullOrWhitespaceOnly(time)){
        timeStamp = df.parse(time).getTime
      }

      FirstLogin(uid, tel, timeStamp)
    })
      .assignTimestampsAndWatermarks(new FirstLoginWatermark)

    /**
      * 处理绑卡流
      * 数据源(JSON)字段 uid,cardNum,time
      * 需要从背景表中取出tel  uid->tel
      * 需要将time转为时间戳
      */
    val getCardMap: DataStream[GetCard] = getCardStream.map(line => {
      val df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss")
      var timeStamp = 1l
      val jedis = new Jedis(host,port)

      val data = new JsonParser().parse(line).getAsJsonObject
      val uid = data.get("uid").getAsString.trim
      val time = data.get("time").getAsString.trim
      val cardNum = data.get("cardNum").getAsString.trim

      //通过背景表取出tel字段
      var tel = jedis.get(uid)

      if (!StringUtils.isNullOrWhitespaceOnly(time)){
        timeStamp = df.parse(time).getTime
      }
      GetCard(tel,cardNum,timeStamp)
    })
      .assignTimestampsAndWatermarks(new GetCardWatermark)



    //获取TableApi执行环境
    val tableEnv = TableEnvironment.getTableEnvironment(env)

    import org.apache.flink.table.api.scala._
    //建表
    val channelTable = tableEnv.fromDataStream(channelMap,'ctel,'channel,'ctime.rowtime)
    val firstLoginTable = tableEnv.fromDataStream(firstLoginMap,'uid,'ftel,'ftime.rowtime)
    val getCardTable = tableEnv.fromDataStream(getCardMap,'gtel,'cardNum,'gtime.rowtime)

    //连接三条流处理
    val resultTable = firstLoginTable.join(channelTable,"ctel=ftel")
      .where('ftime > 'ctime && 'ftime <= 'ctime + 2.hour)
      .join(getCardTable,"ctel=gtel")
      .where('gtime > 'ftime && 'gtime <= 'ftime + 2.hour)
      .select('uid,'gtel,'channel,'cardNum)

    import org.apache.flink.api.scala._

    tableEnv.toRetractStream[(String,String,String,String)](resultTable)
        .map( data => {
          val field = data._2
          val output = OutputJson(field._1,field._2,field._3,field._4)
          val gson = new Gson
          gson.toJson(output)
        } )
        .addSink(new FlinkKafkaProducer011[String](
          "120.55.43.230:9092",
          "CA_OUTPUT",
          new SimpleStringSchema()
        ))

    env.execute("watermarkTest")
  }
}

case class Channel(ctel: String, channel: String, ctime: Long)
case class FirstLogin(uid: String,ftel: String, ftime: Long)
case class GetCard(gtel: String, cardNum: String, gtime: Long)
case class OutputJson(uid:String, tel:String, channel: String, cardNum: String)
