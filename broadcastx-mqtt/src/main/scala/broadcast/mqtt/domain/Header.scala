package broadcast.mqtt.domain

/**
 *
 * @param messageType Message Type
 * @param DUP This flag is set when the client or server attempts to re-deliver a
 *            PUBLISH, PUBREL, SUBSCRIBE or UNSUBSCRIBE message. This applies to
 *            messages where the value of QoS is greater than zero (0), and an
 *            acknowledgment is required. When the DUP bit is set, the variable
 *            header includes a Message ID.
 *            The recipient should treat this flag as a hint as to whether the
 *            message may have been previously received. It should not be relied
 *            on to detect duplicates.
 * @param QoS    This flag indicates the level of assurance for delivery of a PUBLISH
 *               message.
 * @param retain This flag is only used on PUBLISH messages. When a client sends a
 *               PUBLISH to a server, if the Retain flag is set (1), the server should
 *               hold on to the message after it has been delivered to the current subscribers.<br/>
 *               When a new subscription is established on a topic, the last retained message
 *               on that topic should be sent to the subscriber with the Retain flag set. If
 *               there is no retained message, nothing is sent.<br/>
 *               This is useful where publishers send messages on a "report by exception"
 *               basis, where it might be some time between messages. This allows new
 *               subscribers to instantly receive data with the retained, or Last Known Good,
 *               value. <br/>
 *               When a server sends a PUBLISH to a client as a result of a subscription that
 *               already existed when the original PUBLISH arrived, the Retain flag should not
 *               be set, regardless of the Retain flag of the original PUBLISH. This allows a
 *               client to distinguish messages that are being received because they were
 *               retained and those that are being received "live".<br/>
 *               Retained messages should be kept over restarts of the server.<br/>
 *               A server may delete a retained message if it receives a message with a
 *               zero-length payload and the Retain flag set on the same topic.<br/>
 * @param remainingLength Represents the number of bytes remaining within the current message,
 *               including data in the variable header and the payload
 * @param raw raw bytes that constitutes the header
 * @see QosLevel
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
case class Header(messageType: CommandType.Value,
                  DUP: Boolean,
                  QoS: QosLevel.Value,
                  retain: Boolean,
                  remainingLength: Long,
                  raw: Option[Array[Byte]]
                  )