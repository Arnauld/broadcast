package broadcast.mqtt.vertx.codec

import broadcast.mqtt.domain.{SessionId, Publish, Header}
import org.slf4j.LoggerFactory
import broadcast.mqtt.vertx.util.ByteStream

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
class PublishDecoder(header: Header,
                     decoderRegistry:MessageDecoderRegistry,
                     sessionId:SessionId) extends Decoder {

  val log = LoggerFactory.getLogger(classOf[PublishDecoder])

  def decode(stream: ByteStream) = {
    if (stream.readableBytes() < header.remainingLength) {
      log.debug("Not enough data to decode `Publish` message ({}/{})", stream.readableBytes(), header.remainingLength)
      // wait until all data has been received to start decoding
      DecodeResult.Incomplete
    }
    else {
      val startPos = stream.readerIndex()
      // --- VARIABLE HEADER

      // The variable header contains the following fields:
      // Topic name
      //    A UTF-encoded string.
      // This must not contain Topic wildcard characters.
      // When received by a client that subscribed using wildcard characters,
      // this string will be the absolute topic specified by the originating
      // publisher and not the subscription string used by the client.
      val topic = stream.readUTF()

      //  Message ID
      //  Present for messages with QoS level 1 and QoS level 2.
      //
      // The Message Identifier (Message ID) field is only present in
      // messages where the QoS bits in the fixed header indicate QoS
      // levels 1 or 2.
      // The Message ID is a 16-bit unsigned integer that must be unique
      // amongst the set of "in flight" messages in a particular direction
      // of communication. It typically increases by exactly one from one
      // message to the next, but is not required to do so.
      // A client will maintain its own list of Message IDs separate to the
      // Message IDs used by the server it is connected to.
      // (MQTT V3.1 Protocol Specification - section 2.4)
      val messageId: Option[Int] =
        if (header.QoS.id == 1 || header.QoS.id == 2)
          Some(stream.readUnsignedShort()) // 16-bit unsigned integer
        else
          None

      // --- PAYLOAD

      val payloadPos = stream.readerIndex()
      val payloadLen = header.remainingLength - (payloadPos - startPos)

      // Contains the data for publishing. The content and format of the data is
      // application specific. The Remaining Length field in the fixed header
      // includes both the variable header length and the payload length. As such,
      // it is valid for a PUBLISH to contain a 0-length payload.

      // make sure there is enough bytes remaining according to the header
      assert(stream.readableBytes() >= payloadLen )

      val payload = new Array[Byte](payloadLen.asInstanceOf[Int])
      stream.readBytes(payload)

      val publish = Publish(header, sessionId, topic, messageId, payload)

      log.debug("Publish decoded {}", publish)

      DecodeResult.Finished(publish, decoderRegistry.headerDecoder())
    }
  }

  override def toString = "PublishDecoder(" + sessionId + ", " + header + ")"

}
