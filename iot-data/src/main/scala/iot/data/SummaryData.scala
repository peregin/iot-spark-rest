package iot.data

case class Speeding(time: String, speed: Float)

case class SummaryData(
                        vechicleId: String,
                        startTime: String, // ISO 8601
                        endTime: String, // ISO 8601
                        hardBreaks: Long,
                        speeding: Seq[Speeding],
                        distance: Option[Double]
)