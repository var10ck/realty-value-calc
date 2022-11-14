package services
import dao.entities.auth.UserId
import dao.entities.realty.RealtyObjectPoolId
import dao.repositories.analytics.AnalyticsRepository
import dto.analytics.DefaultAnalyticsDTO
import zio.{ULayer, ZIO}

import java.sql.SQLException
import javax.sql.DataSource

trait AnalyticsService {

    def getDefaultAnalytics(userId: UserId): ZIO[DataSource with AnalyticsRepository, SQLException, DefaultAnalyticsDTO]

    def getAvgPriceOfRealtyByRoomsNumberForPool(
        poolId: RealtyObjectPoolId): ZIO[DataSource with AnalyticsRepository, SQLException, Map[Int, BigDecimal]]

}

object AnalyticsService {

    def getDefaultAnalytics(userId: UserId)
        : ZIO[DataSource with AnalyticsRepository with AnalyticsService, SQLException, DefaultAnalyticsDTO] =
        ZIO.serviceWithZIO[AnalyticsService](_.getDefaultAnalytics(userId))

    def getAvgPriceOfRealtyByRoomsNumberForPool(poolId: RealtyObjectPoolId)
        : ZIO[DataSource with AnalyticsRepository with AnalyticsService, SQLException, Map[Int, BigDecimal]] =
        ZIO.serviceWithZIO[AnalyticsService](_.getAvgPriceOfRealtyByRoomsNumberForPool(poolId))

    lazy val live: ULayer[AnalyticsService] = AnalyticsServiceLive.layer
}
