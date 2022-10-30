package services
import dao.entities.auth.UserId
import dao.entities.realty.{RealtyObject, RealtyObjectId}
import dao.repositories.realty.RealtyObjectRepository
import dto.realty.{CreateRealtyObjectDTO, DeleteRealtyObjectDTO, RealtyObjectInfoDTO}
import zio.{Scope, ZIO}
import zio.stream.ZStream

import java.sql.SQLException
import javax.sql.DataSource

trait RealtyObjectService {

    /** Takes ZStream, containing xlsx-file, converts xlsx rows to RealtyObject entities and writes them in database.
      * @param bodyStream
      *   http body containing binary-encoded xlsx file
      * @param userId
      *   user that uploads file
      */
    def importFromXlsx(
        bodyStream: ZStream[Any, Throwable, Byte],
        userId: UserId
    ): ZIO[DataSource with RealtyObjectRepository with Any with Scope, Throwable, Unit]

    /** Creates RealtyObject and writes into database */
    def createRealtyObject(
        dto: CreateRealtyObjectDTO,
        userId: UserId): ZIO[DataSource with RealtyObjectRepository, SQLException, RealtyObject]

    /** Get all realty objects, created and imported by user */
    def getRealtyObjectsForUser(
        userId: UserId): ZIO[DataSource with RealtyObjectRepository, SQLException, List[RealtyObject]]

    /** Delete Realty object with checking that it was created by user */
    def deleteRealtyObject(
        realtyObjectId: RealtyObjectId,
        userId: UserId): ZIO[DataSource with RealtyObjectRepository, Throwable, Unit]

    /** Return information about realty object with check that this object was added by attempting user */
    def getRealtyObjectInfo(
        realtyObjectId: String,
        userId: UserId): ZIO[DataSource with RealtyObjectRepository, Throwable, RealtyObjectInfoDTO]
}

object RealtyObjectService {

    /** Takes ZStream, containing xlsx-file, converts xlsx rows to RealtyObject entities and writes them in database.
      * @param bodyStream
      *   http body containing binary-encoded xlsx file
      * @param userId
      *   user that uploads file
      */
    def importFromXlsx(
        bodyStream: ZStream[Any, Throwable, Byte],
        userId: UserId
    ): ZIO[DataSource with RealtyObjectRepository with Any with Scope with RealtyObjectService, Throwable, Unit] =
        ZIO.serviceWithZIO[RealtyObjectService](_.importFromXlsx(bodyStream, userId))

    /** Creates RealtyObject and writes into database */
    def createRealtyObject(dto: CreateRealtyObjectDTO, userId: UserId)
        : ZIO[DataSource with RealtyObjectRepository with RealtyObjectService, SQLException, RealtyObject] =
        ZIO.serviceWithZIO[RealtyObjectService](_.createRealtyObject(dto, userId))

    /** Get all realty objects, created and imported by user */
    def getRealtyObjectsForUser(userId: UserId)
        : ZIO[DataSource with RealtyObjectRepository with RealtyObjectService, SQLException, List[RealtyObject]] =
        ZIO.serviceWithZIO[RealtyObjectService](_.getRealtyObjectsForUser(userId))

    /** Delete Realty object with checking that it was created by user */
    def deleteRealtyObject(
        realtyObjectId: RealtyObjectId,
        userId: UserId): ZIO[DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, Unit] =
        ZIO.serviceWithZIO[RealtyObjectService](_.deleteRealtyObject(realtyObjectId, userId))

    def getRealtyObjectInfo(realtyObjectId: String, userId: UserId)
        : ZIO[DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, RealtyObjectInfoDTO] =
        ZIO.serviceWithZIO[RealtyObjectService](_.getRealtyObjectInfo(realtyObjectId, userId))
}
