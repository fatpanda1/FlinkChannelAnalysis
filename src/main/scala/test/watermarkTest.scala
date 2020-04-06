package test

import java.text.SimpleDateFormat

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.util.StringUtils
import watermarks.{ChannelWaterMark, FirstLoginWatermark, GetCardWatermark}


//object watermarkTest {
//
//  def main(args: Array[String]): Unit = {
//    val env = StreamExecutionEnvironment.getExecutionEnvironment
//    //设置事件时间
//    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
//
//    val channelStream = env.readTextFile("F:\\learning\\FlinkLearning\\FlinkChannelAnalysis\\src\\main\\resources\\channel.txt")
//    val firstLoginStream = env.readTextFile("F:\\learning\\FlinkLearning\\FlinkChannelAnalysis\\src\\main\\resources\\firstLogin.txt")
//    val getCardStream = env.readTextFile("F:\\learning\\FlinkLearning\\FlinkChannelAnalysis\\src\\main\\resources\\getCard")
//
//    //处理渠道流
//    val channelMap: DataStream[ChannelT] = channelStream.map(line => {
//      val df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss")
//      var time = 1l
//      val data = line.split(",")
//      if (!StringUtils.isNullOrWhitespaceOnly(data(2))){
//        time = df.parse(data(2)).getTime
//      }
//      ChannelT(data(0).trim, data(1).trim, time)
//    })
//      .assignTimestampsAndWatermarks(new ChannelWaterMark)
//
//    //处理首次点击流
//    val firstLoginMap = firstLoginStream.map(line => {
//      val df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss")
//      var time = 1l
//      val data = line.split(",")
//      if (!StringUtils.isNullOrWhitespaceOnly(data(1))){
//        time = df.parse(data(1)).getTime
//      }
//      FirstLoginT(data(0).trim, time)
//    })
//      .assignTimestampsAndWatermarks(new FirstLoginWatermark)
//
//
//    //处理绑卡流
//    val getCardMap: DataStream[GetCardT] = getCardStream.map(line => {
//      val df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss")
//      var time = 1l
//      val data = line.split(",")
//      if (!StringUtils.isNullOrWhitespaceOnly(data(2))){
//        time = df.parse(data(2)).getTime
//      }
//      GetCardT(data(0),data(1),time)
//    })
//      .assignTimestampsAndWatermarks(new GetCardWatermark)
//
//    //获取TableApi执行环境
//    val tableEnv = TableEnvironment.getTableEnvironment(env)
//
//    import org.apache.flink.table.api.scala._
//    //建表
//    val channelTable = tableEnv.fromDataStream(channelMap,'cuid,'channel,'ctime.rowtime)
//    val firstLoginTable = tableEnv.fromDataStream(firstLoginMap,'fuid,'ftime.rowtime)
//    val getCardTable = tableEnv.fromDataStream(getCardMap,'carduid,'cardNum,'cardtime.rowtime)
//
//    //连接三条流处理
//    val resultTable = firstLoginTable.join(channelTable,"cuid=fuid")
//      .where('ftime > 'ctime && 'ftime <= 'ctime + 2.hour)
//      .join(getCardTable,"carduid=cuid")
//      .where('cardtime > 'ftime && 'cardtime <= 'ftime + 2.hour)
//      .select('cuid,'channel,'cardNum)
//
//    import org.apache.flink.api.scala._
//
//    tableEnv.toRetractStream[(String,String,String)](resultTable).print()
//
//    env.execute("watermarkTest")
//  }
//}


case class ChannelT(cuid: String, channel: String, ctime: Long)
case class FirstLoginT(fuid: String, ftime: Long)
case class GetCardT(carduid: String, cardNum: String, cardtime: Long)