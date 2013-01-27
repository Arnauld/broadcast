package broadcast.mqtt.vertx.codec

import org.vertx.java.core.net.NetSocket
import broadcast.mqtt.domain.Connack
import org.vertx.java.core.buffer.Buffer
import org.slf4j.LoggerFactory

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
object Encoders {
  def apply():Encoders = new Encoders {}
}

trait Encoders {

  private val log = LoggerFactory.getLogger(classOf[Encoders])

  def writeConnack(sock:NetSocket, connackCode:Connack.Code.Value) {
    //
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // bit     |  7  |  6  |  5  |  4  |  3  |  2  |  1  |  0   |
    // --------+-----+-----+-----+-----+-----+-----+-----+------+
    // byte 1  |  0     0     1     0  |  x  |  x     x  |  x   |
    // --------+-----------------------+-----+-----------+------+
    // byte 2  |              Remaining Length                  |
    //---------+------------------------------------------------+
    // The DUP, QoS and RETAIN flags are not used in the CONNACK message.
    // Message
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

    log.info("Connack sent: {}", buffer.toString)
    sock.write(buffer)
  }
}
