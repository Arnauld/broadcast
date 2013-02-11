package broadcast.mqtt.domain

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
class PublishSpec extends FunSpec with ShouldMatchers {

  val DUMMY_HEADER = Header(CommandType.PUBLISH, DUP = false, QosLevel.AtMostOnce, retain = false, 17)
  val DUMMY_SESSID = SessionId("cli","tok")

  describe("payload") {
    it("should be correctly retrieved from raw data") {
      val raw = Array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).map(_.asInstanceOf[Byte])
      val p = Publish(DUMMY_HEADER, DUMMY_SESSID, "topic", Some(1), 8, raw)
      p.payload() should equal(Array(4, 5, 6, 7, 8, 9, 10, 11).map(_.asInstanceOf[Byte]))
    }
  }
}
