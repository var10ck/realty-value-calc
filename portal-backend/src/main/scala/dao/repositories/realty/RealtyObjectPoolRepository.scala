package dao.repositories.realty
import dao.entities.realty.{RealtyObjectPool, RealtyObjectPoolId}
import io.getquill.context.ZioJdbc.QIO
import zio.ZIO

import java.sql.SQLException
import javax.sql.DataSource

trait RealtyObjectPoolRepository {

    def create(name: Option[String] = None): QIO[RealtyObjectPool]

    def delete(id: RealtyObjectPoolId): QIO[Unit]

}

object RealtyObjectPoolRepository {
    def create(
        name: Option[String] = None): ZIO[DataSource with RealtyObjectPoolRepository, SQLException, RealtyObjectPool] =
        ZIO.serviceWithZIO[RealtyObjectPoolRepository](_.create(name))

    def delete(id: RealtyObjectPoolId): ZIO[DataSource with RealtyObjectPoolRepository, SQLException, Unit] =
        ZIO.serviceWithZIO[RealtyObjectPoolRepository](_.delete(id))
}
