package broadcast.mqtt.vertx.codec

import org.vertx.java.core.net.NetSocket
import broadcast.mqtt.domain.{QosLevel, Connack}
import org.vertx.java.core.buffer.Buffer
import org.slf4j.LoggerFactory
import broadcast.util.Bytes
import broadcast.mqtt.vertx.util.{ByteStream, BufferToString}

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
trait Encoder {

  def sock: NetSocket

  private val log = LoggerFactory.getLogger(classOf[Encoder])

  def writeConnack(connackCode:Connack.Code.Value) {
    //
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // bit     |  7  |  6  |  5  |  4  |  3  |  2  |  1  |  0   |
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // byte 1  |  0     0     1     0  |  x  |  x     x  |  x   |
    // --------+-----------------------+-----+-----------+------+
    // byte 2  |              Remaining Length                  |
    //---------+------------------------------------------------+
    // The DUP, QoS and RETAIN flags are not used in the CONNACK message.
    // MQTT V3.1 Protocol Specification - sections 3.2

    val buffer = new Buffer(4)

    // byte 1: 0b_0010_0000 = 0x20
    buffer.appendByte(0x20.asInstanceOf[Byte])
    // byte 2: remaining length = 2 => 0x02
    buffer.appendByte(0x02.asInstanceOf[Byte])
    // 1st byte; unused => 0x00
    buffer.appendByte(0x00.asInstanceOf[Byte])
    // 2nd byte: connack return code
    buffer.appendByte(connackCode.id.asInstanceOf[Byte])

    log.debug("Connack sent: {}", BufferToString.asHex(buffer))
    sock.write(buffer)
  }

  def writePuback(messageId: Int) {
    //
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // bit     |  7  |  6  |  5  |  4  |  3  |  2  |  1  |  0   |
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // byte 1  |  0     1     0     0  |  x  |  x     x  |  x   |
    // --------+-----------------------+-----+-----------+------+
    // byte 2  |              Remaining Length                  |
    //---------+------------------------------------------------+
    // The DUP, QoS and RETAIN flags are not used in the PUBACK message.
    // MQTT V3.1 Protocol Specification - sections 3.4

    val buffer = new Buffer(4)

    // byte 1: 0b_0100_0000 = 0x40
    buffer.appendByte(0x40.asInstanceOf[Byte])
    // byte 2: remaining length = 2 => 0x02
    buffer.appendByte(0x02.asInstanceOf[Byte])

    // variable header:
    // Contains the Message Identifier (Message ID) for the PUBLISH
    // message that is being acknowledged.
    ByteStream.writeUnsignedShort(buffer, messageId)  // 16-bit unsigned integer

    log.debug("Puback sent: {}", BufferToString.asHex(buffer))
    sock.write(buffer)
  }

  def writePubrec(messageId: Int) {
    //
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // bit     |  7  |  6  |  5  |  4  |  3  |  2  |  1  |  0   |
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // byte 1  |  0     1     0     1  |  x  |  x     x  |  x   |
    // --------+-----------------------+-----+-----------+------+
    // byte 2  |              Remaining Length                  |
    //---------+------------------------------------------------+
    // The DUP, QoS and RETAIN flags are not used in the PUBREC message.
    // MQTT V3.1 Protocol Specification - sections 3.5

    val buffer = new Buffer(4)

    // byte 1: 0b_0101_0000 = 0x50
    buffer.appendByte(0x50.asInstanceOf[Byte])
    // byte 2: remaining length = 2 => 0x02
    buffer.appendByte(0x02.asInstanceOf[Byte])

    // variable header:
    // Contains the Message Identifier (Message ID) for the PUBLISH
    // message that is being acknowledged.
    ByteStream.writeUnsignedShort(buffer, messageId)  // 16-bit unsigned integer

    log.debug("Pubrec sent: {}", BufferToString.asHex(buffer))
    sock.write(buffer)
  }

  def writeSuback(messageId:Int, grantedQos:List[QosLevel.Value]) {
    //
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // bit     |  7  |  6  |  5  |  4  |  3  |  2  |  1  |  0   |
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // byte 1  |  1     0     0     1  |  x  |  x     x  |  x   |
    // --------+-----------------------+-----+-----------+------+
    // byte 2  |              Remaining Length                  |
    //---------+------------------------------------------------+
    // The DUP, QoS and RETAIN flags are not used in the SUBACK message.
    // MQTT V3.1 Protocol Specification - sections 3.9

    // write payload first to calculate the 'remaining length'
    val content = new Buffer(grantedQos.size + 2)

    ByteStream.writeUnsignedShort(content, messageId)  // 16-bit unsigned integer
    grantedQos.foreach({ qos => content.appendByte( (qos.id & 0xF).asInstanceOf[Byte] )})

    val len = content.length()

    val header = new Buffer(2)
    // byte 1: 0b_1001_0000 = 0x90
    header.appendByte(0x90.asInstanceOf[Byte])
    HeaderDecoder.encodeRemainingLength(len, header)

    sock.write(header)
    sock.write(content)
  }

}
