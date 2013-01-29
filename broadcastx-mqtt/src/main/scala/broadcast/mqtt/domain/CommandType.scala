package broadcast.mqtt.domain

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
object CommandType extends Enumeration {
  /**
   * Reserved
   */
  val R0 = Value(0)

  /**
   * Client request to connect to Server
   */
  val CONNECT = Value(1)

  /**
   * Connect Acknowledgment
   */
  val CONNACK = Value(2)

  /**
   * Publish message
   */
  val PUBLISH = Value(3)

  /**
   * Publish Acknowledgment
   */
  val PUBACK = Value(4)

  /**
   * Publish Received (assured delivery part 1)
   */
  val PUBREC = Value(5)

  /**
   * Publish Release (assured delivery part 2)
   */
  val PUBREL = Value(6)

  /**
   * Publish Complete (assured delivery part 3)
   */
  val PUBCOMP = Value(7)

  /**
   * Client Subscribe request
   */
  val SUBSCRIBE = Value(8)

  /**
   * Subscribe Acknowledgment
   */
  val SUBACK = Value(9)

  /**
   * Client Unsubscribe request
   */
  val UNSUBSCRIBE = Value(10)

  /**
   * Unsubscribe Acknowledgment
   */
  val UNSUBACK = Value(11)

  /**
   * PING Request
   */
  val PINGREQ = Value(12)

  /**
   * PING Response
   */
  val PINGRESP = Value(13)

  /**
   * Client is Disconnecting
   */
  val DISCONNECT = Value(14)

  /**
   * Reserved
   */
  val R15 = Value(15)
}
