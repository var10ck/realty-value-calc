package dto.realty
import zio.json.JsonCodec

case class CreateRealtyObjectDTO (location: String,
                                  roomsNumber: Int,
                                  segment: String,
                                  floorCount: Int,
                                  wallMaterial: String,
                                  floorNumber: Int,
                                  totalArea: Double,
                                  kitchenArea: Double,
                                  gotBalcony: Boolean,
                                  condition: String,
                                  distanceFromMetro: Int)

object CreateRealtyObjectDTO{
    implicit val codec: JsonCodec[CreateRealtyObjectDTO] = zio.json.DeriveJsonCodec.gen[CreateRealtyObjectDTO]
}
