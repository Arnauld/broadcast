package broadcast.mqtt.domain

/**
 * 
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
case class Message(topic:String,
                   message:String,
                   QoS:QosLevel.Value,
                   retain:Boolean)