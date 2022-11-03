package dao.repositories.realty
import dao.entities.auth.UserId
import dao.entities.realty.{RealtyObject, RealtyObjectId, RealtyObjectPoolId}
import io.getquill.context.ZioJdbc.QIO
import zio.{ULayer, ZLayer}
import zio.metrics.Metric

import java.time.LocalDateTime

final case class RealtyObjectRepositoryLive() extends RealtyObjectRepository {

    import db.Ctx._

    /** Creates a new RealtyObject. */
    override def create(
        location: String,
        roomsNumber: Int,
        segment: String,
        floorCount: Int,
        wallMaterial: String,
        floorNumber: Int,
        totalArea: Double,
        kitchenArea: Double,
        gotBalcony: Boolean,
        condition: String,
        distanceFromMetro: Int,
        addedByUserId: UserId,
        calculatedValue: Option[Long] = None,
        poolId: RealtyObjectPoolId): QIO[RealtyObject] =
        for {
            realtyObject <- RealtyObject.make(
              location = location,
              roomsNumber = roomsNumber,
              segment = segment,
              floorCount = floorCount,
              wallMaterial = wallMaterial,
              floorNumber = floorNumber,
              totalArea = totalArea,
              kitchenArea = kitchenArea,
              gotBalcony = gotBalcony,
              condition = condition,
              distanceFromMetro = distanceFromMetro,
              addedByUserId = addedByUserId,
              calculatedValue = calculatedValue,
              poolId = poolId
            )
            _ <- run(query[RealtyObject].insertValue(lift(realtyObject)))
            _ <- Metric.counter("realtyObject.created").increment
        } yield realtyObject

    /** Deletes an existing RealtyObject. */
    override def delete(id: RealtyObjectId): QIO[Unit] =
        run(query[RealtyObject].filter(_.id == lift(id)).delete).unit

    /** Retrieves a RealtyObject from the database by id. */
    override def get(id: RealtyObjectId): QIO[Option[RealtyObject]] =
        run(query[RealtyObject].filter(_.id == lift(id))).map(_.headOption)

    /** Retrieves all RealtyObjects from the database. */
    override def getAllByUser(userId: UserId): QIO[List[RealtyObject]] =
        run(query[RealtyObject].filter(_.addedByUserId == lift(userId)).sortBy(_.location)).map(_.toList)

    /** Updates info of an existing User. */
    override def updateInfo(
        id: RealtyObjectId,
        location: Option[String] = None,
        roomsNumber: Option[Int] = None,
        segment: Option[String] = None,
        floorCount: Option[Int] = None,
        wallMaterial: Option[String] = None,
        floorNumber: Option[Int] = None,
        totalArea: Option[Double] = None,
        kitchenArea: Option[Double] = None,
        gotBalcony: Option[Boolean] = None,
        condition: Option[String] = None,
        distanceFromMetro: Option[Int] = None,
        calculatedValue: Option[Long] = None): QIO[Unit] = {
        run(
          dynamicQuery[RealtyObject]
              .filter(_.id == lift(id))
              update (
                setOpt(_.location, location),
                setOpt(_.roomsNumber, roomsNumber),
                setOpt(_.segment, segment),
                setOpt(_.floorCount, floorCount),
                setOpt(_.wallMaterial, wallMaterial),
                setOpt(_.floorNumber, floorNumber),
                setOpt(_.totalArea, totalArea),
                setOpt(_.kitchenArea, kitchenArea),
                setOpt(_.gotBalcony, gotBalcony),
                setOpt(_.condition, condition),
                setOpt(_.distanceFromMetro, distanceFromMetro),
                setOpt(_.calculatedValue, calculatedValue.map(Option(_))),
                setValue(_.updatedAt, LocalDateTime.now())
              )
        ).unit
    }

    /** Set calculatedValue to RealtyObject */
    override def setCalculatedValue(id: RealtyObjectId, calculatedValue: Long): QIO[Unit] =
        run(
          query[RealtyObject]
              .filter(_.id == lift(id))
              .update(
                _.calculatedValue -> lift(Option(calculatedValue))
              )
        ).unit
}

object RealtyObjectRepositoryLive {
    lazy val layer: ULayer[RealtyObjectRepositoryLive] = ZLayer.succeed(RealtyObjectRepositoryLive())
}
