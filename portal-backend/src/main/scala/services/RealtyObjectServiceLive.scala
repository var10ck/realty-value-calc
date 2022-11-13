package services
import dao.entities.auth.{User, UserId}
import dao.entities.integration.AnalogueObject
import dao.entities.realty.{RealtyObject, RealtyObjectId, RealtyObjectPoolId}
import dao.repositories.corrections.CorrectionNumericRepository
import dao.repositories.integration.AnalogueObjectRepository
import dao.repositories.realty.{RealtyObjectPoolRepository, RealtyObjectRepository}
import dto.realty.{AnalogueObjectInfoDTO, CalculateValueOfSomeObjectsDTO, CreateRealtyObjectDTO, ExportSomeObjectsDTO, RealtyObjectInfoDTO, UpdateRealtyObjectDTO}
import helpers.aggregatorParser.CianValuesTranslator.{segmentTranslate, wallMaterialTranslate}
import helpers.{CorrectionHelper, ExcelHelper, FileHelper, GeoHelper}
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.stream.{ZSink, ZStream}
import zio.{Console, Scope, ULayer, ZIO, ZLayer, durationInt}

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
            tempFile <- writeObjectsInXlsxFile(realtyObjects)
        } yield tempFile

    /** Getting RealtyObjects added by User and belong to RealtyObjectsPool with poolId, then writes it into xlsx-file
      */
    override def exportPoolOfObjectsToXlsx(user: User, poolId: String)
        : ZIO[Any with Scope with DataSource with RealtyObjectRepository with RealtyObjectService, Throwable, File] =
        for {
            poolIdTyped <- RealtyObjectPoolId.fromString(poolId)
            poolObjects <- RealtyObjectRepository.getAllInPoolByUser(poolIdTyped, user.id)
            tempFile <- writeObjectsInXlsxFile(poolObjects)
        } yield tempFile

    private def writeObjectsInXlsxFile(objects: List[RealtyObject]): ZIO[Any with Scope, Throwable, File] =
        for{
            tempFile <- FileHelper.makeTempFileZIO("upload", ".xlsx")
            _ <- ZIO.debug(s"Created temporary file \"${tempFile.getAbsolutePath}\"")
            inputStream <- FileHelper.makeFileOutputStream(tempFile)
            _ <- ExcelHelper.transformObjectsToXlsx(inputStream, objects) <* ZIO.from(tempFile.delete()).delay(15.minutes).forkDaemon
            _ <- ZIO.debug(s"Wrote ${objects.length} objects $objects to file ${tempFile.getAbsolutePath}")
        } yield tempFile

    override def exportSelectedObjectsToXlsx(dto: ExportSomeObjectsDTO, userId: UserId)
    : ZIO[Any with Scope with DataSource with RealtyObjectRepository, Throwable, File] =
        for {
            objects <- ZIO.foreach(dto.objectIds){objectId => RealtyObjectRepository.get(objectId)}
                .map(_.flatten.filter(_.addedByUserId == userId))
            tempFile <- writeObjectsInXlsxFile(objects)
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
    override def getRealtyObjectInfo(realtyObjectId: String, userId: UserId)
        : ZIO[DataSource with RealtyObjectRepository with AnalogueObjectRepository, Throwable, RealtyObjectInfoDTO] =
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
    override def calculateAllInPool(
        poolId: String,
        userId: UserId,
        withCorrections: Boolean,
        numPages: Int,
        limitOfAnalogs: Int = 20): ZIO[
      DataSource
          with RealtyObjectRepository with AnalogueObjectRepository with EventLoopGroup with ChannelFactory
          with configuration.ApplicationConfig with SearchRealtyService with CorrectionNumericRepository,
      Throwable,
      Unit] =
        for {
            typedPoolId <- RealtyObjectPoolId.fromString(poolId)

            realtyObjectsToCalculate <- RealtyObjectRepository
                .getAllInPoolByUser(typedPoolId, userId)
                .map(_.filter(o => o.latitude.isDefined && o.longitude.isDefined))
            _ <- calculateValueOfObjects(realtyObjectsToCalculate, withCorrections, numPages, limitOfAnalogs).forkDaemon
        } yield ()

    /** Calculates value of some RealtyObjects */
    override def calculateForSome(
        dto: CalculateValueOfSomeObjectsDTO,
        userId: UserId,
        numPages: Int,
        limitOfAnalogs: Int = 20): ZIO[
      DataSource
          with RealtyObjectRepository with AnalogueObjectRepository with EventLoopGroup with ChannelFactory
          with configuration.ApplicationConfig with SearchRealtyService with CorrectionNumericRepository,
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

            _ <- calculateValueOfObjects(
              realtyObjectsToCalculate,
              dto.withCorrections,
              numPages,
              limitOfAnalogs).forkDaemon
        } yield ()

    /** Calculates value of all RealtyObject in list */
    private def calculateValueOfObjects(
        realtyObjects: List[RealtyObject],
        withCorrections: Boolean,
        numPages: Int,
        limitOfAnalogs: Int): ZIO[
      DataSource
          with RealtyObjectRepository with AnalogueObjectRepository with EventLoopGroup with ChannelFactory
          with configuration.ApplicationConfig with SearchRealtyService with CorrectionNumericRepository,
      Throwable,
      Unit] =
        ZIO.foreach(realtyObjects)(calculateValueOfSingleObject(_, withCorrections, numPages, limitOfAnalogs)).unit

    /** Calculates value of RealtyObject using Cian API */
    private def calculateValueOfSingleObject(
        objToCalculate: RealtyObject,
        withCorrections: Boolean,
        numPages: Int,
        limitOfAnalogs: Int): ZIO[
      DataSource
          with RealtyObjectRepository with AnalogueObjectRepository with EventLoopGroup with ChannelFactory
          with configuration.ApplicationConfig with SearchRealtyService with CorrectionNumericRepository,
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
            allCorrections <- CorrectionNumericRepository.getAll
            correctionFunctions <- ZIO.attempt(allCorrections.map(CorrectionHelper.correctionNumericToFunction))
            analoguesOfObject <- SearchRealtyService.searchRealtyInSquare(
              polygon,
              rooms = rooms,
              totalAreaGte = 10,
              totalAreaLte = objToCalculate.totalArea.toInt + 50,
              floorGte = 1,
              floorLte = 110,
              pages = numPages)

            analoguesOfObjectFiltered = analoguesOfObject.view
                .filter(_.material.contains(wallMaterialTranslate(objToCalculate.wallMaterial)))
//                .filter(_.coordinates.floors.contains(objToCalculate.floorCount)) // possibly problem filter by exact floor number
                .filter(_.apartmentsType.contains(segmentTranslate(objToCalculate.segment)))
                .filter(a => a.price.isDefined && a.area.isDefined)
                .take(limitOfAnalogs)
                .toList
            _ <- ZIO.debug(analoguesOfObjectFiltered.length)
            _ <- ZIO.foreach(analoguesOfObjectFiltered)(o => ZIO.debug(o))
            averageCalculatedValueOfSquareMeterOfAnalogs =
                if (withCorrections) {
                    analoguesOfObjectFiltered.zipWithIndex.map { case (apartment, index) =>
                        val priceOfSqMeter = apartment.price.get / apartment.area.get
                        val correctedPrices = correctionFunctions.map(f => f(objToCalculate, apartment)(priceOfSqMeter))
                        val correctedPricesFiltered = correctedPrices.filter(_ != priceOfSqMeter)
                        (correctedPricesFiltered.sum + priceOfSqMeter) / (correctedPricesFiltered.length + 1)
                    }.sum / analoguesOfObjectFiltered.length
                } else {
                    analoguesOfObjectFiltered.zipWithIndex.map { case (apartment, index) =>
                        val priceOfSqMeter = apartment.price.get / apartment.area.get
                        priceOfSqMeter
                    }.sum / analoguesOfObjectFiltered.length
                }
            objToCalculatePrice = averageCalculatedValueOfSquareMeterOfAnalogs * objToCalculate.totalArea
            _ <- RealtyObjectRepository.updateInfo(
              objToCalculate.id,
              calculatedValue = Some(objToCalculatePrice.toLong))
            _ <- ZIO
                .foreach(analoguesOfObjectFiltered) { analogue =>
                    for {
                        analogueObject <- AnalogueObject.fromApartment(analogue, objToCalculate.id)
                        _ <- AnalogueObjectRepository.insert(analogueObject).ignore
                    } yield ()
                }
                .forkDaemon
        } yield ()

}

object RealtyObjectServiceLive {
    lazy val layer: ULayer[RealtyObjectServiceLive] = ZLayer.succeed(RealtyObjectServiceLive())
}
