package broadcast.mqtt.vertx.codec

import broadcast.mqtt.domain._
import org.slf4j.LoggerFactory
import broadcast.mqtt.vertx.util.ByteStream
import broadcast.mqtt.domain.Header
import broadcast.mqtt.domain.Message
import broadcast.mqtt.domain.Connect
import scala.Some

/**
 *
 *
 *
 * (MQTT V3.1 Protocol Specification - section 3.1)
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
class ConnectDecoder(header: Header, registry:MessageDecoderRegistry) extends Decoder {

  val log = LoggerFactory.getLogger(classOf[ConnectDecoder])

  def decode(stream: ByteStream) = {
    if (stream.readableBytes() < header.remainingLength) {
      log.debug("Not enough data to decode `Connect` message ({}/{})", stream.readableBytes(), header.remainingLength)
      // wait until all data has been received to start decoding
      DecodeResult.Incomplete
    }
    else {
      val startPos = stream.readerIndex()
      val protocolName = stream.readUTF()
      val protocolVersion = stream.readUnsignedByte()

      log.debug("Protocol {} v{}", protocolName, protocolVersion)

      // --- VARIABLE HEADER

      // Connect flags
      // ------+------+-------+-------+-------+-------+-------+-------+---------+
      // bit   |   7  |   6   |   5   |   4   |   3   |   2   |   1   |    O    |
      // ------+------+-------+-------+-------+-------+-------+-------+---------+
      //       | User | Pass- | Will  |     Will      | Will  | Clean | Reserved|
      //       | Name |  word | Retain|     QoS       | Flag  |Session|         |
      // ------+------+-------+-------+---------------+-------+-------+---------+
      // 0b0000_0010: 0x02
      // 0b0000_0100: 0x04
      // 0b0001_1000: 0x18
      // 0b0010_0000: 0x20
      // 0b0100_0000: 0x40
      // 0b1000_0000: 0x80
      val b1 = stream.readByte()
      val hasUsername = (((b1 & 0x80) >> 7) == 1)
      val hasPassword = (((b1 & 0x40) >> 6) == 1)
      val willRetain = (((b1 & 0x20) >> 5) == 1)
      val willQoS = (((b1 & 0x18) >> 3))
      val willFlag = (((b1 & 0x04) >> 2) == 1)
      val cleanSession = (((b1 & 0x02) >> 1) == 1)

      // The Keep Alive timer is a 16-bit value that represents the number of seconds for the time period.
      val keepAlive = stream.read16bitInt()

      // --- PAYLOAD

      val clientId = stream.readUTF()

      val willMessage =
        if (willFlag) {
          val willTopic = stream.readUTF()
          val willMessageBody = stream.readUTF()
          Some(Message(willTopic, willMessageBody, QosLevel(willQoS), willRetain))
        }
        else
          None

      /*
      Note that, for compatibility with the original MQTT V3 specification,
      the Remaining Length field from the fixed header takes precedence over
      the User Name flag. Server implementations must allow for the possibility
      that the User Name flag is set, but the User Name string is missing.
      This is valid, and connections should be allowed to continue.

      Same for password...

      (MQTT V3.1 Protocol Specification - section 3.1)

      => one need to check if there is enough bytes remaining to read them both
       */
      val username =
        if (hasUsername && canReadMore(startPos, stream))
          Some(stream.readUTF())
        else
          None

      val password =
        if (hasPassword && canReadMore(startPos, stream))
          Some(stream.readUTF())
        else
          None

      val connect = Connect(header,
        protocolName,
        protocolVersion,
        cleanSession,
        keepAlive,
        clientId, username, password,
        willMessage)

      log.debug("Connect decoded {}", connect)

      DecodeResult.FinishedButWaitingForSessionId(connect,
        (s:SessionId) =>
          registry.specializesFor(s).headerDecoder())
    }
  }

  /**
   * Indicates whether or not the buffer contains enough bytes according to the
   * 'remaining length' defined in the header.
   */
  private def canReadMore(startPos: Int, stream: ByteStream): Boolean = {
    val currentPos = stream.readerIndex()
    ((currentPos - startPos) < header.remainingLength)
  }

  override def toString = "ConnectDecoder(" + header + ")"
}
