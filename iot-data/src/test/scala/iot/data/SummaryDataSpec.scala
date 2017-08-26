package iot.data

import iot.data.util.JsonIO
import org.specs2.mutable.Specification

class SummaryDataSpec extends Specification {

  "model" should {

    val model = SummaryData(
      s"vehicleId",
      startTime = "2017-06-12T09:05:56Z",
      endTime = "2017-06-12T10:05:56Z",
      hardBreaks = 2,
      speeding = Seq(Speeding("t1", 55)),
      Some(10d)
    )

    "write to json" in {
      val json = JsonIO.write(model)
      json === "{\"vechicleId\":\"vehicleId\",\"startTime\":\"2017-06-12T09:05:56Z\",\"endTime\":\"2017-06-12T10:05:56Z\",\"hardBreaks\":2,\"speeding\":[{\"time\":\"t1\",\"speed\":55.0}],\"distance\":10.0}"
    }

    "write and read" in {
      val json = JsonIO.write(model)
      val model2 = JsonIO.read[SummaryData](json)
      model === model2
    }
  }
}
