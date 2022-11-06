package dto.realty
import dao.entities.integration.AnalogueObject
import zio.json.JsonCodec

case class AnalogueObjectInfoDTO(
    url: Option[String],
    latitude: Option[Double],
    longitude: Option[Double],
    price: Option[Int],
    roomsNumber: Option[Int],
    area: Option[Double],
    kitchenArea: Option[Double],
    distanceToMetroInMinutes: Option[Int]
)

object AnalogueObjectInfoDTO {
    implicit val codec: JsonCodec[AnalogueObjectInfoDTO] = zio.json.DeriveJsonCodec.gen[AnalogueObjectInfoDTO]

    def fromEntity(analogueObject: AnalogueObject): AnalogueObjectInfoDTO =
        AnalogueObjectInfoDTO(
          url = analogueObject.url,
          latitude = analogueObject.latitude, longitude = analogueObject.longitude,
          price = analogueObject.price,
          roomsNumber = analogueObject.roomsNumber,
          area = analogueObject.area,
          kitchenArea = analogueObject.kitchenArea,
          distanceToMetroInMinutes = analogueObject.distanceToMetroInMinutes
        )
}
