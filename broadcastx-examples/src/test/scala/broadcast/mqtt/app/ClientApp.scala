package broadcast.mqtt.app

import java.util.Date
import org.fusesource.mqtt.client._
import java.util.concurrent.{TimeUnit, CountDownLatch}

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
object ClientApp extends App {
  val latch = new CountDownLatch(1)
  val mqtt = new MQTT
  mqtt.setConnectAttemptsMax(3)
  mqtt.setReconnectAttemptsMax(3)
  mqtt.setReconnectDelay(3000)
  mqtt.setHost("127.0.0.1", 7654)
  val cnxCb = new CbConnection(mqtt)

  println(new Date() + " Connecting...")
  cnxCb.connect(new Callback[Void] {
    def onSuccess(value: Void) {
      println("Connected!")
      println("Publishing...")
      cnxCb.publish("foo", "Hello".getBytes("utf-8"), QoS.AT_LEAST_ONCE, false, new Callback[Void] {
        def onFailure(thr: Throwable) {
          latch.countDown()
          println("Publish failure")
          thr.printStackTrace()
        }

        def onSuccess(value: Void) {
          latch.countDown()
          println("Published!")
        }
      })
    }

    def onFailure(thr: Throwable) {
      latch.countDown()
      println("Connection failure")
      thr.printStackTrace()
    }
  })

  latch.await(14, TimeUnit.SECONDS)
}
