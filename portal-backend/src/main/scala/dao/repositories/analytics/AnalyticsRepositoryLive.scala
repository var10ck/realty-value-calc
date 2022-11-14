package dao.repositories.analytics
import dao.entities.auth.UserId
import dao.entities.realty.{RealtyObject, RealtyObjectPoolId}
import io.getquill.context.ZioJdbc.QIO
import zio.{ULayer, ZLayer}

final case class AnalyticsRepositoryLive() extends AnalyticsRepository {

    import db.Ctx._

    override def countAllRealtyObjects: QIO[Long] = run(query[RealtyObject].size)

    override def countAllRealtyObjectsOfUser(userId: UserId): QIO[Long] = run(
      query[RealtyObject].filter(_.addedByUserId == lift(userId)).size)

    override def sumAllAreaOfAllObjects: QIO[Double] = run(quote(query[RealtyObject].map(_.totalArea).sum)).map(_.getOrElse(0))

    override def sumAllAreaOfUsersObjects(userId: UserId): QIO[Double] = run(
      quote(query[RealtyObject].filter(_.addedByUserId == lift(userId)).map(_.totalArea).sum)).map(_.getOrElse(0))

    override def avgPriceOfRealtyByRoomsNumber: QIO[List[(Int, BigDecimal)]] = run(
      quote(
        query[RealtyObject]
            .filter(_.calculatedValue.isDefined)
            .groupByMap(_.roomsNumber)(o => (o.roomsNumber, avg(o.calculatedValue.getOrElse(0L)))))
    )

    override def avgPriceOfRealtyByRoomsNumberForPool(poolId: RealtyObjectPoolId): QIO[List[(Int, BigDecimal)]] = run(
      quote(
        query[RealtyObject]
            .filter(o => o.poolId == lift(poolId) && o.calculatedValue.isDefined)
            .groupByMap(_.roomsNumber)(o => (o.roomsNumber, avg(o.calculatedValue.getOrElse(0L)))))
    )
}

object AnalyticsRepositoryLive {
    val layer: ULayer[AnalyticsRepository] = ZLayer.succeed(AnalyticsRepositoryLive())
}
