package dto.analytics
import zio.json.JsonCodec

case class DefaultAnalyticsDTO(
    countAllRealtyObjects: Long,
    countAllRealtyObjectsOfUser: Long,
    sumAllAreaOfAllObjects: Double,
    sumAllAreaOfUsersObjects: Double,
    avgPriceByRoomsNumber: Map[Int, BigDecimal])

object DefaultAnalyticsDTO {
    implicit val codec: JsonCodec[DefaultAnalyticsDTO] = zio.json.DeriveJsonCodec.gen[DefaultAnalyticsDTO]
}
