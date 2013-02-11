package broadcast.mqtt.vertx.codec

import org.vertx.java.core.net.NetSocket
import broadcast.mqtt.domain._
import org.slf4j.LoggerFactory
import broadcast.service.AuthService
import broadcast.mqtt.service.{MessagingService}
import org.vertx.java.core.buffer.Buffer
import broadcast.mqtt.domain.Header
import broadcast.mqtt.domain.Connect


/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
class MqttHandler(val sock: NetSocket,
                  val authService: AuthService,
                  val messagingService: MessagingService)
  extends Encoder
  with ConnectHandler
  with PublishHandler {

  private val log = LoggerFactory.getLogger(classOf[MqttHandler])

  var listeners = List[MqttHandlerListener]()

  def getSessionId = sessionId

  def addListener(listener: MqttHandlerListener) {
    listeners = listener :: listeners
  }

  def removeListener(listener: MqttHandlerListener) {
    listeners = listeners.filterNot(_ == listener)
  }

  def noDecoderFoundForType(header: Header) {
    log.error("Unsupported message type (no decoder found), got: {}", header)
    disconnect(DisconnectReason.UnsupportedMessageType)
  }

  /**
   * Main entry point that dispatch to related method.
   *
   * @param msg message to dispatch
   * @see [[broadcast.mqtt.vertx.codec.MqttHandler.handleConnect( )]]
   */
  def handle(msg: MqttMessage) {
    msg match {
      case connect: Connect =>
        handleConnect(msg.asInstanceOf[Connect])
      case publish: Publish =>
        handlePublish(msg.asInstanceOf[Publish])
      case _ =>
        log.error("Message decoded but not handled, got: {}", msg)
        disconnect(DisconnectReason.UnsupportedMessageType)
    }
  }

  /**
   * The connection should be closed for a good reason...
   * @see [[broadcast.mqtt.domain.DisconnectReason]]
   */
  def disconnect(reason: DisconnectReason) {
    import DisconnectReason._
    reason match {
      case Reconnect =>
        log.warn("Disconnecting client because its reconnection has been " +
          "reported, session: {}", sessionId)
      case SocketError(e) =>
        log.warn("Disconnecting client because socket error has been reported, " +
          "session: {}", sessionId, e)
      case UnsupportedMessageType =>
        log.warn("Disconnecting client because an unsupported message type has " +
          "been reported, session: {}", sessionId)
    }
    disconnect()
  }

  private def disconnect() {
    sessionId.foreach({
      sid =>
        listeners.foreach(_.sessionDisposed(sid, this))
    })
    sock.close()
  }

  /**
   * @see [[broadcast.mqtt.vertx.codec.ConnectHandler.sessionIdAffected( )]]
   */
  override protected def sessionIdAffected() {
    sessionId.foreach({
      sid =>
        listeners.foreach(_.sessionIdAffected(sid, this))
    })
  }

  /**
   * @see [[broadcast.mqtt.vertx.codec.PublishHandler.publish( )]]
   */
  override protected def publish(msg: Publish, oncePublished: => Unit) {
    messagingService.publish(msg.rawWithHeader(), oncePublished)
  }

  /**
   * @see [[MqttSocket]]
   */
  def asMqttSocket(): MqttSocket =
    new MqttSocket {
      def write(msg: Array[Byte]) {
        sock.write(new Buffer(msg))
      }
    }
}
