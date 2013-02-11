package broadcast.mqtt.vertx.codec

import broadcast.mqtt.domain.Publish
import org.slf4j.LoggerFactory

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
trait PublishHandler {
  this: Encoder =>

  private val log = LoggerFactory.getLogger(classOf[PublishHandler])

  /**
   * Intended for subclass to be notified that a message must be published.
   */
  protected def publish(msg: Publish, oncePublished: => Unit)

  /**
   * @see [[broadcast.mqtt.domain.Publish]]
   */
  def handlePublish(msg: Publish) {

    msg.header.QoS.id match {
      case 0 /*QosLevel.AtMostOnce*/ => // 0
        // Make the message available to any interested parties.
        // MQTT V3.1 Protocol Specification - sections 3.3

        log.debug("Publishing message (QoS 0) ({} / {})", msg.messageId, msg.sessionId)
        publish(msg, {})

      case 1 /*QosLevel.AtLeastOnce*/ => // 1
        // A PUBACK message is the response to a PUBLISH message with
        // QoS level 1. A PUBACK message is sent by a server in response
        // to a PUBLISH message from a publishing client, and by a
        // subscriber in response to a PUBLISH message from the server.
        // MQTT V3.1 Protocol Specification - sections 3.4

        // Log the message to persistent storage,
        // make it available to any interested parties,
        // and return a PUBACK message to the sender.
        // MQTT V3.1 Protocol Specification - sections 3.3

        // TODO add persistance

        log.debug("Publishing message (QoS 1) ({} / {})", msg.messageId, msg.sessionId)

        publish(msg, {
          // The Message Identifier (Message ID) field is only present in
          // messages where the QoS bits in the fixed header indicate QoS
          // levels 1 or 2.
          writePuback(msg.messageId.get)
        })


      case 2 /*QosLevel.ExactlyOnce*/ => // 2
        // A PUBREC message is the response to a PUBLISH message with
        // QoS level 2. It is the second message of the QoS level 2
        // protocol flow. A PUBREC message is sent by the server in
        // response to a PUBLISH message from a publishing client, or
        // by a subscriber in response to a PUBLISH message from the
        // server.
        // MQTT V3.1 Protocol Specification - sections 3.5

        // Log the message to persistent storage,
        // do not make it available to interested parties yet,
        // and return a PUBREC message to the sender.
        // MQTT V3.1 Protocol Specification - sections 3.3

        // TODO add persistance

        // The Message Identifier (Message ID) field is only present in
        // messages where the QoS bits in the fixed header indicate QoS
        // levels 1 or 2.

        /*
        writePubrec(msg.messageId.get)

        log.debug("Publishing message (QoS 2) ({} / {})", msg.messageId, msg.sessionId)

        publish(msg, {})
        */
        throw new UnsupportedOperationException
    }
  }

}
