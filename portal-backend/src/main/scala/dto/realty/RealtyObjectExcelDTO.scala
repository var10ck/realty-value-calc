package dto.realty
import zio.json.JsonCodec

case class RealtyObjectExcelDTO(
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
    distanceFromMetro: Int
)

object RealtyObjectExcelDTO {
    implicit val codec: JsonCodec[RealtyObjectExcelDTO] = zio.json.DeriveJsonCodec.gen[RealtyObjectExcelDTO]
}
