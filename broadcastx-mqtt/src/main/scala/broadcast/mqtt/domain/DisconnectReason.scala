package broadcast.mqtt.domain

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
sealed trait DisconnectReason

object DisconnectReason {

  /**
   * The connection should be closed since a client with the same clientId
   * is reconnecting.
   */
  case object Reconnect extends DisconnectReason

  /**
   * The connection should be closed since the socket thrown an error: fail fast.
   */
  case class SocketError(e: Throwable) extends DisconnectReason

  /**
   * The connection should be closed since an unsupported message type has been received.
   */
  case object UnsupportedMessageType extends DisconnectReason

}
