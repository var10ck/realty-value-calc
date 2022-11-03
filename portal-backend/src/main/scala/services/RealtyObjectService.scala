package services
import dao.entities.auth.{User, UserId}
import dao.entities.realty.{RealtyObject, RealtyObjectId}
import dao.repositories.realty.RealtyObjectRepository
import dto.realty.{CreateRealtyObjectDTO, DeleteRealtyObjectDTO, RealtyObjectInfoDTO, UpdateRealtyObjectDTO}
import zio.{Scope, ULayer, ZIO}
import zio.stream.ZStream

import java.io.File
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

    /** Getting all RealtyObjects added by User and writes it into xlsx-file */
    def exportRealtyObjectsOfUserToXlsx(user: User)
        : ZIO[Any with Scope with DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, File]

    /** Creates RealtyObject and writes into database */
    def createRealtyObject(
        dto: CreateRealtyObjectDTO,
        userId: UserId): ZIO[DataSource with RealtyObjectRepository, Throwable, RealtyObject]

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

    /** Updates RealtyObject if this object was added by attempting User
      * @param userId
      *   retrieving User's id
      */
    def updateRealtyObjectInfo(
        dto: UpdateRealtyObjectDTO,
        userId: UserId
    ): ZIO[DataSource with RealtyObjectRepository, Throwable, Unit]
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

    def exportRealtyObjectsOfUserToXlsx(user: User)
        : ZIO[Any with Scope with DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, File] =
        ZIO.serviceWithZIO[RealtyObjectService](_.exportRealtyObjectsOfUserToXlsx(user))

    /** Creates RealtyObject and writes into database */
    def createRealtyObject(dto: CreateRealtyObjectDTO, userId: UserId)
        : ZIO[DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, RealtyObject] =
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

    /** Updates RealtyObject if this object was added by attempting User
      * @param userId
      *   retrieving User's id
      */
    def updateRealtyObjectInfo(
        dto: UpdateRealtyObjectDTO,
        userId: UserId
    ): ZIO[DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, Unit] =
        ZIO.serviceWithZIO[RealtyObjectService](_.updateRealtyObjectInfo(dto, userId))

    val live: ULayer[RealtyObjectService] = RealtyObjectServiceLive.layer
}
