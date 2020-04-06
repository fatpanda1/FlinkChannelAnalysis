package test

import java.text.SimpleDateFormat

object timeAndTimestamp {

  private val df = new SimpleDateFormat("yyy-MM-dd HH:mm:ss")


//  def main(args: Array[String]): Unit = {
////    println(dateToStamp("2020-03-24 15:20:00"))
//    print(stampTodata(1585034400000l))
//  }

  /**
    * 将时间转换为时间戳
    */
  def dateToStamp(str: String) = {
    val date = df.parse(str).getTime
    date
  }

  /**
    * 将时间戳转换为时间
    */
  def stampTodata(stamp: Long) = {
    val date = df.format(stamp)
    date
  }
}
//1585034160000