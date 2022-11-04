package services
import dao.entities.auth.UserId
import dao.entities.realty.RealtyObjectPool
import dao.repositories.realty.RealtyObjectPoolRepository
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

    def delete(poolId: String, userId: UserId): ZIO[DataSource with RealtyObjectPoolRepository, Throwable, Unit]
}

object RealtyObjectPoolService {
    def create(name: Option[String], userId: UserId)
        : ZIO[DataSource with RealtyObjectPoolRepository with RealtyObjectPoolService, SQLException, RealtyObjectPool] =
        ZIO.serviceWithZIO[RealtyObjectPoolService](_.create(name, userId))

    def get(poolId: String, userId: UserId)
        : ZIO[DataSource with RealtyObjectPoolRepository with RealtyObjectPoolService, Throwable, RealtyObjectPool] =
        ZIO.serviceWithZIO[RealtyObjectPoolService](_.get(poolId, userId))

    def delete(
        poolId: String,
        userId: UserId): ZIO[DataSource with RealtyObjectPoolRepository with RealtyObjectPoolService, Throwable, Unit] =
        ZIO.serviceWithZIO[RealtyObjectPoolService](_.delete(poolId, userId))

    val live: ULayer[RealtyObjectPoolService] = RealtyObjectPoolServiceLive.layer
}
