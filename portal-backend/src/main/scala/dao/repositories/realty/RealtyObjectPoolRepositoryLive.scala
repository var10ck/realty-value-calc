package dao.repositories.realty
import dao.entities.auth.UserId
import dao.entities.realty.{RealtyObjectPool, RealtyObjectPoolId}
import io.getquill.context.ZioJdbc.QIO
import zio.{ULayer, ZIO, ZLayer}
import zio.metrics.Metric

final case class RealtyObjectPoolRepositoryLive() extends RealtyObjectPoolRepository {

    import db.Ctx._

    override def create(name: Option[String], userId: UserId): QIO[RealtyObjectPool] =
        for {
            pool <- RealtyObjectPool.make(name.getOrElse(RealtyObjectPool.makeName), userId)
            _ <- run(query[RealtyObjectPool].insertValue(lift(pool)))
            _ <- Metric.counter("realtyObjectPool.created").increment
        } yield pool

    override def get(poolId: RealtyObjectPoolId): QIO[Option[RealtyObjectPool]] =
        run(query[RealtyObjectPool].filter(pool => pool.id == lift(poolId))).map(_.headOption)

    override def delete(id: RealtyObjectPoolId): QIO[Unit] =
        run(query[RealtyObjectPool].filter(_.id == lift(id)).delete).unit
}

object RealtyObjectPoolRepositoryLive {
    lazy val layer: ULayer[RealtyObjectPoolRepository] = ZLayer.succeed(RealtyObjectPoolRepositoryLive())
}
