package broadcast.mqtt.domain

import broadcast.mqtt.vertx.util.ByteStream


/**
 *
 * @param header
 * @param sessionId
 * @param topic
 * @param messageId
 * @param payloadLength indicates the length of the payload that is:
 *                      the length of the variable header *minus* the length used to encode
 *                      both the topic name and the optional message id.
 *                      Payload is then the last `payloadLength` bytes of `raw`.
 * @param raw raw bytes (including both header and variable header content) of the initial Publish message
 */
case class Publish(header: Header,
                   sessionId: SessionId,
                   topic: String,
                   messageId: Option[Int],
                   payloadLength: Int,
                   raw: Option[Array[Byte]]) extends MqttMessage {
  def write(socket:MqttSocket) {
    socket.write(header.raw.get)
    socket.write(raw.get)
  }

  def rawWithHeader(): Array[Byte] = {
    val headerBytes = header.raw.get
    val msgBytes = raw.get
    val len = headerBytes.length + msgBytes.length
    val res = new Array[Byte](len)
    Array.copy(headerBytes, 0, res, 0, headerBytes.length)
    Array.copy(msgBytes, 0, res, headerBytes.length, msgBytes.length)
    res
  }

  def payload(): Option[Array[Byte]] =
    raw match {
      case Some(r) =>
        val slice = r.slice(r.length - payloadLength, r.length)
        Some(slice)
      case None =>
        None
    }
}


object Publish {
  def topic(raw:Array[Byte]):String =
    // hopefully topic is the first utf string of the variable header
    ByteStream.readUTF(raw, 0)
}