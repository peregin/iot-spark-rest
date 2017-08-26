package iot.data


import iot.data.util.GeoLoc
import util.Trigo._

case class VehicleData(
                        vehicleId: String, // identifies the vehicle
                        eventId: String, // unique per event
                        eventTime: String, // ISO 8601
                        lat: Float,
                        long: Float,
                        elevation: Double, // in meters
                        eventType: String,  // e.g sample, idle, etc
                        speed: Option[Float], // measured from the previous point, in km/h
                        distance: Option[Double], // measured from the previous point, in km
                        acceleration: Option[Float] // from the previous point in m/s2
) {

  def distanceTo(that: VehicleData): Double = {
    val here = GeoLoc(lat, long)
    val d = here.haversineDistanceTo(GeoLoc(that.lat, that.long)) // km
    val h = (elevation - that.elevation) / 1000 // km
    pythagoras(d, h)
  }
}