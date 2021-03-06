# Broadcast.x: a broadcast plateform for push notification and alerting 

Based on [Vert.x](http://vertx.io) and written in [scala](http://www.scala-lang.org/)

In progress: a [MQTT](http://mqtt.org/) broker


Next? (random ideas)

* A JSON equivalent of MQTT for web client (through SockJS)
* Support for [Stomp](http://stomp.github.com//) for both TCP and HTTP: don't Stomp without a Sock...
* Gateway to [Redis](http://redis.io)
* Gateway to [AMQP](http://www.amqp.org/)

# Broker

Compile:

	sbt
	> compile
	> project broadcast-mqtt
	> prepareDist

All dependencies are then copied into `lib_dist` and can be easily added to the verticle classpath

Launch:

    vertx run broadcast.mqtt.vertx.MqttBroker -cp broadcastx-mqtt/target/scala-2.9.2/classes:lib_dist/scala-library.jar:lib_dist/slf4j-api-1.6.4.jar:lib_dist/logback-classic-1.0.3.jar:lib_dist/logback-core-1.0.3.jar

# Vert.x

http://vertxproject.wordpress.com/2012/05/18/where-vert-x-delivers-over-node/

> Unlike Node.js Vert.x doesn’t make you do *everything* on an event loop. 
> You can choose instead to run long running tasks or blocking calls using a thread pool if that’s more appropriate.


# MQTT

* [MQTT](http://mqtt.org/)
* [Power Profiling: HTTPS Long Polling vs. MQTT with SSL, on Android](http://stephendnicholas.com/archives/1217)


# Misc

Gridster / proprioception
