package iot.spark

import iot.data.VehicleData
import iot.data.util.JsonIO
import kafka.serializer.Decoder
import kafka.utils.VerifiableProperties

import scala.util.Try

class VehicleDecoder(verifiableProperties: VerifiableProperties) extends Decoder[VehicleData] {

  override def fromBytes(bytes: Array[Byte]) = {
    Try( JsonIO.read[VehicleData](new String(bytes)) ).getOrElse(null)
  }
}
