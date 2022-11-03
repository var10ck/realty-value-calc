package dto.integration.cian
import zio.json.{jsonField, jsonHint, JsonCodec}

/*
 * {
  "jsonQuery": {
    "_type": "flatsale",
    "engine_version": {
      "type": "term",
      "value": 2
    },
    "geo": {
      "type": "geo",
      "value": [
        {
          "type": "polygon",
          "name": "район",
          "coordinates": [
            [
              "37.393721",
              "55.848751"
            ],
            [
              "37.393721",
              "55.879712"
            ],
            [
              "37.462577",
              "55.879712"
            ],
            [
              "37.462577",
              "55.848751"
            ],
            [
              "37.393721",
              "55.848751"
            ]
          ]
        }
      ]
    },
    "room": {
      "type": "terms",
      "value": [
        1
      ]
    },
    "total_area": {
      "type": "range",
      "value": {
        "gte": 10,
        "lte": 100
      }
    },
    "balconies": {
      "type": "range",
      "value": {
        "gte": 1
      }
    },
    "repair": {
      "type": "terms",
      "value": [
        2
      ]
    },
    "floor": {
      "type": "range",
      "value": {
        "gte": 10,
        "lte": 20
      }
    }
  }
}
 */

sealed trait CianJsonQuery

object CianJsonQuery{
    implicit val codec: JsonCodec[CianJsonQuery] = zio.json.DeriveJsonCodec.gen[CianJsonQuery]
}

@jsonHint("jsonQuery") case class CianSearchRequestDTO(
    @jsonField("_type") adType: String = "flatsale",
    @jsonField("engine_version") engineVersion: TermProperty = TermProperty(value = 2),
    geo: Geo,
    rooms: TermsProperty,
    @jsonField("total_area") totalArea: RangeProperty,
    balconies: RangeProperty,
    repair: Option[TermsProperty],
    floor: RangeProperty
) extends CianJsonQuery

object CianSearchRequestDTO {
    implicit val codec: JsonCodec[CianSearchRequestDTO] = zio.json.DeriveJsonCodec.gen[CianSearchRequestDTO]

    def make(
        coordinates: List[(String, String)],
        rooms: List[Int],
        totalAreaGte: Int,
        totalAreaLte: Int,
        balconiesGt: Int,
        floorGte: Int = 1,
        floorLte: Option[Int],
        repair: Option[List[Int]] = None): CianJsonQuery = {
        val transformedCoordinates = coordinates.map(v => List(v._1, v._2))
        val geo = Geo(value = List(GeoValue(name = "район", coordinates = transformedCoordinates)))

        val roomsProp = TermsProperty(value = rooms)

        val totalAreaProp = RangeProperty.make(Some(totalAreaGte), Some(totalAreaLte))

        val balconiesProp = RangeProperty.make(Some(balconiesGt), None)

        val repairProp = repair.map(l => TermsProperty(value = l))

        val floorProp = RangeProperty.make(Some(floorGte), floorLte)

        CianSearchRequestDTO(
          geo = geo,
          rooms = roomsProp,
          totalArea = totalAreaProp,
          balconies = balconiesProp,
          repair = repairProp,
          floor = floorProp
        )
    }
}

case class EngineVersion(
    @jsonField("type") propertyType: String,
    value: Int
)

object EngineVersion {
    implicit val codec: JsonCodec[EngineVersion] = zio.json.DeriveJsonCodec.gen[EngineVersion]
}

case class Geo(
    @jsonField("type") propertyType: String = "geo",
    value: List[GeoValue]
)

object Geo {
    implicit val codec: JsonCodec[Geo] = zio.json.DeriveJsonCodec.gen[Geo]
}

case class GeoValue(
    @jsonField("type") propertyType: String = "polygon",
    name: String,
    coordinates: List[List[String]]
)
object GeoValue {
    implicit val codec: JsonCodec[GeoValue] = zio.json.DeriveJsonCodec.gen[GeoValue]
}

case class TermProperty(
    @jsonField("type") propertyType: String = "term",
    value: Int
)

object TermProperty{
    implicit val codec: JsonCodec[TermProperty] = zio.json.DeriveJsonCodec.gen[TermProperty]
}

case class TermsProperty(
    @jsonField("type") propertyType: String = "terms",
    value: List[Int]
)

object TermsProperty {
    implicit val codec: JsonCodec[TermsProperty] = zio.json.DeriveJsonCodec.gen[TermsProperty]
}

case class RangeProperty(
    @jsonField("type") propertyType: String = "range",
    value: RangePropertyValue
)

object RangeProperty {
    implicit val codec: JsonCodec[RangeProperty] = zio.json.DeriveJsonCodec.gen[RangeProperty]

    def make(gte: Option[Int] = None, lte: Option[Int] = None): RangeProperty =
        RangeProperty(value = RangePropertyValue(gte, lte))
}

case class RangePropertyValue(gte: Option[Int], lte: Option[Int])

object RangePropertyValue {
    implicit val codec: JsonCodec[RangePropertyValue] = zio.json.DeriveJsonCodec.gen[RangePropertyValue]
}
