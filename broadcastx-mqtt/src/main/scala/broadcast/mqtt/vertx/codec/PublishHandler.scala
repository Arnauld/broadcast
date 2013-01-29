package broadcast.mqtt.vertx.codec

import broadcast.mqtt.domain.Publish

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
trait PublishHandler {
  this: BaseHandler =>

  /**
   * @see [[broadcast.mqtt.domain.Connect]]
   */
  def handlePublish(msg: Publish) {
  }

}
