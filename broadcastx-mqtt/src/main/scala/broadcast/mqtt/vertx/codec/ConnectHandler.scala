package broadcast.mqtt.vertx.codec

import broadcast.mqtt.domain.{SessionId, Connack, Connect}
import broadcast.service.AuthService
import org.slf4j.LoggerFactory

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
trait ConnectHandler {
  this: Encoder =>

  private val log = LoggerFactory.getLogger(classOf[ConnectDecoder])

  def authService: AuthService

  var sessionId: Option[SessionId] = None

  /**
   * Intended for subclass to be notified when a sessionId has been
   * affected. Default implementation does nothing.
   */
  protected def sessionIdAffected() {}

  /**
   * @see [[broadcast.mqtt.domain.Connect]]
   */
  def handleConnect(msg: Connect) {
    // If the Client ID contains more than 23 characters, the server responds
    // to the CONNECT message with a CONNACK return code 2: Identifier Rejected.
    // MQTT V3.1 Protocol Specification - section 3.1
    val clientId = msg.clientId
    val len = if (clientId == null) 0 else clientId.length
    if (len == 0 || len > 23) {
      log.info("Identifier rejected caused by an invalid size: \'{}\'", clientId)
      writeConnack(Connack.Code.IdentifierRejected)
    }
    // Check username & password
    else if (!authService.isAuthorized(msg.username, msg.password)) {
      log.info("User not authorized: \'{}\'", msg.username)
      writeConnack(Connack.Code.BadUserOrPassword)
    }
    else if (sessionId.isDefined) {
      log.warn("User already identified: \'{}\'", sessionId.get)
      // TODO find a suitable connack code...
      writeConnack(Connack.Code.BadUserOrPassword)
    }
    // username authorized, let's continue
    else {
      val sessId = SessionId.generate(clientId)
      log.info("User connected: clientId:\'{}\' sessionId:\'{}\'", clientId, sessId)
      sessionId = Some(sessId)
      sessionIdAffected()

      writeConnack(Connack.Code.Accepted)
    }
  }
}
