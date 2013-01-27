package broadcast.mqtt.vertx.codec

import org.scalatest.{FeatureSpec, GivenWhenThen, BeforeAndAfter}
import org.scalatest.matchers.ShouldMatchers._
import org.vertx.java.core.buffer.Buffer
import broadcast.util.Bytes._
import org.scalatest.matchers.{MatchResult, Matcher}
import broadcast.mqtt.domain.{QosLevel, CommandType, Header}

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
class HeaderDecoderSpec extends FeatureSpec with GivenWhenThen with BeforeAndAfter {

  val beAChangeDecoder = Matcher {
    (m:DecodeResult) => MatchResult(m match {
      case DecodeResult.ChangeDecoder(decoder) => true
      case _ => false
    }, "wasn't a ChangeDecoder", "was a ChangeDecoder")
  }

  val anyDecoder = new Decoder {
    def decode(stream: ByteStream) = null
  }

  feature("Header must be complete to be decoded") {
    scenario("Empty content") {
      given("an empty stream")
      val stream = new ByteStream()

      and("a new Header decoder")
      val decoder = HeaderDecoder()

      when("one attempts to decode a header")
      val result = decoder.decode(stream)

      then("it indicates there's not enough data")
      result should be === DecodeResult.Incomplete
    }

    scenario("One byte content") {
      given("an empty stream fed with one byte")
      val stream = new ByteStream()
      val bytes: Array[Byte] = "0111 1101"
      stream.appendBuffer(new Buffer(bytes))

      and("a new Header decoder")
      val decoder = HeaderDecoder()

      when("one attempts to decode a header")
      val result = decoder.decode(stream)

      then("it indicates there's not enough data")
      result should be === DecodeResult.Incomplete
    }

    scenario("Two bytes content") {
      given("an empty stream fed with two bytes")
      val stream = new ByteStream()
      val bytes: Array[Byte] = "0011 0000, 0000 0010"
      stream.appendBuffer(new Buffer(bytes))

      and("a new Header decoder")
      val anyDecoderRegistry = new GrabHeaderDecoderRegistry
      val decoder = HeaderDecoder(anyDecoderRegistry)

      when("one attempts to decode a header")
      val result = decoder.decode(stream)

      then("Header is sucessfully decoded")
      anyDecoderRegistry.header should be === Header(CommandType.PUBLISH, false, QosLevel.AtMostOnce, false, 2)

      and("it indicates a decoder change")
      result should be === DecodeResult.ChangeDecoder(anyDecoder)

    }

  }

  feature("Header remaining length is successfully read") {
    scenario("a value small enough to not have to decode it") {
      given("a stream fed with a small byte")
      val stream = new ByteStream()
      val bytes: Array[Byte] = "0000 0010"
      stream.appendBuffer(new Buffer(bytes))

      when("one attempts to decode the remaining length")
      val result = HeaderDecoder.decodeRemainingLength(stream)

      then("The remaining length is sucessfully decoded")
      result should be === Some(2)
    }

    scenario("the maximum value of the remaining length is successfully read") {
      given("a stream fed with the 4 bytes representing the maximum value allowed")
      val stream = new ByteStream()
      val bytes = Array(0xFF, 0xFF, 0xFF, 0x7F).map(_.asInstanceOf[Byte])
      stream.appendBuffer(new Buffer(bytes))

      when("one attempts to decode the remaining length")
      val result = HeaderDecoder.decodeRemainingLength(stream)

      then("The remaining length is sucessfully decoded")
      result should be === Some(268435455)
    }
  }

  class GrabHeaderDecoderRegistry extends MessageDecoderRegistry {
    var header:Header = _
    def decoderFor(h: Header) = {
      header = h
      Some(anyDecoder)
    }
  }

}

