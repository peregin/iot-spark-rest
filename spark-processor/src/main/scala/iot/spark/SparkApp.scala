package iot.spark

import java.nio.charset.Charset

import iot.data.util.JsonIO
import iot.data.{Speeding, SummaryData, VehicleData}
import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka._
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.util.Try

object SparkApp extends App {

  val sparkMaster = sys.props.getOrElse("iot.spark.master", "local[*]")
  val conf = new SparkConf()
    .setAppName("IOT")
    .setMaster(sparkMaster)
    .set("spark.driver.bindAddress", "localhost")
    .set("spark.driver.host", "localhost")

  val ssc = new StreamingContext(conf, Seconds(5))

  val topicName = sys.props.getOrElse("iot.kafka.topic", "iot-spark")
  val brokerHost = sys.props.getOrElse("iot.kafka.host", "localhost:9092")
  val restEndpoint = sys.props.getOrElse("iot.rest.endpoint", "http://localhost:8888/summary")

  val hardBreakForceLimit = sys.props.getOrElse("iot.hardbreak.threshold", "-2.45").toFloat  // 1/4 of G force
  val speedingThreshold = sys.props.getOrElse("iot.speeding.threshold", "52").toFloat // because it's bicycle data

  val kafkaParams = Map[String, String]("metadata.broker.list" -> brokerHost)
  val kafkaStream = KafkaUtils.createDirectStream[String, VehicleData, StringDecoder, VehicleDecoder](ssc, kafkaParams, Set(topicName))


  val iotStream = kafkaStream.map(_._2)  //.filter(_.eventType == "location")
  val vehicleStream = iotStream.map(e => (e.vehicleId, e)).groupByKey()
  val activities = vehicleStream.map(r => extractSummary(r._2))
  activities.foreachRDD{ rdd =>
    rdd.collect().foreach{summary =>
      Try( sendToRestService(summary) ).recover{ case f => f.printStackTrace() }
    }
  }

  ssc.start()
  ssc.awaitTermination()


  def extractSummary(it: Iterable[VehicleData]): SummaryData = {
    val sorted = it.toSeq.sortBy(_.eventTime)
    val first = sorted.head
    val last = sorted.last
    val hardBreaks = sorted.flatMap(_.acceleration).filter(_ < hardBreakForceLimit).size
    val speeding = sorted.filter(_.speed.exists(_ > speedingThreshold)).map(in => Speeding(in.eventTime, in.speed.getOrElse(0)))
    val distanceCovered = if (sorted.isEmpty) None else Some(sorted.flatMap(_.distance).sum)
    SummaryData(
      first.vehicleId,
      first.eventTime, last.eventTime,
      hardBreaks,
      speeding,
      distanceCovered
    )
  }

  def sendToRestService(out: SummaryData) {
    println(s"posting $out")
    val json = JsonIO.write(out)
    import dispatch._
    import Defaults._
    val req = url(restEndpoint).setContentType("application/json", Charset.forName("UTF-8")).setMethod("POST") << json
    Http.default(req)
  }
}
