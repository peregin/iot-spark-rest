package iot.data

import java.io.{File, PrintWriter}
import java.util.UUID

import com.fasterxml.jackson.databind.util.ISO8601Utils
import iot.data.util.JsonIO

import scala.annotation.tailrec
import scala.xml.XML

/**
  * Generates json in the Vechicle IOT format.
  * Sample data is converted from Strava cycling activities.
  * The sample data is in GPX and it is used to validate the correctness of the Spark transformations.
  */
object GenerateDataApp extends App {

  val millisToHours = 1000 * 60 * 60

  val dir = new File("iot-data/src/test/resources/gpx")

  val gpxFiles = dir.listFiles().filter(_.getName.endsWith(".gpx"))
  val kafkaInput = gpxFiles.flatMap(convert).map(JsonIO.write)
  println(s"generated ${kafkaInput.size} entries")

  val pw = new PrintWriter(new File("kafka-producer/src/main/resources/kafka/input.json"))
  kafkaInput.foreach(pw.println)
  pw.close()

  println("done")


  def convert(file: File): Seq[VehicleData] = {
    val fileName = file.getName.stripSuffix(".gpx")
    println(s"converting $fileName")

    val elem = XML.loadFile(file)
    val name = (elem \ "trk" \ "name").text
    println(s"\t$name")
    val coordinates = (elem \ "trk" \ "trkseg" \ "trkpt").map{ node =>
      val lat = (node \ "@lat").text.toFloat
      val lon = (node \ "@lon").text.toFloat
      val time = (node \ "time").text
      val elevation = (node \ "ele").text.toDouble
      VehicleData(
        s"vehicleId-$fileName",
        UUID.randomUUID().toString,
        time,
        lat, lon, elevation,
        "sample",
        speed = None,
        distance = None,
        acceleration = None
      )
    }
    enrich(coordinates)
  }

  def enrich(list: Seq[VehicleData]): Seq[VehicleData] = {
    enrich(Seq.empty, list)
  }

  @tailrec
  def enrich(accu: Seq[VehicleData], remainder: Seq[VehicleData]): Seq[VehicleData] = {
    if (remainder.isEmpty) accu
    else if (accu.isEmpty) {
      enrich(Seq(remainder.head), remainder.tail)
    } else {
      val last = accu.last
      val first = remainder.head
      val distance = last.distanceTo(first).toFloat

      val timeFrom = ISO8601Utils.parse(last.eventTime).getTime
      val timeTo = ISO8601Utils.parse(first.eventTime).getTime
      val time = (timeTo - timeFrom).toFloat / millisToHours
      val speedInKmPerHour = if (time > 0) Some(distance / time) else None
      val accelerationInMeterPerSecond = speedInKmPerHour.map(s => ( (s - last.speed.getOrElse(0f)) * .277f ) / (time * 1000))
      val entry = first.copy(distance = Some(distance), speed = speedInKmPerHour, acceleration = accelerationInMeterPerSecond)
      enrich(accu :+ entry, remainder.tail)
    }
  }
}
