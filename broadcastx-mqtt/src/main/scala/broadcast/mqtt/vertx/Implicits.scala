package broadcast.mqtt.vertx

import org.vertx.java.core.Handler

/**
 *
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
object Implicits {

  implicit def funcToHandler[T](f: (T) => Unit): Handler[T] = new Handler[T] {
    def handle(event: T) {
      f(event)
    }
  }

  implicit def procToHandler(f: ()=> Unit): Handler[Void] = new Handler[Void] {
    def handle(event: Void) {
      f()
    }
  }
}
