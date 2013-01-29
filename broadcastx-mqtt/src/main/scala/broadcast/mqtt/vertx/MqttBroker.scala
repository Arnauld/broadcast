package broadcast.mqtt.vertx

import codec._
import org.vertx.java.deploy.Verticle
import org.vertx.java.core.net.{NetSocket, NetServer}
import org.vertx.java.core.logging.Logger
import org.vertx.java.core.json.JsonObject
import broadcast.service.AuthService
import org.vertx.java.core.eventbus.Message
import broadcast.mqtt.domain
import domain.{DisconnectReason, SessionId}

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
class MqttBroker extends Verticle {

  import Implicits._

  var log: Logger = _
  var sessions = Map[SessionId, MqttHandler]()
  val sessionListener = new SessionListener

  /**
   * Starts the verticle (invoked by the Vert.x platform).
   */
  override def start() {
    log = container.getLogger

    val server = createTcpServer()
    server.connectHandler(newSocketHandler)

    vertx.eventBus().registerHandler("connect", reconnectListener())

    val port = 7654
    log.info("Starting broker on port " + port)
    server.listen(port, "127.0.0.1")
  }

  protected def createTcpServer(): NetServer =
    vertx.createNetServer()
      .setClientAuthRequired(false)
      .setTCPKeepAlive(true)
      .setTCPNoDelay(true)

  private def newSocketHandler = (sock: NetSocket) => {
    val handler = new MqttHandler(AuthService.acceptAll(), sock)
    handler.addListener(sessionListener)
    sock.exceptionHandler(newSocketErrorHandler(handler))
    sock.dataHandler(new StateBasedDecoder(HeaderDecoder(), handler))
  }

  private def newSocketErrorHandler(handler: MqttHandler) = (e: Exception) => {
    handler.disconnect(DisconnectReason.SocketError(e))
  }

  /**
   * Listener that will disconnect existing client.
   *
   * <blockquote>
   * <p>
   * If a client with the same Client ID is already connected to the server,
   * the "older" client must be disconnected by the server before completing
   * the CONNECT flow of the new client.
   * </p>
   * <small>MQTT V3.1 Protocol Specification - section 3.1</small>
   * </blockquote>
   * @see [[broadcast.mqtt.domain.SessionId]]
   */
  private def reconnectListener() = (m: Message[JsonObject]) => {
    val body = m.body
    //
    val cliId = body.getString("clientId")
    val token = body.getString("token")
    val (toCloses, remainings) = sessions.partition({
      e => e._1.clientId == cliId && e._1.token != token
    })
    sessions = remainings
    toCloses.foreach(_._2.disconnect(DisconnectReason.Reconnect))
  }

  class SessionListener extends MqttHandlerListener {
    /**
     * Register the [[broadcast.mqtt.vertx.codec.MqttHandler]] with the
     * specified [[broadcast.mqtt.domain.SessionId]].
     *
     * @param sessionId identifier of the handler to discard
     * @param handler handler associates to the identifier
     * @see [[broadcast.mqtt.vertx.MqttBroker.reconnectListener( )]]
     */
    override def sessionIdAffected(sessionId: SessionId, handler: MqttHandler) {
      // to make sure to distinct different connections using the same client id
      // one attaches a unique token to it, this will prevent to disconnect ourself :)
      sessions += (sessionId -> handler)

      // see reconnectListener
      vertx.eventBus().publish("connect", sessionId.asJson())
    }

    /**
     * Unregister the given [[broadcast.mqtt.domain.SessionId]].
     * The corresponding [[broadcast.mqtt.vertx.codec.MqttHandler]] will not any
     * longer take part of any broadcast.
     *
     * @param sessionId identifier of the handler to discard
     */
    override def sessionDisposed(sessionId: SessionId, handler: MqttHandler) {
      sessions = sessions - sessionId
      handler.removeListener(this)
    }
  }
}
