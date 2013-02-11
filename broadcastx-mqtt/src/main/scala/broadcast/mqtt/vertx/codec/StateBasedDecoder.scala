package broadcast.mqtt.vertx.codec

import org.vertx.java.core.buffer.Buffer
import org.vertx.java.core.Handler
import annotation.tailrec
import broadcast.mqtt.domain.{SessionId, MqttMessage}
import org.slf4j.LoggerFactory
import broadcast.mqtt.vertx.util.{BufferToString, ByteStream}
import broadcast.util.Objects

/**
 *
 * @param initialDecoder initial decoder
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
class StateBasedDecoder(initialDecoder: Decoder,
                        handler: MqttHandler) extends Handler[Buffer] {

  private val log = LoggerFactory.getLogger(classOf[StateBasedDecoder])

  var decoder: Option[Decoder] = Some(initialDecoder)

  private val accumulationStream = new ByteStream()

  override def handle(buffer: Buffer) {
    accumulationStream.appendBuffer(buffer)
    log.debug("Chunk received (current decoder {}) \n{}", decoder, BufferToString.asHex(buffer))
    attemptDecoding()
  }

  def attemptDecoding() {
    @tailrec def attemptDecoding0() {
      val readerIndex: Int = accumulationStream.readerIndex()

      decoder match {
        case None => // nothing can be performed yet, need a decoder
        case Some(d) =>

          import DecodeResult._
          log.debug("Decoding data using decoder {} (readerIndex {} / readableBytes {})",
            Objects.o(d, readerIndex, accumulationStream.readableBytes()))

          d.decode(accumulationStream) match {
            case Incomplete => // wait for more data
              accumulationStream.readerIndex(readerIndex)

            case FinishedButWaitingForSessionId(result, nextDecoderFunc) =>
              accumulationStream.discardReadBytes()

              decoder = None
              handler.addListener(new MqttHandlerListener {
                override def sessionIdAffected(sessionId: SessionId, handler:MqttHandler) {
                  log.info("SessionId affected, decoder can be defined to read next incomming data")
                  decoder = Some(nextDecoderFunc(sessionId))
                  handler.removeListener(this)
                  attemptDecoding()
                }
              })
              handler.handle(result.asInstanceOf[MqttMessage])
              attemptDecoding0()

            case Finished(result, newDecoder) =>
              accumulationStream.discardReadBytes()

              decoder = Some(newDecoder)
              handler.handle(result.asInstanceOf[MqttMessage])
              if(accumulationStream.readableBytes()>0)
                attemptDecoding0()

            case ChangeDecoder(newDecoder) =>
              accumulationStream.discardReadBytes()

              decoder = Some(newDecoder)
              attemptDecoding0()

            case UnsupportedType(header) =>
              accumulationStream.discardReadBytes()

              handler.noDecoderFoundForType(header)
          }
      }
    }
    attemptDecoding0()
  }
}
