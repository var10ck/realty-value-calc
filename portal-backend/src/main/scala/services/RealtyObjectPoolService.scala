package services
import dao.entities.auth.UserId
import dao.entities.realty.RealtyObjectPool
import dao.repositories.realty.RealtyObjectPoolRepository
import dto.realty.PoolInfoDTO
import zio.{ULayer, ZIO}

import java.sql.SQLException
import javax.sql.DataSource

trait RealtyObjectPoolService {

    def create(
        name: Option[String],
        userId: UserId): ZIO[DataSource with RealtyObjectPoolRepository, SQLException, RealtyObjectPool]

    def get(
        poolId: String,
        userId: UserId): ZIO[DataSource with RealtyObjectPoolRepository, Throwable, RealtyObjectPool]

    def getAllOfUser(userId: UserId): ZIO[DataSource with RealtyObjectPoolRepository, Throwable, List[PoolInfoDTO]]

    def delete(poolId: String, userId: UserId): ZIO[DataSource with RealtyObjectPoolRepository, Throwable, Unit]
}

object RealtyObjectPoolService {
    def create(name: Option[String], userId: UserId)
        : ZIO[DataSource with RealtyObjectPoolRepository with RealtyObjectPoolService, SQLException, RealtyObjectPool] =
        ZIO.serviceWithZIO[RealtyObjectPoolService](_.create(name, userId))

    def get(poolId: String, userId: UserId)
        : ZIO[DataSource with RealtyObjectPoolRepository with RealtyObjectPoolService, Throwable, RealtyObjectPool] =
        ZIO.serviceWithZIO[RealtyObjectPoolService](_.get(poolId, userId))

    def getAllOfUser(userId: UserId): ZIO[
      DataSource with RealtyObjectPoolRepository with RealtyObjectPoolService,
      Throwable,
      List[PoolInfoDTO]] = ZIO.serviceWithZIO[RealtyObjectPoolService](_.getAllOfUser(userId))

    def delete(
        poolId: String,
        userId: UserId): ZIO[DataSource with RealtyObjectPoolRepository with RealtyObjectPoolService, Throwable, Unit] =
        ZIO.serviceWithZIO[RealtyObjectPoolService](_.delete(poolId, userId))

    val live: ULayer[RealtyObjectPoolService] = RealtyObjectPoolServiceLive.layer
}
