package broadcast.mqtt.vertx.codec

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
trait MqttHandlerGateway {
  def register(sessionId:SessionId, handler:MqttHandler)
  def unregister(sessionId:SessionId)
}
