package broadcast.mqtt.app

import java.util.Date
import org.fusesource.mqtt.client._
import java.util.concurrent.{TimeUnit, CountDownLatch}
import org.fusesource.hawtbuf.{Buffer, UTF8Buffer}

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
object ClientSubscribeApp extends App {
  val latch = new CountDownLatch(1)
  val mqtt = new MQTT
  mqtt.setConnectAttemptsMax(3)
  mqtt.setReconnectAttemptsMax(3)
  mqtt.setReconnectDelay(3000)
  mqtt.setHost("127.0.0.1", 7654)
  val cnxCb = new CbConnection(mqtt)

  cnxCb.listener(new Listener {
    def onPublish(topic: UTF8Buffer, body: Buffer, ack: Runnable) {
      println("Message received...(" + topic.toString + "): " + body.utf8().toString)
    }

    def onConnected() {}

    def onFailure(value: Throwable) {}

    def onDisconnected() {}
  })

  println(new Date() + " Connecting...")
  cnxCb.connect(new Callback[Void] {
    def onSuccess(value: Void) {
      println("Connected!")
      println("Publishing...")
      cnxCb.subscribe(Array(new Topic("foo", QoS.AT_MOST_ONCE)), new Callback[Array[Byte]] {
        def onFailure(thr: Throwable) {
          latch.countDown()
          println("Publish failure")
          thr.printStackTrace()
        }

        def onSuccess(value: Array[Byte]) {
          latch.countDown()
          println("Subscribed! " + new String(value))
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
