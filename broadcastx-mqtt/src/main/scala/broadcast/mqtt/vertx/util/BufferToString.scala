package broadcast.mqtt.vertx.util

import org.vertx.java.core.buffer.Buffer

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
object BufferToString {
  def asHex(buffer: Buffer): String = {
    val builder = new StringBuilder
    val bString = new StringBuilder

    for (i <- 0 until buffer.length()) {
      val b = buffer.getByte(i).asInstanceOf[Int]
      if (b < 0x10) {
        builder.append('0')
      }
      if (b < 31) {
        bString.append('•')
      }
      else {
        bString.append(b.asInstanceOf[Char])
      }

      builder.append(Integer.toHexString(b)).append(' ')

      if ((i + 1) % 16 == 0) {
        builder.append("  ").append(bString).append('\n')
        bString.setLength(0)
      }
    }
    if (bString.length > 0) {
      for (i <- bString.length until 16) {
        builder.append("   ")
      }
      builder.append("  ").append(bString).append('\n')
    }
    builder.toString()
  }
}
