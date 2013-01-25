package broadcast.mqtt.vertx

import codec._
import org.vertx.java.deploy.Verticle
import org.vertx.java.core.net.{NetSocket, NetServer}
import org.vertx.java.core.logging.Logger
import org.vertx.java.core.json.JsonObject
import broadcast.service.AuthService
import org.vertx.java.core.eventbus.Message

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
class MqttBroker extends Verticle with MqttHandlerGateway {

  import Implicits._

  var log: Logger = _
  var sessions = Map[SessionId, MqttHandler]()

  /**
   *
   */
  override def start() {
    log = container.getLogger

    val server: NetServer = vertx.createNetServer()
    server.setClientAuthRequired(false)
    server.setTCPKeepAlive(true)
    server.setTCPNoDelay(true)
    server.connectHandler({
      sock: NetSocket =>
        log.info("A client has connected!")
        handleNewSocket(sock)
    })

    vertx.eventBus().registerHandler("connect", {
      m: Message[JsonObject] =>
        val body = m.body
        // If a client with the same Client ID is already connected to the server,
        // the "older" client must be disconnected by the server before completing
        // the CONNECT flow of the new client.
        // MQTT V3.1 Protocol Specification - section 3.1
        val cliId = body.getString("clientId")
        val token = body.getString("token")
        val (toCloses, remainings) = sessions.partition({
          e => e._1.clientId == cliId && e._1.token != token
        })
        sessions = remainings
        toCloses.foreach({
          e =>
            e._2.disconnectCausedByConnect()
        })
    })

    val port = 7654
    log.info("Starting broker on port " + port)
    server.listen(port, "127.0.0.1")
  }

  private def handleNewSocket(sock: NetSocket) {

    val handler = new MqttHandler(this, AuthService.acceptAll(), sock)

    sock.exceptionHandler({
      e: Exception =>
        log.error("Oops, something went wrong ({})", handler.getSessionId, e)
        handler.disconnectCausedBySocketError()
    })
    sock.dataHandler(new StateBasedDecoder(HeaderDecoder(), handler))

  }

  def register(sessionId: SessionId, handler: MqttHandler) {
    // to make sure to distinct different connections using the same client id
    // one attaches a unique token to it, this will prevent to disconnect ourself :)
    sessions += (sessionId -> handler)

    vertx.eventBus().publish("connect", sessionId.asJson())
  }

  def unregister(sessionId: SessionId) {
    sessions = sessions - sessionId
  }
}
