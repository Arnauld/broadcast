package broadcast.util

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
object Bytes {
  implicit def binary(binary: String): Array[Byte] =
    binary.split("[,;]").map({
      value =>
        Integer.parseInt(value.replace(" ", ""), 2).asInstanceOf[Byte]
    }).array
}
