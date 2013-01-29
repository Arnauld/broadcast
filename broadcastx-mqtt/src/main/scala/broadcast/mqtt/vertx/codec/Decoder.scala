package broadcast.mqtt.vertx.codec

import broadcast.mqtt.domain.{SessionId, Header}
import broadcast.mqtt.vertx.util.ByteStream

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
trait Decoder {
  def decode(stream: ByteStream): DecodeResult
}

sealed trait DecodeResult

object DecodeResult {

  case object Incomplete extends DecodeResult

  case class Finished(result: AnyRef, decoder: Decoder) extends DecodeResult

  case class FinishedButWaitingForSessionId(result: AnyRef, nextDecoderFunc:(SessionId) => Decoder) extends DecodeResult

  case class ChangeDecoder(newDecoder: Decoder) extends DecodeResult

  case class UnsupportedType(header: Header) extends DecodeResult

}
