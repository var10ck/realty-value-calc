package helpers
import java.text.{DecimalFormat, NumberFormat}
import java.util.Locale

object GeoHelper {

    case class Point(lat: Double, lon: Double) {
        def latStr: String = strRep(lat)
        def lonStr: String = strRep(lon)
        override def toString: String = s"${latStr}_$lonStr"

        def strRep(double: Double): String =
            new DecimalFormat("#.000000").format(double).replace(",", ".")
    }

    case class GeoPolygon(bottomLeft: Point, topLeft: Point, bottomRight: Point, topRight: Point)

    /** Create 4 points (pair of lat, lon) around 1
      * @param r
      *   \- radius in meters
      */
    def makeSquareAroundLocation(lat: String, lon: String, r: Int): GeoPolygon = {
        val dist: Double = r / 1.36 / 1000000
        val bottomLeft = Point(lat.toDouble - dist, lon.toDouble - dist)
        val bottomRight = Point(lat.toDouble + dist, lon.toDouble - dist)
        val topLeft = Point(lat.toDouble - dist, lon.toDouble + dist)
        val topRight = Point(lat.toDouble + dist, lon.toDouble + dist)
        GeoPolygon(bottomLeft, topLeft, bottomRight, topRight)
    }

}
