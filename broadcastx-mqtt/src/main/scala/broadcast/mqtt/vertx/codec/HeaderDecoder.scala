package broadcast.mqtt.vertx.codec

import annotation.tailrec
import broadcast.mqtt.domain.{QosLevel, CommandType, Header}
import org.slf4j.LoggerFactory
import broadcast.mqtt.vertx.util.ByteStream
import org.vertx.java.core.buffer.Buffer

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
object HeaderDecoder {
  def apply(decoderRegistry: MessageDecoderRegistry = DefaultMessageDecoderRegistry): HeaderDecoder = new HeaderDecoder(decoderRegistry)

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

  // do
  //   digit = X MOD 128
  //   X = X DIV 128
  //   // if there are more digits to encode, set the top bit of this digit
  //   if ( X > 0 )
  //     digit = digit OR 0x80
  //   endif
  //   'output' digit
  // while ( X > 0 )
  //
  // (MQTT V3.1 Protocol Specification - section 2.1)
  def encodeRemainingLength(remainingLength:Long, buffer:Buffer) {
    @tailrec def encodeRemainingLength0(x:Long) {
      val digit = (x % 128).asInstanceOf[Int]
      val newX  = (x / 128)
      if (newX > 0) {
        buffer.appendByte( (digit | 0x80 ).asInstanceOf[Byte] )
        encodeRemainingLength0(newX)
      }
      else {
        buffer.appendByte( digit.asInstanceOf[Byte] )
      }
    }
    encodeRemainingLength0(remainingLength)
  }


}

class HeaderDecoder(decoderRegistry: MessageDecoderRegistry) extends Decoder {

  private val log = LoggerFactory.getLogger(classOf[HeaderDecoder])

  override def decode(stream: ByteStream) = {
    if (stream.readableBytes() < 2) {
      log.debug("Header incomplete got: {} bytes", stream.readableBytes())
      // header is not yet complete
      DecodeResult.Incomplete
    }
    else {
      val startPos = stream.readerIndex()

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
      HeaderDecoder.decodeRemainingLength(stream) match {
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

          val endPos = stream.readerIndex()
          val headerLen = endPos - startPos
          val raw = new Array[Byte](headerLen)
          stream.readerIndex(startPos)
          stream.readBytes(raw)

          val header =
            Header(
              CommandType(messageType),
              dupFlag,
              QosLevel(qosLevel),
              retainFlag,
              length,
              Some(raw))

          log.debug("Header decoded {}", header)

          // Hand off the remaining data to the next decoder
          stream.discardReadBytes()
          decoderRegistry.decoderFor(header) match {
            case None =>
              DecodeResult.UnsupportedType(header)
            case Some(decoder) =>
              DecodeResult.ChangeDecoder(decoder)
          }
      }
    }
  }

  override def toString = "HeaderDecoder()"

}
