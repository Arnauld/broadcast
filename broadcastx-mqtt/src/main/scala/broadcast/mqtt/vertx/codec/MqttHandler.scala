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

  def unsupportedMessageType(header: Header) {
    // log'n close
    sock.close()
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
      log.info("Identifier rejected caused by invalid size: \'{}\'", clientId)
      encoders.writeConnack(sock, Connack.Code.IdentifierRejected)
    }
    // Check username & password
    else if (!authService.isAuthorized(msg.username, msg.password)) {
      log.info("User not authorized: \'{}\'", msg.username)
      encoders.writeConnack(sock, Connack.Code.BadUserOrPassword)
    }
    else if (sessionId.isDefined) {
      log.info("User already identified: \'{}\'", sessionId.get)
      encoders.writeConnack(sock, Connack.Code.IdentifierRejected)
    }
    // username authorized, let's continue
    else {
      val sessId = SessionId.generate(clientId)
      sessionId = Some(sessId)
      gateway.register(sessId, this)
      encoders.writeConnack(sock, Connack.Code.Accepted)
    }
  }

  /**
   * The connection should be closed since a client with the same clientId
   * is reconnecting.
   */
  def disconnectCausedByConnect() {
    disconnect()
  }

  /**
   * The connection should be closed since the socket thrown an error: fail fast.
   */
  def disconnectCausedBySocketError() {
    disconnect()
  }

  private def disconnect() {
    sock.close()
    sessionId.foreach(gateway.unregister(_))
  }
}
