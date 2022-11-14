package dao.repositories.analytics
import dao.entities.auth.UserId
import dao.entities.realty.RealtyObjectPoolId
import io.getquill.context.ZioJdbc.QIO
import zio.{ULayer, ZIO}

import java.sql.SQLException
import javax.sql.DataSource

trait AnalyticsRepository {

    def countAllRealtyObjects: QIO[Long]

    def countAllRealtyObjectsOfUser(userId: UserId): QIO[Long]

    def sumAllAreaOfAllObjects: QIO[Double]

    def sumAllAreaOfUsersObjects(userId: UserId): QIO[Double]

    def avgPriceOfRealtyByRoomsNumber: QIO[List[(Int, BigDecimal)]]

    def avgPriceOfRealtyByRoomsNumberForPool(poolId: RealtyObjectPoolId): QIO[List[(Int, BigDecimal)]]

}

object AnalyticsRepository {

    def countAllRealtyObjects: ZIO[DataSource with AnalyticsRepository, SQLException, Long] =
        ZIO.serviceWithZIO[AnalyticsRepository](_.countAllRealtyObjects)

    def countAllRealtyObjectsOfUser(userId: UserId): ZIO[DataSource with AnalyticsRepository, SQLException, Long] =
        ZIO.serviceWithZIO[AnalyticsRepository](_.countAllRealtyObjectsOfUser(userId))

    def sumAllAreaOfAllObjects: ZIO[DataSource with AnalyticsRepository, SQLException, Double] =
        ZIO.serviceWithZIO[AnalyticsRepository](_.sumAllAreaOfAllObjects)

    def sumAllAreaOfUsersObjects(userId: UserId): ZIO[DataSource with AnalyticsRepository, SQLException, Double] =
        ZIO.serviceWithZIO[AnalyticsRepository](_.sumAllAreaOfUsersObjects(userId))

    def avgPriceOfRealtyByRoomsNumber: ZIO[DataSource with AnalyticsRepository, SQLException, List[(Int, BigDecimal)]] =
        ZIO.serviceWithZIO[AnalyticsRepository](_.avgPriceOfRealtyByRoomsNumber)

    def avgPriceOfRealtyByRoomsNumberForPool(
        poolId: RealtyObjectPoolId): ZIO[DataSource with AnalyticsRepository, SQLException, List[(Int, BigDecimal)]] =
        ZIO.serviceWithZIO[AnalyticsRepository](_.avgPriceOfRealtyByRoomsNumberForPool(poolId))

    lazy val live: ULayer[AnalyticsRepository] = AnalyticsRepositoryLive.layer
}
