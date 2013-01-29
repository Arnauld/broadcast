package broadcast.mqtt.vertx.codec

import org.slf4j.Logger
import org.vertx.java.core.net.NetSocket

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */

trait BaseHandler {
  def log: Logger

  def sock: NetSocket

  def encoders: Encoders
}

