import sbt._

object Versions {
  val pureconfigVersion = "0.12.1"
  val slf4jVersion = "1.7.29"
  val logbackVersion = "1.2.3"
  val scalaLoggingVersion = "3.9.2"
}

object Dependencies {
  import Versions._
  val pureconfig = "com.github.pureconfig" %% "pureconfig" % pureconfigVersion
  val slf4jApi = "org.slf4j" % "slf4j-api" % slf4jVersion // log API
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion // log IMPL
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion // lazy logging
}
