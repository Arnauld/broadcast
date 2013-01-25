package broadcast.mqtt.domain

/**
 *
 *
 * MQTT V3.1 Protocol Specification - sections 2.2 & 3.1
 *
 * @param header - Fixed header
 * @param protocolName - (Variable header) This field is a UTF-encoded string that represents
 *                     the protocol name <code>MQIsdp</code>, capitalized as shown.
 * @param protocolVersion - (Variable header) The field is an 8-bit unsigned value that
 *                        represents the revision level of the protocol used by the client
 * @param cleanSession - (Variable header) If not set (0), then the server must store
 *                     the subscriptions of the client after it disconnects. This
 *                     includes continuing to store QoS 1 and QoS 2 messages for the
 *                     subscribed topics so that they can be delivered when the client
 *                     reconnects. The server must also maintain the state of in-flight
 *                     messages being delivered at the point the connection is lost.
 *                     This information must be kept until the client reconnects.<br/>
 *                     If set (1), then the server must discard any previously maintained
 *                     information about the client and treat the connection as "clean".
 *                     The server must also discard any state when the client disconnects.<br/>
 *                     Typically, a client will operate in one mode or the other and not change.
 *                     The choice will depend on the application. A clean session client will
 *                     not receive stale information and it must resubscribe each time it
 *                     connects. A non-clean session client will not miss any QoS 1 or QoS 2
 *                     messages that were published whilst it was disconnected. QoS 0 messages
 *                     are never stored, since they are delivered on a best efforts basis.<br/>
 *                     This flag was formerly known as "Clean start". It has been renamed to
 *                     clarify the fact it applies to the whole session and not just to the
 *                     initial connect.
 * @param keepAliveInSeconds - (Variable header) The Keep Alive timer, measured in seconds,
 *                           defines the maximum time interval between messages received from
 *                           a client. It enables the server to detect that the network
 *                           connection to a client has dropped, without having to wait for
 *                           the long TCP/IP timeout. The client has a responsibility to send
 *                           a message within each Keep Alive time period. In the absence of a
 *                           data-related message during the time period, the client sends a
 *                           PINGREQ message, which the server acknowledges with a PINGRESP message.<br/>
 *                           If the server does not receive a message from the client within one and
 *                           a half times the Keep Alive time period (the client is allowed "grace"
 *                           of half a time period), it disconnects the client as if the client had
 *                           sent a DISCONNECT message. This action does not impact any of the
 *                           client's subscriptions. See DISCONNECT for more details.<br/>
 *                           If a client does not receive a PINGRESP message within a Keep Alive
 *                           time period after sending a PINGREQ, it should close the TCP/IP socket
 *                           connection.<br/>
 *                           The Keep Alive timer is a 16-bit value that represents the number of
 *                           seconds for the time period. <br/>
 *                           (MQTT V3.1 Protocol Specification - section 2.2)
 * @param clientId - The Client Identifier (Client ID) is between 1 and 23 characters long,
 *                 and uniquely identifies the client to the server. It must be unique across
 *                 all clients connecting to a single server, and is the key in handling
 *                 Message IDs messages with QoS levels 1 and 2. If the Client ID contains
 *                 more than 23 characters, the server responds to the CONNECT message with
 *                 a CONNACK return code 2: Identifier Rejected.
 * @param username - A connecting client can specify a user name and a password, and setting
 *                 the flag bits signifies that a User Name, and optionally a password, are
 *                 included in the payload of a CONNECT message.<br/>
 *                 If the User Name flag is set, the User Name field is mandatory, otherwise
 *                 its value is disregarded.
 * @param password - If the Password flag is set, the Password field is mandatory, otherwise
 *                 its value is disregarded. It is not valid to supply a password without
 *                 supplying a user
 * @param willMessage - The Will message defines that a message is published on behalf of the
 *                    client by the server when either an I/O error is encountered by the server
 *                    during communication with the client, or the client fails to communicate
 *                    within the Keep Alive timer schedule. Sending a Will message is not triggered
 *                    by the server receiving a DISCONNECT message from the client.
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
case class Connect(header: Header,
                   protocolName: String,
                   protocolVersion: Short,
                   cleanSession: Boolean,
                   keepAliveInSeconds: Int,
                   clientId: String,
                   username: Option[String],
                   password: Option[String],
                   willMessage: Option[Message]) extends MqttMessage

object Connack {

  sealed trait Code

  /**
   * MQTT V3.1 Protocol Specification - section 3.2
   */
  object Code extends Enumeration {
    /**
     * Connection Accepted
     */
    val Accepted = Value(0)

    /**
     * Connection Refused: unacceptable protocol version
     */
    val UnacceptableProtocolVersion = Value(1)

    /**
     * Connection Refused: identifier rejected
     */
    val IdentifierRejected = Value(2)

    /**
     * Connection Refused: server unavailable
     */
    val ServerUnavailable = Value(3)

    /**
     * Connection Refused: bad user name or password
     */
    val BadUserOrPassword = Value(4)

    /**
     * Connection Refused: not authorized
     */
    val NotAuthorized = Value(5)

    // 6 - 255
    // Reserved for future use
  }

  val header = Header(messageType = CommandType.CONNACK,
    DUP = false,
    QoS = QosLevel(0),
    retain = false,
    remainingLength = 2)

}

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
case class Connack(returnCode: Connack.Code.Value) extends MqttMessage {
  def header() = Connack.header
}
