//
// Cumquats Server build file
//

import sbt._
import Keys._

//
// Build setup
//
object BroadcastXBuild extends Build {

  val prepareRun = TaskKey[Seq[File]]("prepareDist", "Prepare the dist...")

  def prepareRunFiles(jar: java.io.File, dependencies: Seq[Attributed[File]]): Seq[java.io.File] = {
    println("Preparing dist...")
    val files = jar +: (for (f <- dependencies) yield f.data)
    val dist = file(".") / "lib_dist"
    if(dist.exists())
      IO.delete(dist)
    dist.mkdirs()
    val blacklist = List("vertx", "hazelcast", "jackson", "netty")
    for (f <- files; if (!f.isDirectory); if (blacklist.forall({ n => !f.getPath.contains(n)}))) {
      IO.copyFile(f, dist / f.getName)
    }
    files
  }

  //
  // Settings
  //
  lazy val defaultSettings = Defaults.defaultSettings ++ Seq(
    // Info
    organization := "org.technbolts",
    version := "0.1.0",
    scalaVersion := "2.9.2",

    // Repositories
    resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
      "Local maven" at ("file://" + Path.userHome + "/.m2/repository")),

    // Compile options
    scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked", "-optimize"),
    javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")

  )

  lazy val doNotPublishSettings = Seq(publish := {}, publishLocal := {})

  //
  // Packaging to SonaType using SBT
  //
  // https://github.com/sbt/sbt.github.com/blob/gen-master/src/jekyll/using_sonatype.md
  // http://www.cakesolutions.net/teamblogs/2012/01/28/publishing-sbt-projects-to-nexus/
  // https://docs.sonatype.org/display/Repository/How+To+Generate+PGP+Signatures+With+Maven
  //    

  //
  // Projects
  //
  lazy val root = Project(id = "broadcastx",
    base = file("."),
    settings = defaultSettings) aggregate(server, examples)

  lazy val server = Project(id = "broadcastx-mqtt",
    base = file("broadcastx-mqtt"),
    settings = defaultSettings ++ Seq(
      libraryDependencies ++= Dependencies.server,
      publishMavenStyle := true,
      publishArtifact in Test := false,
      pomIncludeRepository := {
        x => false
      },
      prepareRun <<= (packageBin in Compile, dependencyClasspath in Compile) map prepareRunFiles
    ))

  lazy val examples = Project(id = "broadcastx-examples",
    base = file("broadcastx-examples"),
    //dependencies = Seq(server),
    settings = defaultSettings ++ doNotPublishSettings ++ Seq(
      libraryDependencies ++= Dependencies.examples
    ))
}

//
// Dependencies
//
object Dependencies {

  val server = Seq(
    Dependency.vertxCore,
    Dependency.vertxJava,
    Dependency.logback,
    Dependency.scalatest,
    Dependency.mqttClient
  )

  val examples = Seq(
    Dependency.logback,
    Dependency.mqttClient
  )
}

object Dependency {
  val vertxVersion = "1.3.1.final"
  val vertxCore = "org.vert-x" % "vertx-core" % vertxVersion
  val vertxJava = "org.vert-x" % "vertx-lang-java" % vertxVersion
  val vertxPlatform = "org.vert-x" % "vertx-platform" % vertxVersion

  val logback = "ch.qos.logback" % "logback-classic" % "1.0.3"
  val scalatest = "org.scalatest" %% "scalatest" % "2.0.M2" % "test"
  val mqttClient = "org.fusesource.mqtt-client" % "mqtt-client" % "1.4" % "test"
}
