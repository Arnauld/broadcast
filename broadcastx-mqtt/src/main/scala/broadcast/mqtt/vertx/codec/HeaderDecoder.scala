package broadcast.mqtt.vertx.codec

import annotation.tailrec
import broadcast.mqtt.domain.{QosLevel, CommandType, Header}
import org.slf4j.LoggerFactory

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
object HeaderDecoder {
  def apply(): HeaderDecoder = new HeaderDecoder
}

class HeaderDecoder extends Decoder {

  private val log = LoggerFactory.getLogger(classOf[HeaderDecoder])

  override def decode(stream: ByteStream) = {
    if (stream.readableBytes() < 2) {
      // header is not yet complete
      DecodeResult.Incomplete
    }
    else {
      val b1 = stream.readByte()

      // The message header for each MQTT command message contains a fixed header.
      // The table below shows the fixed header format.

      // --------+-----+-----+-----+-----+-----+-----+-----+------+
      // bit     |  7  |  6  |  5  |  4  |  3  |  2  |  1  |  0   |
      // --------+-----+-----+-----+-----+-----+-----+-----+------+
      // byte 1  |     Message Type      | DUP |    QoS    |Retain|
      // --------+-----------------------+-----+-----------+------+
      // byte 2  |              Remaining Length                  |
      //---------+------------------------------------------------+

      // Make sure remaining length is complete, otherwise wait for more data
      decodeRemainingLength(stream) match {
        case None =>
          // The whole bytes were not received yet - return null.
          // This method will be invoked again when more packets are
          // received and appended to the buffer.

          // Reset to the marked position to read the length field again
          // next time.
          stream.resetReaderIndex()
          DecodeResult.Incomplete
        case Some(length) =>
          // Message Type mask: 0b11110000 === 0x00F0
          // DUP          mask: 0b00001000 === 0x0008
          val messageType = ((b1 & 0x00F0) >> 4)
          val dupFlag = (((b1 & 0x0008) >> 3) == 1)
          val qosLevel = ((b1 & 0x0006) >> 1)
          val retainFlag = ((b1 & 0x0001) == 1)
          val header = Header(CommandType(messageType), dupFlag, QosLevel(qosLevel), retainFlag, length)

          log.debug("Header decoded {}", header)

          // Hand off the remaining data to the next decoder
          stream.discardReadBytes()
          decoderFor(header) match {
            case None =>
              DecodeResult.UnsupportedType(header)
            case Some(decoder) =>
              DecodeResult.ChangeDecoder(decoder)
          }
      }
    }
  }

  def decoderFor(header: Header): Option[Decoder] = header.messageType match {
    case CommandType.CONNECT =>
      Some(new ConnectDecoder(header))
    case _ =>
      None
  }

  //
  // multiplier = 1
  // value = 0
  // do
  //    digit = 'next digit from stream'
  //    value += (digit AND 127) * multiplier
  //    multiplier *= 128
  // while ((digit AND 128) != 0)
  //
  // (MQTT V3.1 Protocol Specification - section 2.1)
  def decodeRemainingLength(stream: ByteStream): Option[Long] = {
    @tailrec def decodeRemainingLength0(multiplier: Long, length: Long): Option[Long] = {
      if (stream.readableBytes() == 0)
      // not enough data
        None
      else {
        val digit = stream.readByte()
        val newLength = length + (digit & 127) * multiplier
        val newMultiplier = multiplier * 128
        if ((digit & 128) != 0)
          decodeRemainingLength0(newMultiplier, newLength)
        else
          Some(newLength)
      }
    }
    decodeRemainingLength0(1, 0)
  }
}
