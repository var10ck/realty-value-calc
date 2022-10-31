package dto.realty
import dao.entities.auth.UserId
import dao.entities.realty.{RealtyObject, RealtyObjectId}
import zio.json.JsonCodec

case class RealtyObjectCreatedDTO(
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
    createdAt: java.time.LocalDateTime
)

object RealtyObjectCreatedDTO {
    implicit val codec: JsonCodec[RealtyObjectCreatedDTO] = zio.json.DeriveJsonCodec.gen[RealtyObjectCreatedDTO]

    def fromEntity(entity: RealtyObject): RealtyObjectCreatedDTO = {
        RealtyObjectCreatedDTO(
          entity.id,
          entity.location,
          entity.roomsNumber,
          entity.segment,
          entity.floorCount,
          entity.wallMaterial,
          entity.floorNumber,
          entity.totalArea,
          entity.kitchenArea,
          entity.gotBalcony,
          entity.condition,
          entity.distanceFromMetro,
          entity.createdAt
        )
    }
}
