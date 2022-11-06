package dto.realty
import dao.entities.realty.{RealtyObject, RealtyObjectId, RealtyObjectPool, RealtyObjectPoolId}
import zio.json.{DeriveJsonCodec, JsonCodec}

case class RealtyObjectInfoDTO(
    id: RealtyObjectId,
    location: String,
    roomsNumber: Int,
    segment: String,
    floorCount: Int,
    wallMaterial: String,
    floorNumber: Int,
    totalArea: Double,
    kitchenArea: Double,
    gotBalcony: Boolean,
    condition: String,
    distanceFromMetro: Int,
    calculatedValue: Option[Long],
    createdAt: java.time.LocalDateTime,
    updatedAt: java.time.LocalDateTime,
    poolId: RealtyObjectPoolId,
    coordinates: CoordinatesDTO,
    analogs: Option[List[AnalogueObjectInfoDTO]] = None
)

case class CoordinatesDTO(lat: String, lon: String)

object RealtyObjectInfoDTO {
    implicit val codec: JsonCodec[RealtyObjectInfoDTO] = DeriveJsonCodec.gen[RealtyObjectInfoDTO]

    def fromEntity(realtyObject: RealtyObject): RealtyObjectInfoDTO =
        RealtyObjectInfoDTO(
          realtyObject.id,
          realtyObject.location,
          realtyObject.roomsNumber,
          realtyObject.segment,
          realtyObject.floorCount,
          realtyObject.wallMaterial,
          realtyObject.floorNumber,
          realtyObject.totalArea,
          realtyObject.kitchenArea,
          realtyObject.gotBalcony,
          realtyObject.condition,
          realtyObject.distanceFromMetro,
          realtyObject.calculatedValue,
          realtyObject.createdAt,
          realtyObject.updatedAt,
          realtyObject.poolId,
          CoordinatesDTO(realtyObject.latitude.getOrElse(""), realtyObject.longitude.getOrElse(""))
        )

    def fromEntityWithAnalogs(realtyObject: RealtyObject, analogs: List[AnalogueObjectInfoDTO]): RealtyObjectInfoDTO =
        fromEntity(realtyObject).copy(analogs = Some(analogs))
}

object CoordinatesDTO {
    implicit val codec: JsonCodec[CoordinatesDTO] = DeriveJsonCodec.gen[CoordinatesDTO]
}
