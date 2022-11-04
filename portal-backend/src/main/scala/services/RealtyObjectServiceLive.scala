package services
import dao.entities.auth.{User, UserId}
import dao.entities.realty.{RealtyObject, RealtyObjectId, RealtyObjectPoolId}
import dao.repositories.realty.{RealtyObjectPoolRepository, RealtyObjectRepository}
import dto.realty.{CoordinatesDTO, CreateRealtyObjectDTO, DeleteRealtyObjectDTO, RealtyObjectInfoDTO, UpdateRealtyObjectDTO}
import helpers.{ExcelHelper, FileHelper}
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.{Scope, ULayer, ZIO, ZLayer}
import zio.stream.{ZSink, ZStream}
import zio.Console

import java.io.{File, FileInputStream}
import java.sql.SQLException
import javax.sql.DataSource

final case class RealtyObjectServiceLive() extends RealtyObjectService {

    /** Takes ZStream, containing xlsx-file, converts xlsx rows to RealtyObject entities and writes them in database.
      * Executing insertion in database in new fiber.
      * @param bodyStream
      *   http body containing binary-encoded xlsx file
      * @param userId
      *   user that uploads file
      */
    override def importFromXlsx(bodyStream: ZStream[Any, Throwable, Byte], userId: UserId): ZIO[
      DataSource with RealtyObjectPoolRepository
          with RealtyObjectRepository with EventLoopGroup with ChannelFactory with configuration.ApplicationConfig
          with GeoSuggestionService with Any with Scope,
      Throwable,
      RealtyObjectPoolId] =
        for {
            // make temp file and write stream to it
            tempFile <- FileHelper.makeTempFileZIO("upload", ".xlsx")
            bytesWritten <- bodyStream.run(ZSink.fromFile(tempFile))
            _ <- Console.printLine(s"created temp file $tempFile and $bytesWritten written")
            // read file and convert rows in xlsx to objects in database
            poolId <- transmitXlsxObjectsToDatabase(tempFile, userId)
            // run filling coordinates of objects in background
            _ <- fillCoordinatesOnAllRealtyObjects.forkDaemon
        } yield poolId

    /** Takes xlsx-file, transforms it to RealtyObject entities and writes into database
      * @param file
      *   xlsx-file with records about realty objects
      * @param userId
      *   user, importing these objects
      */
    private def transmitXlsxObjectsToDatabase(file: File, userId: UserId)
        : ZIO[DataSource with RealtyObjectPoolRepository with RealtyObjectRepository with Any with Scope, Throwable, RealtyObjectPoolId] =
        for {
            fileInputStream <- FileHelper.makeFileInputStream(file)
            realtyObjectsExcelDtoList <- ExcelHelper.transformXlsxToObject(fileInputStream) <*
                ZIO.from(file.delete())
            _ <- Console.printLine(s"objects imported from ${file.getAbsolutePath}")
            pool <- RealtyObjectPoolRepository.create(None, userId).mapError(e =>new Throwable(e.getMessage))
            _ <- (ZIO foreach realtyObjectsExcelDtoList) { dto =>
                RealtyObjectRepository
                    .create(
                      location = dto.location,
                      roomsNumber = dto.roomsNumber,
                      segment = dto.segment,
                      floorCount = dto.floorCount,
                      wallMaterial = dto.wallMaterial,
                      floorNumber = dto.floorNumber,
                      totalArea = dto.totalArea,
                      kitchenArea = dto.kitchenArea,
                      gotBalcony = dto.gotBalcony,
                      condition = dto.condition,
                      distanceFromMetro = dto.distanceFromMetro,
                      addedByUserId = userId,
                      poolId = pool.id
                    )
            }
        } yield pool.id

    /** Getting all RealtyObjects added by User and writes it into xlsx-file */
    override def exportRealtyObjectsOfUserToXlsx(user: User)
        : ZIO[Any with Scope with DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, File] =
        for {
            realtyObjects <- RealtyObjectService.getRealtyObjectsForUser(user.id)
            tempFile <- FileHelper.makeTempFileZIO("upload-", ".xlsx")
            _ <- Console.printLine(s"Created temporary file \"${tempFile.getAbsolutePath}\"")
            inputStream <- FileHelper.makeFileOutputStream(tempFile)
            _ <- ExcelHelper.transformObjectsToXlsx(inputStream, realtyObjects)
            _ <- Console.printLine(
              s"Wrote ${realtyObjects.length} objects $realtyObjects to file ${tempFile.getAbsolutePath}"
            ) // <* ZIO.from(tempFile.delete()).forkDaemon
        } yield tempFile

    /** Getting RealtyObjects added by User and belong to RealtyObjectsPool with poolId, then writes it into xlsx-file
      */
    override def exportPoolOfObjectsToXlsx(user: User, poolId: String)
        : ZIO[Any with Scope with DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, File] =
        for {
            poolIdTyped <- RealtyObjectPoolId.fromString(poolId)
            allRealtyObjectsOfUser <- RealtyObjectService.getRealtyObjectsForUser(user.id)
            poolObjects = allRealtyObjectsOfUser.filter(_.poolId == poolIdTyped)
            tempFile <- FileHelper.makeTempFileZIO("upload", ".xlsx")
            _ <- Console.printLine(s"Created temporary file \"${tempFile.getAbsolutePath}\"")
            inputStream <- FileHelper.makeFileOutputStream(tempFile)
            _ <- ExcelHelper.transformObjectsToXlsx(inputStream, poolObjects)
            _ <- Console.printLine(
              s"Wrote ${poolObjects.length} objects $poolObjects to file ${tempFile.getAbsolutePath}")
        } yield tempFile

    /** Creates RealtyObject and writes into database */
    override def createRealtyObject(
        dto: CreateRealtyObjectDTO,
        userId: UserId,
        latitude: Option[String],
        longitude: Option[String]): ZIO[DataSource with RealtyObjectRepository, Throwable, RealtyObject] =
        for {
            poolId <- RealtyObjectPoolId.fromString(dto.poolId)
            realtyObject <- RealtyObjectRepository.create(
              dto.location,
              dto.roomsNumber,
              dto.segment,
              dto.floorCount,
              dto.wallMaterial,
              dto.floorNumber,
              dto.totalArea,
              dto.kitchenArea,
              dto.gotBalcony,
              dto.condition,
              dto.distanceFromMetro,
              userId,
              poolId = poolId,
              latitude = latitude,
              longitude = longitude
            )
        } yield realtyObject

    /** Get all realty objects, created and imported by user */
    override def getRealtyObjectsForUser(
        userId: UserId): ZIO[DataSource with RealtyObjectRepository, SQLException, List[RealtyObject]] =
        RealtyObjectRepository.getAllByUser(userId)

    /** Delete Realty object with checking that it was created by user */
    override def deleteRealtyObject(
        realtyObjectId: RealtyObjectId,
        userId: UserId): ZIO[DataSource with RealtyObjectRepository, Throwable, Unit] = {
        for {
            realtyObjOpt <- RealtyObjectRepository.get(realtyObjectId)
            realtyObj <- ZIO
                .fromOption(realtyObjOpt)
                .orElseFail(exceptions.RealtyObjectNotFound("id", realtyObjectId.id.toString))
            _ <- ZIO.ifZIO(ZIO.from(realtyObj.addedByUserId == userId))(
              RealtyObjectRepository.delete(realtyObjectId),
              ZIO.fail(exceptions.NotEnoughRightsException("User is not author of object"))
            )
        } yield ()

    }

    /** Return information about realty object with check that this object was added by attempting user */
    def getRealtyObjectInfo(
        realtyObjectId: String,
        userId: UserId): ZIO[DataSource with RealtyObjectRepository, Throwable, RealtyObjectInfoDTO] =
        for {
            id <- RealtyObjectId.fromString(realtyObjectId)
            realtyObjectOpt <- RealtyObjectRepository.get(id)
            entity <- ZIO.fromOption(realtyObjectOpt).orElseFail(exceptions.RealtyObjectNotFound("id", realtyObjectId))
            obj <- ZIO.ifZIO(ZIO.succeed(entity.addedByUserId == userId))(
              ZIO.succeed(entity),
              ZIO.fail(exceptions.NotEnoughRightsException("User is not author of object"))
            )
        } yield RealtyObjectInfoDTO.fromEntity(obj)

    /** Updates RealtyObject if this object was added by attempting User
      * @param userId
      *   retrieving User's id
      */
    override def updateRealtyObjectInfo(
        dto: UpdateRealtyObjectDTO,
        userId: UserId): ZIO[DataSource with RealtyObjectRepository, Throwable, Unit] = for {
        realtyObjectId <- RealtyObjectId.fromString(dto.id)
        realtyObjectOpt <- RealtyObjectRepository.get(realtyObjectId)
        realtyObject <- ZIO.fromOption(realtyObjectOpt).orElseFail(exceptions.RealtyObjectNotFound("id", dto.id))
        _ <- ZIO.ifZIO(ZIO.succeed(realtyObject.addedByUserId == userId))(
          RealtyObjectRepository.updateInfo(
            realtyObjectId,
            dto.location,
            dto.roomsNumber,
            dto.segment,
            dto.floorCount,
            dto.wallMaterial,
            dto.floorNumber,
            dto.totalArea,
            dto.kitchenArea,
            dto.gotBalcony,
            dto.condition,
            dto.distanceFromMetro,
            dto.calculatedValue
          ),
          ZIO.fail(exceptions.NotEnoughRightsException("Realty object was added by another user"))
        )
    } yield ()

    override def fillCoordinatesOnAllRealtyObjects: ZIO[
      DataSource
          with RealtyObjectRepository with EventLoopGroup with ChannelFactory with configuration.ApplicationConfig
          with GeoSuggestionService,
      Throwable,
      Unit] =
        for {
            objectsToFill <- RealtyObjectRepository.getAllWithoutCoordinates
            res <- ZIO.foreach(objectsToFill) { obj =>
                for {
                    coordsDto <- GeoSuggestionService.getCoordinatedByAddress(obj.location)
                    _ <- RealtyObjectRepository.updateInfo(
                      obj.id,
                      latitude = Some(coordsDto.lat),
                      longitude = Some(coordsDto.lon))
                } yield ()
            }
        } yield ()

}

object RealtyObjectServiceLive {
    lazy val layer: ULayer[RealtyObjectServiceLive] = ZLayer.succeed(RealtyObjectServiceLive())
}
