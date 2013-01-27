package broadcast.mqtt.vertx.codec

import org.vertx.java.core.net.NetSocket
import broadcast.mqtt.domain.{Connack, Connect, Header, MqttMessage}
import org.slf4j.LoggerFactory
import broadcast.service.AuthService

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
class MqttHandler(gateway: MqttHandlerGateway,
                  authService: AuthService,
                  sock: NetSocket,
                  encoders: Encoders = Encoders()) {


  private var sessionId: Option[SessionId] = None

  private val log = LoggerFactory.getLogger(classOf[MqttHandler])

  def getSessionId = sessionId

  def noDecoderFoundForType(header: Header) {
    log.error("Unsupported message type (no decoder found), got: {}", header)
    disconnectCausedByUnsupportedMessageType()
  }

  /**
   * Main entry point that dispatch to related method.
   *
   * @param msg message to dispatch
   * @see #handleConnect(Connect)
   */
  def handle(msg: MqttMessage) {
    msg match {
      case connect: Connect =>
        handleConnect(msg.asInstanceOf[Connect])
      case _ =>
        log.error("Message decoded but not handlede, got: {}", msg)
        disconnectCausedByUnsupportedMessageType()

    }
  }

  /**
   * @see Connect
   */
  def handleConnect(msg: Connect) {
    // If the Client ID contains more than 23 characters, the server responds
    // to the CONNECT message with a CONNACK return code 2: Identifier Rejected.
    // MQTT V3.1 Protocol Specification - section 3.1
    val clientId = msg.clientId
    val len = if (clientId == null) 0 else clientId.length
    if (len == 0 || len > 23) {
      log.info("Identifier rejected caused by an invalid size: \'{}\'", clientId)
      encoders.writeConnack(sock, Connack.Code.IdentifierRejected)
    }
    // Check username & password
    else if (!authService.isAuthorized(msg.username, msg.password)) {
      log.info("User not authorized: \'{}\'", msg.username)
      encoders.writeConnack(sock, Connack.Code.BadUserOrPassword)
    }
    else if (sessionId.isDefined) {
      log.warn("User already identified: \'{}\'", sessionId.get)
      // TODO find a suitable connack code...
      encoders.writeConnack(sock, Connack.Code.BadUserOrPassword)
    }
    // username authorized, let's continue
    else {
      val sessId = SessionId.generate(clientId)
      log.info("User connected: clientId:\'{}\' sessionId:\'{}\'", clientId, sessId)
      sessionId = Some(sessId)
      gateway.register(sessId, this)
      encoders.writeConnack(sock, Connack.Code.Accepted)
    }
  }

  //TODO: enum DisconnectReason
  /**
   * The connection should be closed since a client with the same clientId
   * is reconnecting.
   */
  def disconnectCausedByConnect() {
    log.warn("Disconnecting client because its reconnection has been reported, session: {}", sessionId)
    disconnect()
  }

  /**
   * The connection should be closed since the socket thrown an error: fail fast.
   */
  def disconnectCausedBySocketError() {
    log.warn("Disconnecting client because socket error has been reported, session: {}", sessionId)
    disconnect()
  }

/**
   * The connection should be closed since an unsupported message type has been received.
   */
  def disconnectCausedByUnsupportedMessageType() {
    log.warn("Disconnecting client because an unsupported message type has been reported, session: {}", sessionId)
    disconnect()
  }

  private def disconnect() {
    sock.close()
    sessionId.foreach(gateway.unregister(_))
  }
}
