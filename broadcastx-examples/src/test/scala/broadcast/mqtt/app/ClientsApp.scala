package broadcast.mqtt.app

import java.util.Date
import org.fusesource.mqtt.client._
import java.util.concurrent.{TimeUnit, CountDownLatch}
import org.fusesource.hawtbuf.{Buffer, UTF8Buffer}

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
object ClientsApp extends App {

  val latch = new CountDownLatch(2)

  connectAnd({
    res =>
      if (res.isLeft) {
        val cnxCb = res.left.get

        cnxCb.listener(new Listener {
          def onPublish(topic: UTF8Buffer, body: Buffer, ack: Runnable) {
            println("Message received topic: " + topic + ", body: " + body.utf8().toString)
            latch.countDown()
          }

          def onConnected() {}

          def onFailure(value: Throwable) {

            latch.countDown()
          }

          def onDisconnected() {}
        })

        println("Subscribing...")
        cnxCb.subscribe(Array(new Topic("foo", QoS.AT_LEAST_ONCE)), new Callback[Array[Byte]] {
          def onFailure(thr: Throwable) {
            latch.countDown()
            println("Subscribe failure")
            thr.printStackTrace()
          }

          def onSuccess(value: Array[Byte]) {
            println("Subscribed done... waiting for message " + new String(value))
          }
        })
      }
      else {
        res.right.get.printStackTrace()
        latch.countDown()
      }
  })

  connectAnd({
    res =>
      if (res.isLeft) {

        println("Publishing...")
        res.left.get.publish("foo", "Hello".getBytes("utf-8"), QoS.AT_LEAST_ONCE, false, new Callback[Void] {
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
      else {
        res.right.get.printStackTrace()
        latch.countDown()
      }
  })

  latch.await(14, TimeUnit.SECONDS)

  def connectAnd(next: (Either[CbConnection, Throwable]) => Unit) {

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
        next(Left[CbConnection, Throwable](cnxCb))
      }

      def onFailure(thr: Throwable) {
        println("Connection failure")
        next(Right[CbConnection, Throwable](thr))
      }
    })

  }
}
