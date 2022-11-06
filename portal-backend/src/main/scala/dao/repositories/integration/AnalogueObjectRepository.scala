package dao.repositories.integration
import dao.entities.integration.{AnalogueObject, AnalogueObjectId}
import dao.entities.realty.{RealtyObject, RealtyObjectId}
import io.getquill.context.ZioJdbc.QIO
import zio.{ULayer, ZIO}

import java.sql.SQLException
import javax.sql.DataSource

trait AnalogueObjectRepository {

    def create(
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
        realtyObjectId: RealtyObjectId
    ): QIO[Unit]

    def insert(analogueObject: AnalogueObject): QIO[Unit]

    def getAllByRealtyObjectId(realtyObjectId: RealtyObjectId): QIO[List[AnalogueObject]]

    def delete(id: AnalogueObjectId): QIO[Unit]

}

object AnalogueObjectRepository {
    def create(
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
        realtyObjectId: RealtyObjectId): ZIO[DataSource with AnalogueObjectRepository, SQLException, Unit] =
        ZIO.serviceWithZIO[AnalogueObjectRepository](
          _.create(
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
          ))

    def insert(analogueObject: AnalogueObject): ZIO[DataSource with AnalogueObjectRepository, SQLException, Unit] =
        ZIO.serviceWithZIO[AnalogueObjectRepository](_.insert(analogueObject))

    def getAllByRealtyObjectId(realtyObjectId: RealtyObjectId)
        : ZIO[DataSource with AnalogueObjectRepository, SQLException, List[AnalogueObject]] =
        ZIO.serviceWithZIO[AnalogueObjectRepository](_.getAllByRealtyObjectId(realtyObjectId))

    def delete(id: AnalogueObjectId): ZIO[DataSource with AnalogueObjectRepository, SQLException, Unit] =
        ZIO.serviceWithZIO[AnalogueObjectRepository](_.delete(id))

    def live: ULayer[AnalogueObjectRepository] = AnalogueObjectRepositoryLive.layer
}
