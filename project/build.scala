import sbt._
import Keys._


object dependencies {

  val sparkVersion = "2.2.0"
  val logbackVersion = "1.2.3"
  val scalatraVersion = "2.3.1"
  val specsVersion = "3.7"
  val json4sVersion = "3.2.11"

  val logback = "ch.qos.logback" % "logback-classic" % logbackVersion

  val sparkCore = "org.apache.spark" %% "spark-core" % sparkVersion
  val sparkStreaming = "org.apache.spark" %% "spark-streaming" % sparkVersion
  val sparkSQL = "org.apache.spark" %% "spark-sql" % sparkVersion
  val sparkKafka = "org.apache.spark" % "spark-streaming-kafka-0-8_2.11" % "2.1.0"

  val httpLib = "net.databinder.dispatch" %% "dispatch-core" % "0.13.1"

  val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"
  val scalaSpec = "org.specs2" %% "specs2" % specsVersion % "test"
  val kafkaClient = "org.apache.kafka" % "kafka-clients" % "0.10.0.0"
  val kafkaCore = "org.apache.kafka" % "kafka_2.11" % "0.10.0.0"

  def spark = Seq(sparkCore, sparkStreaming, sparkSQL, sparkKafka)
  def kafka = Seq(kafkaClient, kafkaCore)
  def logging = Seq(logback)
  def json = Seq(
    "org.json4s" %% "json4s-core" % json4sVersion,
    "org.json4s" %% "json4s-jackson" % json4sVersion
  )
  def scalatra = Seq(
    "org.scalatra" %% "scalatra" % scalatraVersion,
    "org.scalatra" %% "scalatra-json" % scalatraVersion,
    "org.eclipse.jetty" % "jetty-webapp" % "9.2.19.v20160908",
    "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
  )
  def testing = Seq(scalaCheck, scalaSpec)
}

object sbuild extends Build {

  lazy val buildSettings = Defaults.coreDefaultSettings ++ Seq (
    version <<= version in ThisBuild,
    scalaVersion := "2.11.11",
    organization := "iot.demo",
    description := "Spark IOT",
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    scalacOptions := Seq("-target:jvm-1.8", "-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
    resolvers ++= Seq(
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
    )
  )

  lazy val iotData = Project(
    id = "iot-data",
    base = file("iot-data"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= dependencies.json ++ dependencies.testing

    )
  )

  lazy val sparkProcessor = Project(
    id = "spark-processor",
    base = file("spark-processor"),
    dependencies = Seq(iotData),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= dependencies.spark ++ Seq(dependencies.httpLib) ++ dependencies.testing

    )
  )

  lazy val kafkaProducer = Project(
    id = "kafka-producer",
    base = file("kafka-producer"),
    dependencies = Seq(iotData),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= dependencies.kafka ++ dependencies.logging
    )
  )

  lazy val restService = Project(
    id = "rest-service",
    base = file("rest-service"),
    dependencies = Seq(iotData),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= dependencies.scalatra ++ dependencies.logging
    )
  )

  // top level aggregate
  lazy val root = Project(
    id = "iot-spark-rest",
    base = file("."),
    settings = buildSettings,
    aggregate = Seq(iotData, sparkProcessor, kafkaProducer, restService)
  )
}
