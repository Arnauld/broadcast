package broadcast.mqtt.domain


/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
case class Publish(header: Header,
                   sessionId: SessionId,
                   topic: String,
                   messageId: Option[Int],
                   payload: Array[Byte]) extends MqttMessage

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
object Puback {
  val header = Header(messageType = CommandType.PUBACK,
    DUP = false,
    QoS = QosLevel(1),
    retain = false,
    remainingLength = 2)

}

/**
 *
 * @param messageId
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
case class Puback(messageId:Int) {
  def header() = Puback.header
}

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
object Pubrec {
  val header = Header(messageType = CommandType.PUBREC,
    DUP = false,
    QoS = QosLevel(2),
    retain = false,
    remainingLength = 2)

}

/**
 *
 * @param messageId
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
case class Pubrec(messageId:Int) {
  def header() = Pubrec.header
}