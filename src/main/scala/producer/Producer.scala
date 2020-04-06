package producer

import java.util.Properties

import com.google.gson.Gson
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.io.Source

object Producer {

  val brokers = "private001:9092"

  val props=new Properties()
  props.setProperty("bootstrap.servers", "120.55.43.230:9092")
  props.setProperty("group.id", "consumer-group")
  props.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.setProperty("auto.offset.reset", "latest")

  def main(args: Array[String]): Unit = {
    channelProducer
    loginProducer
    cardProducer
  }

  def channelProducer= {
    val file = Source.fromFile("F:\\learning\\FlinkLearning\\FlinkChannelAnalysis\\src\\main\\resources\\channel.txt")
    val topic = "CA_CHANNEL"

    val producer = new KafkaProducer[String, String](props)
    val gson = new Gson()

    //数据源uid008,QD008,2020-03-31 15:12:00
    for (line <- file.getLines()){
      val lines = line.split(",")
      val data = FileToTopicChannel(lines(0),lines(1),lines(2))
      val output = gson.toJson(data).toString

      val rcd = new ProducerRecord[String, String](topic, output)
      producer.send(rcd)
    }
    // 这里必须要调结束，否则kafka那边收不到消息
    producer.close()
  }

  def loginProducer= {
    val file = Source.fromFile("F:\\learning\\FlinkLearning\\FlinkChannelAnalysis\\src\\main\\resources\\firstLogin.txt")
    val topic = "CA_LOGIN"

    val producer = new KafkaProducer[String, String](props)
    val gson = new Gson()

    //数据源uid008,2020-03-31 15:03:00
    for (line <- file.getLines()){
      val lines = line.split(",")
      val data = FileToTopicLogin(lines(0),lines(1))
      val output = gson.toJson(data).toString
      println(output)

      val rcd = new ProducerRecord[String, String](topic, output)
      producer.send(rcd)
    }
    // 这里必须要调结束，否则kafka那边收不到消息
    producer.close()
  }

  def cardProducer= {
    val file = Source.fromFile("F:\\learning\\FlinkLearning\\FlinkChannelAnalysis\\src\\main\\resources\\getCard")
    val topic = "CA_CARD"

    val producer = new KafkaProducer[String, String](props)
    val gson = new Gson()

    //uid008,card008,2020-03-31 15:05:00
    for (line <- file.getLines()){
      val lines = line.split(",")
      val data = FileToTopicGetCard(lines(0),lines(1),lines(2))
      val output = gson.toJson(data).toString
      println(output)

      val rcd = new ProducerRecord[String, String](topic, output)
      producer.send(rcd)
    }
    // 这里必须要调结束，否则kafka那边收不到消息
    producer.close()
  }
}

case class FileToTopicChannel(tel: String, channel: String, time: String)
case class FileToTopicLogin(uid: String,time:String)
case class FileToTopicGetCard(uid: String, cardNum: String, time: String)