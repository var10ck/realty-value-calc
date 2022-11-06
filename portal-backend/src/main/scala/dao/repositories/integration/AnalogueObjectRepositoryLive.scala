package dao.repositories.integration
import dao.entities.integration.{AnalogueObject, AnalogueObjectId}
import dao.entities.realty.{RealtyObject, RealtyObjectId}
import io.getquill.context.ZioJdbc.QIO
import zio.{ULayer, ZLayer}
import zio.metrics.Metric

final case class AnalogueObjectRepositoryLive() extends AnalogueObjectRepository {

    import db.Ctx._

    override def create(
        url: Option[String],
        latitude: Option[Double],
        longitude: Option[Double],
        price: Option[Int],
        roomsNumber: Option[Int],
        apartmentsType: Option[String],
        material: Option[String],
        floor: Option[Int],
        area: Option[Double],
        kitchenArea: Option[Double],
        balcony: Boolean,
        distanceToMetroInMinutes: Option[Int],
        condition: Option[String],
        floors: Option[Int],
        realtyObjectId: RealtyObjectId): QIO[Unit] = {
        for {
            entity <- AnalogueObject.make(
              url,
              latitude,
              longitude,
              price,
              roomsNumber,
              apartmentsType,
              material,
              floor,
              area,
              kitchenArea,
              balcony,
              distanceToMetroInMinutes,
              condition,
              floors,
              realtyObjectId
            )
            _ <- run(query[AnalogueObject].insertValue(lift(entity)))
            _ <- Metric.counter("analogueObject.created").increment
        } yield ()
    }

    override def insert(analogueObject: AnalogueObject): QIO[Unit] = run(query[AnalogueObject].insertValue(lift(analogueObject))).unit

    override def getAllByRealtyObjectId(realtyObjectId: RealtyObjectId): QIO[List[AnalogueObject]] =
        run(query[AnalogueObject].filter(_.realtyObjectId == lift(realtyObjectId))).map(_.toList)

    override def delete(id: AnalogueObjectId): QIO[Unit] = run(
      query[AnalogueObject].filter(_.id == lift(id)).delete).unit
}

object AnalogueObjectRepositoryLive{
    val layer: ULayer[AnalogueObjectRepository] = ZLayer.succeed(AnalogueObjectRepositoryLive())
}