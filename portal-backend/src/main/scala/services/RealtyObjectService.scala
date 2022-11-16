package services
import dao.entities.auth.{User, UserId}
import dao.entities.realty.{RealtyObject, RealtyObjectId, RealtyObjectPoolId}
import dao.repositories.corrections.{CorrectionConstantRepository, CorrectionNumericRepository}
import dao.repositories.integration.AnalogueObjectRepository
import dao.repositories.realty.{RealtyObjectPoolRepository, RealtyObjectRepository}
import dto.realty.{CalculateValueOfSomeObjectsDTO, CreateRealtyObjectDTO, DeleteRealtyObjectDTO, ExportSomeObjectsDTO, RealtyObjectInfoDTO, UpdateRealtyObjectDTO}
import zhttp.service.{ChannelFactory, EventLoopGroup}
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
    ): ZIO[
      DataSource
          with RealtyObjectPoolRepository with RealtyObjectRepository with EventLoopGroup with ChannelFactory
          with configuration.ApplicationConfig with GeoSuggestionService with Any with Scope,
      Throwable,
      RealtyObjectPoolId]

    /** Getting all RealtyObjects added by User and writes it into xlsx-file */
    def exportRealtyObjectsOfUserToXlsx(user: User)
        : ZIO[Any with Scope with DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, File]

    /** Export Objects of user with poolId to xlsx */
    def exportPoolOfObjectsToXlsx(user: User, poolId: String)
        : ZIO[Any with Scope with DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, File]

    /** Export selected realty objects to xlsx */
    def exportSelectedObjectsToXlsx(
        dto: ExportSomeObjectsDTO, userId: UserId): ZIO[Any with Scope with DataSource with RealtyObjectRepository, Throwable, File]

    /** Creates RealtyObject and writes into database */
    def createRealtyObject(
        dto: CreateRealtyObjectDTO,
        userId: UserId,
        latitude: Option[String],
        longitude: Option[String]): ZIO[DataSource with RealtyObjectRepository, Throwable, RealtyObject]

    /** Get all realty objects, created and imported by user */
    def getRealtyObjectsForUser(
        userId: UserId): ZIO[DataSource with RealtyObjectRepository, Throwable, List[RealtyObject]]

    /** Delete Realty object with checking that it was created by user */
    def deleteRealtyObject(
        realtyObjectId: RealtyObjectId,
        userId: UserId): ZIO[DataSource with RealtyObjectRepository, Throwable, Unit]

    /** Return information about realty object with check that this object was added by attempting user */
    def getRealtyObjectInfo(
        realtyObjectId: String,
        userId: UserId): ZIO[DataSource with RealtyObjectRepository with AnalogueObjectRepository, Throwable, RealtyObjectInfoDTO]

    /** Updates RealtyObject if this object was added by attempting User
      * @param userId
      *   retrieving User's id
      */
    def updateRealtyObjectInfo(
        dto: UpdateRealtyObjectDTO,
        userId: UserId
    ): ZIO[DataSource with RealtyObjectRepository, Throwable, Unit]

    /** Retrieves all RealityObject where coordinates is not set and try fill it using GeoSuggestionService */
    def fillCoordinatesOnAllRealtyObjects: ZIO[
      DataSource
          with RealtyObjectRepository with EventLoopGroup with ChannelFactory with configuration.ApplicationConfig
          with GeoSuggestionService,
      Throwable,
      Unit]

    /** Calculates market value of all objects in pool */
    def calculateAllInPool(poolId: String, userId: UserId, withCorrections: Boolean, numPages: Int, limitOfAnalogs: Int): ZIO[
      DataSource
          with RealtyObjectRepository with AnalogueObjectRepository with EventLoopGroup with ChannelFactory with configuration.ApplicationConfig
          with SearchRealtyService with CorrectionNumericRepository with CorrectionConstantRepository,
      Throwable,
      Unit]

    /** Calculates value of some RealtyObjects */
    def calculateForSome(
        dto: CalculateValueOfSomeObjectsDTO,
        userId: UserId,
        numPages: Int,
        limitOfAnalogs: Int): ZIO[
      DataSource
          with RealtyObjectRepository with AnalogueObjectRepository with EventLoopGroup with ChannelFactory with configuration.ApplicationConfig
          with SearchRealtyService with CorrectionNumericRepository with CorrectionConstantRepository,
      Throwable,
      Unit]
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
    ): ZIO[
      DataSource
          with RealtyObjectPoolRepository with RealtyObjectRepository with EventLoopGroup with ChannelFactory
          with configuration.ApplicationConfig with GeoSuggestionService with Any with Scope with RealtyObjectService,
      Throwable,
      RealtyObjectPoolId] =
        ZIO.serviceWithZIO[RealtyObjectService](_.importFromXlsx(bodyStream, userId))

    /** Getting all RealtyObjects added by User and writes it into xlsx-file */
    def exportRealtyObjectsOfUserToXlsx(user: User)
        : ZIO[Any with Scope with DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, File] =
        ZIO.serviceWithZIO[RealtyObjectService](_.exportRealtyObjectsOfUserToXlsx(user))

    /** Getting RealtyObjects added by User and belong to RealtyObjectsPool with poolId, then writes it into xlsx-file
      */
    def exportPoolOfObjectsToXlsx(user: User, poolId: String)
        : ZIO[Any with Scope with DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, File] =
        ZIO.serviceWithZIO[RealtyObjectService](_.exportPoolOfObjectsToXlsx(user, poolId))

    def exportSelectedObjectsToXlsx(dto: ExportSomeObjectsDTO, userId: UserId)
        : ZIO[Any with Scope with DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, File] =
        ZIO.serviceWithZIO[RealtyObjectService](_.exportSelectedObjectsToXlsx(dto, userId))

    /** Creates RealtyObject and writes into database */
    def createRealtyObject(
        dto: CreateRealtyObjectDTO,
        userId: UserId,
        latitude: Option[String],
        longitude: Option[String])
        : ZIO[DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, RealtyObject] =
        ZIO.serviceWithZIO[RealtyObjectService](_.createRealtyObject(dto, userId, latitude, longitude))

    /** Get all realty objects, created and imported by user */
    def getRealtyObjectsForUser(userId: UserId)
        : ZIO[DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, List[RealtyObject]] =
        ZIO.serviceWithZIO[RealtyObjectService](_.getRealtyObjectsForUser(userId))

    /** Delete Realty object with checking that it was created by user */
    def deleteRealtyObject(
        realtyObjectId: RealtyObjectId,
        userId: UserId): ZIO[DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, Unit] =
        ZIO.serviceWithZIO[RealtyObjectService](_.deleteRealtyObject(realtyObjectId, userId))

    /** Return information about realty object with check that this object was added by attempting user */
    def getRealtyObjectInfo(realtyObjectId: String, userId: UserId)
        : ZIO[DataSource with RealtyObjectRepository with AnalogueObjectRepository with RealtyObjectService, Throwable, RealtyObjectInfoDTO] =
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

    /** Retrieves all RealityObject where coordinates is not set and try fill it using GeoSuggestionService */
    def fillCoordinatesOnAllRealtyObjects: ZIO[
      DataSource
          with RealtyObjectRepository with EventLoopGroup with ChannelFactory with configuration.ApplicationConfig
          with GeoSuggestionService with RealtyObjectService,
      Throwable,
      Unit] = ZIO.serviceWithZIO[RealtyObjectService](_.fillCoordinatesOnAllRealtyObjects)

    /** Calculates market value of all objects in pool */
    def calculateAllInPool(poolId: String, userId: UserId, withCorrections: Boolean, numPages: Int,
                           limitOfAnalogs: Int = 20): ZIO[
      DataSource
          with RealtyObjectRepository with AnalogueObjectRepository with EventLoopGroup with ChannelFactory with configuration.ApplicationConfig
          with SearchRealtyService with RealtyObjectService with CorrectionNumericRepository with CorrectionConstantRepository,
      Throwable,
      Unit] =
        ZIO.serviceWithZIO[RealtyObjectService](_.calculateAllInPool(poolId, userId, withCorrections, numPages, limitOfAnalogs))

    /** Calculates value of some RealtyObjects */
    def calculateForSome(
        dto: CalculateValueOfSomeObjectsDTO,
        userId: UserId,
        numPages: Int,
        limitOfAnalogs: Int = 20): ZIO[
      DataSource
          with RealtyObjectRepository with AnalogueObjectRepository with EventLoopGroup with ChannelFactory with configuration.ApplicationConfig
          with SearchRealtyService with RealtyObjectService with CorrectionNumericRepository with CorrectionConstantRepository,
      Throwable,
      Unit] =
        ZIO.serviceWithZIO[RealtyObjectService](_.calculateForSome(dto, userId, numPages, limitOfAnalogs))

    val live: ULayer[RealtyObjectService] = RealtyObjectServiceLive.layer
}
