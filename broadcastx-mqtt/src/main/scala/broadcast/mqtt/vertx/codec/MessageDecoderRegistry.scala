package broadcast.mqtt.vertx.codec

import broadcast.mqtt.domain.{CommandType, Header}

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
trait MessageDecoderRegistry {
  def decoderFor(header: Header): Option[Decoder]
}

object DefaultMessageDecoderRegistry extends MessageDecoderRegistry {
  def decoderFor(header: Header): Option[Decoder] = header.messageType match {
    case CommandType.CONNECT =>
      Some(new ConnectDecoder(header))
    case _ =>
      None
  }
}
