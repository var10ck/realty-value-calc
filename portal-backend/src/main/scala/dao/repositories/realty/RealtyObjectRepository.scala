package dao.repositories.realty
import dao.entities.auth.UserId
import dao.entities.realty.{RealtyObject, RealtyObjectId}
import io.getquill.context.ZioJdbc.QIO
import zio.ZIO

import java.sql.SQLException
import javax.sql.DataSource

trait RealtyObjectRepository {

    /** Creates a new RealtyObject and insert into database. */
    def create(
        location: String,
        roomsNumber: Int,
        segment: String,
        floorCount: Int,
        wallMaterial: String,
        floorNumber: Int,
        totalArea: Double,
        kitchenArea: Double,
        gotBalcony: String,
        condition: String,
        distanceFromMetro: Int,
        addedByUserId: UserId,
        calculatedValue: Option[Long] = None): QIO[RealtyObject]

    /** Deletes an existing RealtyObject. */
    def delete(id: RealtyObjectId): QIO[Unit]

    /** Retrieves a RealtyObject from the database by id. */
    def get(id: RealtyObjectId): QIO[Option[RealtyObject]]

    /** Retrieves all RealtyObjects from the database. */
    def getAllByUser(userId: UserId): QIO[List[RealtyObject]]

    /** Updates info of an existing User. */
    def updateInfo(
        id: RealtyObjectId,
        location: Option[String],
        roomsNumber: Option[Int],
        segment: Option[String],
        floorCount: Option[Int],
        wallMaterial: Option[String],
        floorNumber: Option[Int],
        totalArea: Option[Double],
        kitchenArea: Option[Double],
        gotBalcony: Option[String],
        condition: Option[String],
        distanceFromMetro: Option[Int],
        calculatedValue: Option[Long]
    ): QIO[Unit]

    /** Set calculatedValue to RealtyObject */
    def setCalculatedValue(id: RealtyObjectId, calculatedValue: Long): QIO[Unit]
}

object RealtyObjectRepository {

    /** Creates a new RealtyObject. */
    def create(
        location: String,
        roomsNumber: Int,
        segment: String,
        floorCount: Int,
        wallMaterial: String,
        floorNumber: Int,
        totalArea: Double,
        kitchenArea: Double,
        gotBalcony: String,
        condition: String,
        distanceFromMetro: Int,
        addedByUserId: UserId,
        calculatedValue: Option[Long] = None): ZIO[DataSource with RealtyObjectRepository, SQLException, RealtyObject] =
        ZIO.serviceWithZIO[RealtyObjectRepository](
          _.create(
            location,
            roomsNumber,
            segment,
            floorCount,
            wallMaterial,
            floorNumber,
            totalArea,
            kitchenArea,
            gotBalcony,
            condition,
            distanceFromMetro,
            addedByUserId,
            calculatedValue
          ))

    /** Deletes an existing RealtyObject. */
    def delete(id: RealtyObjectId): ZIO[DataSource with RealtyObjectRepository, SQLException, Unit] =
        ZIO.serviceWithZIO[RealtyObjectRepository](_.delete(id))

    /** Retrieves a RealtyObject from the database by id. */
    def get(id: RealtyObjectId): ZIO[DataSource with RealtyObjectRepository, SQLException, Option[RealtyObject]] =
        ZIO.serviceWithZIO[RealtyObjectRepository](_.get(id))

    /** Retrieves all RealtyObjects from the database. */
    def getAllByUser(userId: UserId): ZIO[DataSource with RealtyObjectRepository, SQLException, List[RealtyObject]] =
        ZIO.serviceWithZIO[RealtyObjectRepository](_.getAllByUser(userId))

    /** Updates info of an existing User. */
    def updateInfo(
        id: RealtyObjectId,
        location: Option[String] = None,
        roomsNumber: Option[Int] = None,
        segment: Option[String] = None,
        floorCount: Option[Int] = None,
        wallMaterial: Option[String] = None,
        floorNumber: Option[Int] = None,
        totalArea: Option[Double] = None,
        kitchenArea: Option[Double] = None,
        gotBalcony: Option[String] = None,
        condition: Option[String] = None,
        distanceFromMetro: Option[Int] = None,
        calculatedValue: Option[Long] = None
    ): ZIO[DataSource with RealtyObjectRepository, SQLException, Unit] = ZIO.serviceWithZIO[RealtyObjectRepository](
      _.updateInfo(
        id,
        location,
        roomsNumber,
        segment,
        floorCount,
        wallMaterial,
        floorNumber,
        totalArea,
        kitchenArea,
        gotBalcony,
        condition,
        distanceFromMetro,
        calculatedValue))

    /** Set calculatedValue to RealtyObject */
    def setCalculatedValue(
        id: RealtyObjectId,
        calculatedValue: Long): ZIO[DataSource with RealtyObjectRepository, SQLException, Unit] =
        ZIO.serviceWithZIO[RealtyObjectRepository](_.setCalculatedValue(id, calculatedValue))
}
