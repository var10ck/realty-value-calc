package dto.realty
import dao.entities.realty.RealtyObjectPoolId
import zio.json.JsonCodec

case class UpdateRealtyObjectDTO(
    id: String,
    location: Option[String],
    roomsNumber: Option[Int],
    segment: Option[String],
    floorCount: Option[Int],
    wallMaterial: Option[String],
    floorNumber: Option[Int],
    totalArea: Option[Double],
    kitchenArea: Option[Double],
    gotBalcony: Option[Boolean],
    condition: Option[String],
    distanceFromMetro: Option[Int],
    calculatedValue: Option[Long],
    poolId: Option[RealtyObjectPoolId],
    latitude: Option[String],
    longitude: Option[String]
)

object UpdateRealtyObjectDTO {
    implicit val codec: JsonCodec[UpdateRealtyObjectDTO] = zio.json.DeriveJsonCodec.gen[UpdateRealtyObjectDTO]
}
