package broadcast.mqtt.vertx.codec

import broadcast.mqtt.domain.{SessionId, CommandType, Header}

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
trait MessageDecoderRegistry {
  def headerDecoder():Decoder
  def decoderFor(header: Header): Option[Decoder]
  def specializesFor(sessionId:SessionId):MessageDecoderRegistry
}

object DefaultMessageDecoderRegistry extends MessageDecoderRegistry {
  def decoderFor(header: Header): Option[Decoder] = header.messageType match {
    case CommandType.CONNECT =>
      Some(new ConnectDecoder(header, this))
    case _ =>
      None
  }

  def headerDecoder() = HeaderDecoder(this)

  def specializesFor(sessionId: SessionId) =
      new SessionBasedMessageDecoderRegistry(sessionId)
}

class SessionBasedMessageDecoderRegistry(sessionId:SessionId) extends MessageDecoderRegistry {
  def decoderFor(header: Header): Option[Decoder] = header.messageType match {
    case CommandType.CONNECT =>
      Some(new ConnectDecoder(header, this))
    case CommandType.PUBLISH =>
      Some(new PublishDecoder(header, this, sessionId))
    case CommandType.SUBSCRIBE =>
      Some(new SubscribeDecoder(header, this, sessionId))
    case _ =>
      None
  }

  def headerDecoder() = HeaderDecoder(this)

  def specializesFor(sessionId: SessionId) =
    if (sessionId == this.sessionId)
      this
    else
      new SessionBasedMessageDecoderRegistry(sessionId)
}
