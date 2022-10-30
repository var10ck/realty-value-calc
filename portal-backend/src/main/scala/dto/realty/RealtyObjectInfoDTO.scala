package dto.realty
import dao.entities.realty.RealtyObjectId
import zio.json.{JsonCodec, DeriveJsonCodec}

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
    gotBalcony: String,
    condition: String,
    distanceFromMetro: Int,
    calculatedValue: Option[Long],
    createdAt: java.time.LocalDateTime,
    updatedAt: java.time.LocalDateTime
)

object RealtyObjectInfoDTO{
    implicit val codec: JsonCodec[RealtyObjectInfoDTO] = DeriveJsonCodec.gen[RealtyObjectInfoDTO]
}