package broadcast.mqtt.vertx.codec

import org.slf4j.LoggerFactory
import broadcast.mqtt.domain.{QosLevel, Subscribe}

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
trait SubscribeHandler {
  this: Encoder =>

  private val log = LoggerFactory.getLogger(classOf[SubscribeHandler])

  /**
   * Intended for subclass to be notified that subscriptions must be taken
   * into account.
   */
  protected def subscribe(msg: Subscribe)

  /**
   * @see [[broadcast.mqtt.domain.Connect]]
   */
  def handleSubscribe(msg: Subscribe) {

    subscribe(msg)
    // Note that if a server implementation does not authorize a
    // SUBSCRIBE request to be made by a client, it has no way of
    // informing that client. It must therefore make a positive
    // acknowledgement with a SUBACK, and the client will not be
    // informed that it was not authorized to subscribe.
    // MQTT V3.1 Protocol Specification - sections 3.8

    // A SUBACK message is sent by the server to the client to confirm
    // receipt of a SUBSCRIBE message.
    // A SUBACK message contains a list of granted QoS levels. The order
    // of granted QoS levels in the SUBACK message matches the order of
    // the topic names in the corresponding SUBSCRIBE message.
    // MQTT V3.1 Protocol Specification - sections 3.9

    writeSuback(msg.messageId, msg.topics.map({
      topic =>
      // A server may chose to grant a lower level of QoS than the
      // client requested. This could happen if the server is not
      // able to provide the higher levels of QoS. For example,
      // if the server does not provider a reliable persistence
      // mechanism it may chose to only grant subscriptions at QoS 0.
      // MQTT V3.1 Protocol Specification - sections 3.8
        QosLevel(0)
    }))
  }
}
