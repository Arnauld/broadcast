package broadcast.mqtt.domain

import java.security.MessageDigest
import org.vertx.java.core.http.impl.ws.Base64
import org.vertx.java.core.json.JsonObject

/**
 * Uniquely defines a session. Whereas `clientId` could be used, it is not
 * suitable to handle reconnect case.
 *
 * <blockquote>
 * <p>
 * If a client with the same Client ID is already connected to the server,
 * the "older" client must be disconnected by the server before completing
 * the CONNECT flow of the new client.
 * </p>
 * <small>MQTT V3.1 Protocol Specification - section 3.1</small>
 * </blockquote>
 *
 * @param clientId Unique identifier of a client as defined by the MQTT
 *                 specification.
 * @param token Unique token used to be able to distinguish the same client
 *              in two different session.
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
case class SessionId(clientId: String, token: String) {
  def asJson(): JsonObject = new JsonObject().putString("clientId", clientId).putString("token", token)
}

object SessionId {
  def generate(clientId: String): SessionId = SessionId(clientId, generateToken())

  def generateToken(): String = {
    val nanos = System.nanoTime()
    val digest: MessageDigest = MessageDigest.getInstance("md5")
    update(digest, (nanos >> 56) & 0xFF)
    update(digest, (nanos >> 48) & 0xFF)
    update(digest, (nanos >> 40) & 0xFF)
    update(digest, (nanos >> 32) & 0xFF)
    update(digest, (nanos >> 24) & 0xFF)
    update(digest, (nanos >> 26) & 0xFF)
    update(digest, (nanos >> 8) & 0xFF)
    update(digest, (nanos >> 0) & 0xFF)
    // TODO replace by a more suitable Base64 encoder...
    Base64.encodeBytes(digest.digest())
  }

  private def update(digest: MessageDigest, byte: Long) {
    digest.update(byte.asInstanceOf[Byte])
  }
}
