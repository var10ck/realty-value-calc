package services
import dao.entities.auth.{User, UserId}
import dao.entities.integration.AnalogueObject
import dao.entities.realty.{RealtyObject, RealtyObjectId, RealtyObjectPoolId}
import dao.repositories.integration.AnalogueObjectRepository
import dao.repositories.realty.{RealtyObjectPoolRepository, RealtyObjectRepository}
import dto.realty.{AnalogueObjectInfoDTO, CalculateValueOfSomeObjectsDTO, CreateRealtyObjectDTO, RealtyObjectInfoDTO, UpdateRealtyObjectDTO}
import helpers.{ExcelHelper, FileHelper, GeoHelper}
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.stream.{ZSink, ZStream}
import zio.{Console, Scope, ULayer, ZIO, ZLayer}

import java.io.File
import java.sql.SQLException
import javax.sql.DataSource

//noinspection SimplifyForeachInspection
final case class RealtyObjectServiceLive() extends RealtyObjectService {

    /** Takes ZStream, containing xlsx-file, converts xlsx rows to RealtyObject entities and writes them in database.
      * Executing insertion in database in new fiber.
      * @param bodyStream
      *   http body containing binary-encoded xlsx file
      * @param userId
      *   user that uploads file
      */
    override def importFromXlsx(bodyStream: ZStream[Any, Throwable, Byte], userId: UserId): ZIO[
      DataSource
          with RealtyObjectPoolRepository with RealtyObjectRepository with EventLoopGroup with ChannelFactory
          with configuration.ApplicationConfig with GeoSuggestionService with Any with Scope,
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
    private def transmitXlsxObjectsToDatabase(file: File, userId: UserId): ZIO[
      DataSource with RealtyObjectPoolRepository with RealtyObjectRepository with Any with Scope,
      Throwable,
      RealtyObjectPoolId] =
        for {
            fileInputStream <- FileHelper.makeFileInputStream(file)
            realtyObjectsExcelDtoList <- ExcelHelper.transformXlsxToObject(fileInputStream) <*
                ZIO.from(file.delete())
            _ <- Console.printLine(s"objects imported from ${file.getAbsolutePath}")
            pool <- RealtyObjectPoolRepository.create(None, userId).mapError(e => new Throwable(e.getMessage))
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
    override def getRealtyObjectInfo(
        realtyObjectId: String,
        userId: UserId): ZIO[DataSource with RealtyObjectRepository with AnalogueObjectRepository, Throwable, RealtyObjectInfoDTO] =
        for {
            id <- RealtyObjectId.fromString(realtyObjectId)
            realtyObjectOpt <- RealtyObjectRepository.get(id)
            entity <- ZIO.fromOption(realtyObjectOpt).orElseFail(exceptions.RealtyObjectNotFound("id", realtyObjectId))
            analogsOfObject <- AnalogueObjectRepository.getAllByRealtyObjectId(entity.id)
            analogsDtoList = analogsOfObject.map(AnalogueObjectInfoDTO.fromEntity)

            obj <- ZIO.ifZIO(ZIO.succeed(entity.addedByUserId == userId))(
              ZIO.succeed(entity),
              ZIO.fail(exceptions.NotEnoughRightsException("User is not author of object"))
            )
        } yield RealtyObjectInfoDTO.fromEntityWithAnalogs(obj, analogsDtoList)

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
            dto.calculatedValue,
            dto.poolId,
            dto.latitude,
            dto.longitude
          ),
          ZIO.fail(exceptions.NotEnoughRightsException("Realty object was added by another user"))
        )
    } yield ()

    /** Get all objects with unfilled coordinates and fill them. Using GeoSuggestionService to get latitude and
      * longitude by address in human-readable representation. Should be used as background task.
      */
    override def fillCoordinatesOnAllRealtyObjects: ZIO[
      DataSource
          with RealtyObjectRepository with EventLoopGroup with ChannelFactory with configuration.ApplicationConfig
          with GeoSuggestionService,
      Throwable,
      Unit] =
        for {
            objectsToFill <- RealtyObjectRepository.getAllWithoutCoordinates
            _ <- ZIO.foreach(objectsToFill) { obj =>
                for {
                    coordsDto <- GeoSuggestionService.getCoordinatedByAddress(obj.location)
                    _ <- RealtyObjectRepository.updateInfo(
                      obj.id,
                      latitude = Some(coordsDto.lat),
                      longitude = Some(coordsDto.lon))
                } yield ()
            }
        } yield ()

    /** Calculates market value of all objects in pool */
    override def calculateAllInPool(poolId: String, userId: UserId, withCorrections: Boolean, numPages: Int): ZIO[
      DataSource
          with RealtyObjectRepository with AnalogueObjectRepository with EventLoopGroup with ChannelFactory
          with configuration.ApplicationConfig with SearchRealtyService,
      Throwable,
      Unit] =
        for {
            typedPoolId <- RealtyObjectPoolId.fromString(poolId)

            realtyObjectsToCalculate <- RealtyObjectRepository
                .getAllInPoolByUser(typedPoolId, userId)
                .map(_.filter(o => o.latitude.isDefined && o.longitude.isDefined))
            _ <- calculateValueOfObjects(realtyObjectsToCalculate, withCorrections, numPages).forkDaemon
        } yield ()

    /** Calculates value of some RealtyObjects */
    override def calculateForSome(
        dto: CalculateValueOfSomeObjectsDTO,
        userId: UserId,
        withCorrections: Boolean,
        numPages: Int): ZIO[
      DataSource
          with RealtyObjectRepository with AnalogueObjectRepository with EventLoopGroup with ChannelFactory
          with configuration.ApplicationConfig with SearchRealtyService,
      Throwable,
      Unit] =
        for {
            typedObjectIds <- ZIO.foreach(dto.objectsIds)(RealtyObjectId.fromString)
            realtyObjectsToCalculate <- ZIO
                .foreach(typedObjectIds) { id =>
                    for {
                        objOpt <- RealtyObjectRepository.get(id)
                        realtyObject <- ZIO
                            .fromOption(objOpt)
                            .orElseFail(exceptions.RealtyObjectNotFound("id", id.id.toString))
                    } yield realtyObject
                }
                .map(_.filter(o => o.latitude.isDefined && o.longitude.isDefined))

            _ <- calculateValueOfObjects(realtyObjectsToCalculate, withCorrections, numPages).forkDaemon
        } yield ()

    /** Calculates value of all RealtyObject in list */
    private def calculateValueOfObjects(
        realtyObjects: List[RealtyObject],
        withCorrections: Boolean,
        numPages: Int): ZIO[
      DataSource
          with RealtyObjectRepository with AnalogueObjectRepository with EventLoopGroup with ChannelFactory
          with configuration.ApplicationConfig with SearchRealtyService,
      Throwable,
      Unit] =
        ZIO.foreach(realtyObjects)(calculateValueOfSingleObject(_, withCorrections, numPages)).unit

    /** Calculates value of RealtyObject using Cian API */
    private def calculateValueOfSingleObject(
        objToCalculate: RealtyObject,
        withCorrections: Boolean,
        numPages: Int): ZIO[
      DataSource
          with RealtyObjectRepository with AnalogueObjectRepository with EventLoopGroup with ChannelFactory
          with configuration.ApplicationConfig with SearchRealtyService,
      Throwable,
      Unit] =
        for {
            polygon <- ZIO.from(
              GeoHelper
                  .makeSquareAroundLocation(objToCalculate.latitude.get, objToCalculate.longitude.get, 1000))
            rooms =
//                List(objToCalculate.roomsNumber)
                if (objToCalculate.roomsNumber >= 4)
                    List(objToCalculate.roomsNumber - 1, objToCalculate.roomsNumber, objToCalculate.roomsNumber + 1)
                else List(objToCalculate.roomsNumber)
            analoguesOfObject <- SearchRealtyService.searchRealtyInSquare(
              polygon,
              rooms = rooms,
              totalAreaGte = 15,
              totalAreaLte = objToCalculate.totalArea.toInt + 50,
              floorGte = 1,
              floorLte = 80,
              pages = numPages)
            //
            _ <- ZIO
                .foreach(analoguesOfObject) { analogue =>
                    for {
                        analogueObject <- AnalogueObject.fromApartment(analogue, objToCalculate.id)
                        _ <- AnalogueObjectRepository.insert(analogueObject)
                    } yield ()
                }
                .forkDaemon
            _ <- zio.Console.printLine(analoguesOfObject.length) *> ZIO.attempt(analoguesOfObject)
            analoguesOfObjectFiltered = analoguesOfObject.view
                .filter(_.material.contains(wallMaterialTranslate(objToCalculate.wallMaterial)))
//                .filter(_.coordinates.floors.contains(objToCalculate.floorCount)) // possibly problem filter by exact floor number
                .filter(_.apartmentsType.contains(segmentTranslate(objToCalculate.segment)))
                .filter(a => a.price.isDefined && a.area.isDefined)
                .toList
            _ <- zio.Console.printLine(analoguesOfObjectFiltered)
            _ <- zio.Console.printLine(analoguesOfObjectFiltered.length) *> ZIO.attempt(analoguesOfObjectFiltered)
            averageCalculatedValueOfSquareMeterOfAnalogs = analoguesOfObjectFiltered.map { o =>
                val priceOfSqMeter = o.price.get / o.area.get
                //                          val correctedPrice =
                priceOfSqMeter
            }.sum / analoguesOfObjectFiltered.length
            objToCalculatePrice = averageCalculatedValueOfSquareMeterOfAnalogs * objToCalculate.totalArea
            _ <- RealtyObjectRepository.updateInfo(
              objToCalculate.id,
              calculatedValue = Some(objToCalculatePrice.toLong))
        } yield ()

    private def wallMaterialTranslate(s: String): String = Map(
      "кирпич" -> "brick",
      "монолит" -> "monolith",
      "панель" -> "panel",
      "смешанный" -> "monolithBrick"
    )(s.toLowerCase)

    private def segmentTranslate(s: String): String = Map(
      "новостройка" -> "newBuildingFlatSale",
      "современное жилье" -> "newBuildingFlatSale",
      "старый жилой фонд" -> "flatSale"
    )(s.toLowerCase)

}

object RealtyObjectServiceLive {
    lazy val layer: ULayer[RealtyObjectServiceLive] = ZLayer.succeed(RealtyObjectServiceLive())
}
