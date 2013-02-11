package broadcast.mqtt.service

import broadcast.mqtt.domain.{MqttSocket, SessionId, Publish}
import java.util.regex.Pattern
import org.slf4j.LoggerFactory

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
trait MessagingService {

  private val log = LoggerFactory.getLogger(classOf[MessagingService])

  var sessions:Map[SessionId,Session] = Map.empty

  def closeSession(sessionId: SessionId) {
    sessions = sessions - sessionId
  }

  def openSession(sessionId: SessionId, socket:MqttSocket) {
    sessions = sessions + (sessionId -> new Session(socket))
  }

  def publish(msg: Array[Byte], oncePublished: => Unit)

  def broadcast(msg: Array[Byte], oncePublished: => Unit) {
    val topic = Publish.topic(msg)
    sessions.foreach({ e =>
      val subscriptions = e._2.subscriptions
      subscriptions.foreach({ p =>
        if (p.matcher(topic).matches()) {
          log.debug("Writing message to {} (topic {})", e._1, topic)
          e._2.socket.write(msg)
        }
        else {
          log.debug("{} not interested by message (topic {})", e._1, topic)
        }
      })
    })
  }
}

class Session(val socket:MqttSocket) {
  var subscriptions:List[Pattern] = Nil
}
