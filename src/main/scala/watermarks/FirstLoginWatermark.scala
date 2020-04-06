package watermarks

import computing.FirstLogin
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.watermark.Watermark

class FirstLoginWatermark extends AssignerWithPeriodicWatermarks[FirstLogin] {
  //设置watermark的缓冲为一小时,60*60*1000
  private val bound:Long = 3600000
  //观察到的最大时间戳
  private var maxTs:Long = _

  override def getCurrentWatermark: Watermark = {
    //生成watermark，用当前最大时间戳-缓冲时间
    new Watermark(maxTs - bound)
  }

  override def extractTimestamp(t: FirstLogin, l: Long): Long = {
    maxTs = maxTs.max(t.ftime)
    t.ftime
  }
}
