package dao.repositories.realty
import dao.entities.realty.{RealtyObjectPool, RealtyObjectPoolId}
import io.getquill.context.ZioJdbc.QIO
import zio.{ULayer, ZLayer}
import zio.metrics.Metric

final case class RealtyObjectPoolRepositoryLive() extends RealtyObjectPoolRepository {

    import db.Ctx._

    override def create(name: Option[String]): QIO[RealtyObjectPool] =
        for {
            pool <- if (name.isDefined) RealtyObjectPool.make(name.get) else RealtyObjectPool.make
            _ <- run(query[RealtyObjectPool].insertValue(lift(pool)))
            _ <- Metric.counter("realtyObjectPool.created").increment
        } yield pool

    override def delete(id: RealtyObjectPoolId): QIO[Unit] =
        run(query[RealtyObjectPool].filter(_.id == lift(id)).delete).unit
}

object RealtyObjectPoolRepositoryLive{
    lazy val layer: ULayer[RealtyObjectRepository] = ZLayer.succeed(RealtyObjectRepositoryLive())
}