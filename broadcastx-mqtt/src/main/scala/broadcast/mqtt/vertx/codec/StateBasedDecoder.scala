package broadcast.mqtt.vertx.codec

import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.Handler
import annotation.tailrec
import broadcast.mqtt.domain.MqttMessage
import org.slf4j.LoggerFactory
import broadcast.mqtt.vertx.util.ByteStream

/**
 *
 * @param initialDecoder initial decoder
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
class StateBasedDecoder(initialDecoder: Decoder,
                        handler: MqttHandler) extends Handler[Buffer] {

  private val log = LoggerFactory.getLogger(classOf[StateBasedDecoder])

  private var decoder = initialDecoder

  private val accumulationStream = new ByteStream()

  override def handle(buffer: Buffer) {
    accumulationStream.appendBuffer(buffer)

    log.debug("Chunk received {}", buffer)

    import DecodeResult._
    @tailrec def decode0() {
      decoder.decode(accumulationStream) match {
        case Incomplete =>
        // wait for more data
        case Finished(result, newDecoder) =>
          decoder = newDecoder
          handler.handle(result.asInstanceOf[MqttMessage])
          decode0()
        case ChangeDecoder(newDecoder) =>
          decoder = newDecoder
          decode0()
        case UnsupportedType(header) =>
          handler.noDecoderFoundForType(header)
      }
    }
    decode0()
  }
}
