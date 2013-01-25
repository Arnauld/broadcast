package broadcast.mqtt.vertx.codec

import org.vertx.java.core.net.NetSocket
import broadcast.mqtt.domain.Connack

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
object Encoders {
  def apply():Encoders = new Encoders {}
}

trait Encoders {

  def writeConnack(sock:NetSocket, connack:Connack) {

  }

  def writeConnack(sock:NetSocket, connackCode:Connack.Code.Value) {

  }
}
