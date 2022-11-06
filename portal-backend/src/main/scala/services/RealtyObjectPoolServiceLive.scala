package services
import dao.entities.auth.UserId
import dao.entities.realty.{RealtyObjectPool, RealtyObjectPoolId}
import dao.repositories.realty.{RealtyObjectPoolRepository, RealtyObjectPoolRepositoryLive}
import dto.realty.PoolInfoDTO
import zio.{ULayer, ZIO, ZLayer}

import java.sql.SQLException
import javax.sql.DataSource

final case class RealtyObjectPoolServiceLive() extends RealtyObjectPoolService {
    override def create(
        name: Option[String],
        userId: UserId): ZIO[DataSource with RealtyObjectPoolRepository, SQLException, RealtyObjectPool] =
        RealtyObjectPoolRepository.create(name, userId)

    override def get(
        poolId: String,
        userId: UserId): ZIO[DataSource with RealtyObjectPoolRepository, Throwable, RealtyObjectPool] = {
        for {
            typedId <- RealtyObjectPoolId.fromString(poolId)
            poolOpt <- RealtyObjectPoolRepository.get(typedId)
            pool <- ZIO
                .fromOption(poolOpt)
                .orElseFail(exceptions.RealityObjectsPoolNotFound("id", poolId))
            result <- ZIO.ifZIO(ZIO.succeed(pool.userId == userId))(
              ZIO.succeed(pool),
              ZIO.fail(exceptions.NotEnoughRightsException("User is not creator of this pool"))
            )
        } yield result
    }

    override def getAllOfUser(userId: UserId): ZIO[
      DataSource with RealtyObjectPoolRepository,
      Throwable,
      List[PoolInfoDTO]] =
        for{
            pools <- RealtyObjectPoolRepository.filterByUser(userId)
        } yield pools.map(PoolInfoDTO.fromEntity)

    override def delete(poolId: String, userId: UserId): ZIO[DataSource with RealtyObjectPoolRepository, Throwable, Unit] =
        for {
            typedId <- RealtyObjectPoolId.fromString(poolId)
            poolOpt <- RealtyObjectPoolRepository.get(typedId)
            pool <- ZIO
                .fromOption(poolOpt)
                .orElseFail(exceptions.RealityObjectsPoolNotFound("id", poolId))
            _ <- ZIO.ifZIO(ZIO.succeed(pool.userId == userId))(
              RealtyObjectPoolRepository.delete(pool.id),
              ZIO.fail(exceptions.NotEnoughRightsException("User is not creator of this pool"))
            )
        } yield ()
}

object RealtyObjectPoolServiceLive{
    lazy val layer: ULayer[RealtyObjectPoolServiceLive] = ZLayer.succeed(RealtyObjectPoolServiceLive())
}
