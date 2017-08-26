package iot.data.util

import iot.data.util.Trigo.square

import scala.math._


object Trigo {

  // Distances from points on the surface to the center range from 6353 km to 6384 km.
  // Several different ways of modeling the Earth as a sphere each yield a mean radius of 6371 kilometers.
  val earthRadius = 6371d

  def square(v: Double) = v * v

  def pythagoras(a: Double, b: Double): Double = sqrt(square(a) + square(b))
  def leg(c: Double, a: Double): Double = sqrt(square(c) - square(a))

  def polarX(cx: Double, r: Double, angle: Double): Int = (cx + r * math.cos(math.toRadians(angle))).toInt
  def polarY(cy: Double, r: Double, angle: Double): Int = (cy + r * math.sin(math.toRadians(angle))).toInt
}

case class GeoLoc(lat: Float, long: Float) {

  // The return value is the distance expressed in kilometers.
  // It uses the haversine formula.
  // The accuracy of the distance is decreasing if:
  // - the points are distant
  // - points are closer to the geographic pole
  def haversineDistanceTo(that: GeoLoc): Double = {
    val deltaPhi = (lat - that.lat).toRadians
    val deltaLambda = (long - that.long).toRadians
    val a = square(sin(deltaPhi / 2)) +
      cos(that.lat.toRadians) * cos(lat.toRadians) * sin(deltaLambda / 2) * sin(deltaLambda / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    Trigo.earthRadius * c
  }
}
