package broadcast.mqtt.domain

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
case class Subscribe(header: Header,
                     sessionId: SessionId,
                     messageId:Int,
                     topics:List[Topic])
