package broadcast.mqtt.vertx.codec

import broadcast.mqtt.domain.SessionId

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
trait MqttHandlerListener {
  def sessionIdAffected(sessionId: SessionId, handler:MqttHandler) {}
  def sessionDisposed(sessionId: SessionId, handler:MqttHandler) {}
}
