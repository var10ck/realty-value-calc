package services
import dao.entities.auth.UserId
import dao.entities.realty.RealtyObjectPoolId
import dao.repositories.analytics.AnalyticsRepository
import dto.analytics.DefaultAnalyticsDTO
import zio.{ULayer, ZIO, ZLayer}

import java.sql.SQLException
import javax.sql.DataSource

final case class AnalyticsServiceLive() extends AnalyticsService {

    def getDefaultAnalytics(userId: UserId): ZIO[DataSource with AnalyticsRepository, SQLException, DefaultAnalyticsDTO] =
        for {
            countAllRealtyObjects <- AnalyticsRepository.countAllRealtyObjects
            countAllRealtyObjectsOfUser <- AnalyticsRepository.countAllRealtyObjectsOfUser(userId)
            sumAllAreaOfAllObjects <- AnalyticsRepository.sumAllAreaOfAllObjects
            sumAllAreaOfUsersObjects <- AnalyticsRepository.sumAllAreaOfUsersObjects(userId)
            avgPriceByRoomsNumber <- AnalyticsRepository.avgPriceOfRealtyByRoomsNumber
        } yield DefaultAnalyticsDTO(
          countAllRealtyObjects,
          countAllRealtyObjectsOfUser,
          sumAllAreaOfAllObjects,
          sumAllAreaOfUsersObjects,
          avgPriceByRoomsNumber.toMap)

    def getAvgPriceOfRealtyByRoomsNumberForPool(poolId: RealtyObjectPoolId): ZIO[DataSource with AnalyticsRepository, SQLException, Map[Int, BigDecimal]] =
        AnalyticsRepository.avgPriceOfRealtyByRoomsNumberForPool(poolId).map(_.toMap)

}

object AnalyticsServiceLive{
    val layer: ULayer[AnalyticsService] = ZLayer.succeed(AnalyticsServiceLive())
}

