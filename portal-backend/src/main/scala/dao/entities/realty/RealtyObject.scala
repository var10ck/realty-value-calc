package dao.entities.realty
import dao.entities.auth.UserId
import zio.ZIO
import zio.json.{DeriveJsonCodec, JsonCodec}

case class RealtyObject(
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
    addedByUserId: UserId,
    calculatedValue: Option[Long],
    createdAt: java.time.LocalDateTime,
    updatedAt: java.time.LocalDateTime,
    poolId: RealtyObjectPoolId
)

object RealtyObject {

    /** Uses the `random` method defined on our RealtyObjectId wrapper to generate a random ID and assign that to the
      * RealtyObject we are creating.
      */
    def make(
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
        addedByUserId: UserId,
        calculatedValue: Option[Long] = None,
        createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
        updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),
        poolId: RealtyObjectPoolId): ZIO[Any, Nothing, RealtyObject] =
        RealtyObjectId.random.map(
          RealtyObject(
            _,
            location,
            roomsNumber,
            segment,
            floorCount,
            wallMaterial,
            floorNumber,
            totalArea,
            kitchenArea,
            gotBalcony,
            condition,
            distanceFromMetro,
            addedByUserId,
            calculatedValue,
            createdAt,
            updatedAt,
            poolId
          ))

    /** JSON codec */
    implicit val codec: JsonCodec[RealtyObject] = DeriveJsonCodec.gen[RealtyObject]
}
