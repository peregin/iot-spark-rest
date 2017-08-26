package iot.data

import iot.data.util.JsonIO
import org.specs2.mutable.Specification

class VehicleDataSpec extends Specification {

  "model" should {

    val model = VehicleData(
      s"vehicleId",
      s"eventId",
      eventTime = "2017-06-12T09:05:56Z",
      1f, 2f, 500d,
      "sample",
      speed = None,
      distance = None,
      acceleration = Some(9.8f)
    )

    "write to json" in {
      val json = JsonIO.write(model)
      json === "{\"vehicleId\":\"vehicleId\",\"eventId\":\"eventId\",\"eventTime\":\"2017-06-12T09:05:56Z\",\"lat\":1.0,\"long\":2.0,\"elevation\":500.0,\"eventType\":\"sample\",\"acceleration\":9.800000190734863}"
    }

    "write and read" in {
      val json = JsonIO.write(model)
      val model2 = JsonIO.read[VehicleData](json)
      model === model2
    }
  }
}
