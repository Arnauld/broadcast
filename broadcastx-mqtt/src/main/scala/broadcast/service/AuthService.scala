package broadcast.service

object AuthService {
  def acceptAll():AuthService = new AuthService {
    override def isAuthorized(username:Option[String], password:Option[String]):Boolean = true
  }
}

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
trait AuthService {
  def isAuthorized(username:Option[String], password:Option[String]):Boolean
}
