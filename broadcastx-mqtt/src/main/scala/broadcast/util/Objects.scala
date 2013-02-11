package broadcast.util

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
object Objects {
  def o(any:Any*):Array[AnyRef] = any.toArray.map(_.asInstanceOf[AnyRef])
}
