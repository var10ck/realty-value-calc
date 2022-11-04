package dao.repositories.realty
import dao.entities.auth.UserId
import dao.entities.realty.{RealtyObjectPool, RealtyObjectPoolId}
import io.getquill.context.ZioJdbc.QIO
import zio.{ULayer, ZIO}

import java.sql.SQLException
import javax.sql.DataSource

trait RealtyObjectPoolRepository {

    def create(name: Option[String] = None, userId: UserId): QIO[RealtyObjectPool]

    def get(poolId: RealtyObjectPoolId): QIO[Option[RealtyObjectPool]]

    def delete(id: RealtyObjectPoolId): QIO[Unit]

}

object RealtyObjectPoolRepository {
    def create(
        name: Option[String] = None,
        userId: UserId): ZIO[DataSource with RealtyObjectPoolRepository, SQLException, RealtyObjectPool] =
        ZIO.serviceWithZIO[RealtyObjectPoolRepository](_.create(name, userId))

    def get(poolId: RealtyObjectPoolId)
        : ZIO[DataSource with RealtyObjectPoolRepository, SQLException, Option[RealtyObjectPool]] =
        ZIO.serviceWithZIO[RealtyObjectPoolRepository](_.get(poolId))

    def delete(id: RealtyObjectPoolId): ZIO[DataSource with RealtyObjectPoolRepository, SQLException, Unit] =
        ZIO.serviceWithZIO[RealtyObjectPoolRepository](_.delete(id))

    val live: ULayer[RealtyObjectPoolRepository] = RealtyObjectPoolRepositoryLive.layer
}
