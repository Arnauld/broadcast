package broadcast.mqtt.vertx.codec

import java.security.MessageDigest
import org.vertx.java.core.http.impl.ws.Base64
import org.vertx.java.core.json.JsonObject

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
case class SessionId(clientId: String, token: String) {
  def asJson(): JsonObject = new JsonObject().putString("clientId", clientId).putString("token", token)
}

object SessionId {
  def generate(clientId:String):SessionId = SessionId(clientId, generateToken())

  def generateToken(): String = {
    val nanos = System.nanoTime()
    val digest: MessageDigest = MessageDigest.getInstance("md5")
    update(digest,(nanos >> 56) & 0xFF)
    update(digest,(nanos >> 48) & 0xFF)
    update(digest,(nanos >> 40) & 0xFF)
    update(digest,(nanos >> 32) & 0xFF)
    update(digest,(nanos >> 24) & 0xFF)
    update(digest,(nanos >> 26) & 0xFF)
    update(digest,(nanos >> 8) & 0xFF)
    update(digest,(nanos >> 0) & 0xFF)
    // TODO replace by a more suitable Base64 encoder...
    Base64.encodeBytes(digest.digest())
  }

  private def update(digest:MessageDigest, byte:Long) {
    digest.update(byte.asInstanceOf[Byte])
  }
}
