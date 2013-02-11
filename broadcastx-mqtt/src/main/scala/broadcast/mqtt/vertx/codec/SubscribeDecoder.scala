package broadcast.mqtt.vertx.codec

import broadcast.mqtt.domain._
import org.slf4j.LoggerFactory
import broadcast.mqtt.vertx.util.ByteStream
import broadcast.mqtt.domain.Header
import annotation.tailrec

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
class SubscribeDecoder(header: Header,
                       registry:MessageDecoderRegistry,
                       sessionId: SessionId) extends Decoder {

  val log = LoggerFactory.getLogger(classOf[SubscribeDecoder])

  def decode(stream: ByteStream) = {
    if (stream.readableBytes() < header.remainingLength) {
      log.debug("Not enough data to decode `Subscribe` message ({}/{})", stream.readableBytes(), header.remainingLength)
      // wait until all data has been received to start decoding
      DecodeResult.Incomplete
    }
    else {
      val startPos = stream.readerIndex()
      // --- VARIABLE HEADER

      //  Message ID
      //  The variable header contains a Message ID because a
      //  SUBSCRIBE message has a QoS level of 1.
      // (MQTT V3.1 Protocol Specification - section 3.8)
      val messageId = stream.readUnsignedShort() // 16-bit unsigned integer

      // --- PAYLOAD
      // The payload of a SUBSCRIBE message contains a list of
      // topic names to which the client wants to subscribe, and
      // the QoS level at which the client wants to receive the
      // messages. The strings are UTF-encoded, and the QoS level
      // occupies 2 bits of a single byte. The topic strings may
      // contain special Topic wildcard characters to represent a set
      // of topics. These topic/QoS pairs are packed contiguously (...)
      // (MQTT V3.1 Protocol Specification - section 3.8)
      val topics = readTopics(startPos, stream)

      val subscribe = Subscribe(header, sessionId, messageId, topics)

      log.debug("Subscribe decoded {}", subscribe)

      DecodeResult.Finished(subscribe, registry.headerDecoder())
    }
  }

  private def readTopics(startPos:Int, stream:ByteStream): List[Topic] = {
    @tailrec def readTopics0(topics:List[Topic]): List[Topic] = {
      if (canReadMore(startPos, stream)) {
        val pattern = stream.readUTF()
        val qos     = stream.readByte()
        readTopics0(Topic(pattern, QosLevel(qos)) :: topics)
      }
      else
        topics
    }
    readTopics0(Nil)
  }


  /**
   * Indicates whether or not the buffer contains enough bytes according to the
   * 'remaining length' defined in the header.
   */
  private def canReadMore(startPos: Int, stream: ByteStream): Boolean = {
    val currentPos = stream.readerIndex()
    ((currentPos - startPos) < header.remainingLength)
  }

  override def toString = "SubscribeDecoder(" + sessionId + ", " + header  + ")"

}
