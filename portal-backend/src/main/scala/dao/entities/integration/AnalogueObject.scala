package dao.entities.integration
import dao.entities.realty.RealtyObjectId
import dto.integration.cian.Apartment
import zio.ZIO
import zio.json.JsonCodec

case class AnalogueObject(
    id: AnalogueObjectId,
    url: Option[String],
    latitude: Option[Double],
    longitude: Option[Double],
    price: Option[Int],
    roomsNumber: Option[Int],
    apartmentsType: Option[String],
    material: Option[String],
    floor: Option[Int],
    area: Option[Double],
    kitchenArea: Option[Double],
    balcony: Boolean,
    distanceToMetroInMinutes: Option[Int],
    condition: Option[String],
    floors: Option[Int],
    realtyObjectId: RealtyObjectId
)

object AnalogueObject {
    implicit val codec: JsonCodec[AnalogueObjectId] = zio.json.DeriveJsonCodec.gen[AnalogueObjectId]

    def make(
        url: Option[String],
        latitude: Option[Double],
        longitude: Option[Double],
        price: Option[Int],
        roomsNumber: Option[Int],
        apartmentsType: Option[String],
        material: Option[String],
        floor: Option[Int],
        area: Option[Double],
        kitchenArea: Option[Double],
        balcony: Boolean,
        distanceToMetroInMinutes: Option[Int],
        condition: Option[String],
        floors: Option[Int],
        realtyObjectId: RealtyObjectId): ZIO[Any, Nothing, AnalogueObject] = AnalogueObjectId.random.map(
      AnalogueObject(
        _,
        url,
        latitude,
        longitude,
        price,
        roomsNumber,
        apartmentsType,
        material,
        floor,
        area,
        kitchenArea,
        balcony,
        distanceToMetroInMinutes,
        condition,
        floors,
        realtyObjectId
      ))

    def fromApartment(apartment: Apartment, realtyObjectId: RealtyObjectId): ZIO[Any, Nothing, AnalogueObject] =
        for {
            newId <- AnalogueObjectId.random
        } yield AnalogueObject(
          id = newId,
          url = apartment.URL,
          latitude = apartment.location.map(_.lat),
          longitude = apartment.location.map(_.lng),
          price = apartment.price,
          roomsNumber = apartment.roomsNumber,
          apartmentsType = apartment.apartmentsType,
          material = apartment.material,
          floor = apartment.floor,
          area = apartment.area,
          kitchenArea = apartment.kitchenArea,
          balcony = apartment.balcony,
          distanceToMetroInMinutes = apartment.distanceToMetro.minBy(_.minutes).minutes,
          condition = apartment.condition,
          floors = apartment.floor,
          realtyObjectId = realtyObjectId
        )
}
