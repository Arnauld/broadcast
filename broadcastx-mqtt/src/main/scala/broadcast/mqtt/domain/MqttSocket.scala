package broadcast.mqtt.domain

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
trait MqttSocket {
  def write(msg: Array[Byte])
}
