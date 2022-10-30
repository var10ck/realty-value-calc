package services
import dao.entities.auth.UserId
import dao.entities.realty.{RealtyObject, RealtyObjectId}
import dao.repositories.realty.RealtyObjectRepository
import dto.realty.{CreateRealtyObjectDTO, DeleteRealtyObjectDTO, RealtyObjectInfoDTO}
import helpers.{ExcelHelper, FileHelper}
import zio.{Scope, ULayer, ZIO, ZLayer}
import zio.stream.{ZSink, ZStream}

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
    override def importFromXlsx(
        bodyStream: ZStream[Any, Throwable, Byte],
        userId: UserId): ZIO[DataSource with RealtyObjectRepository with Any with Scope, Throwable, Unit] =
        for {
            tempFile <- FileHelper.makeTempFile("upload", ".xlsx")
            bytesWritten <- bodyStream.run(ZSink.fromFile(tempFile))
            _ <- zio.Console.printLine(s"created temp file $tempFile and $bytesWritten written")
            _ <- transmitXlsxObjectsToDatabase(tempFile, userId)
        } yield ()

    /** takes xlsx-file, transforms it to RealtyObject entities and writes into database
      * @param file
      *   xlsx-file with records about realty objects
      * @param userId
      *   user, importing these objects
      */
    private def transmitXlsxObjectsToDatabase(
        file: File,
        userId: UserId): ZIO[DataSource with RealtyObjectRepository with Any with Scope, Throwable, Unit] =
        for {
            fileInputStream <- ZIO.acquireRelease(ZIO.from(new FileInputStream(file)))(fis => ZIO.succeed(fis.close()))
            realtyObjectsExcelDtoList <- ExcelHelper.transformXlsToObject(fileInputStream) <*
                ZIO.from(file.delete())
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
                      addedByUserId = userId
                    )
            }
        } yield ()

    /** Creates RealtyObject and writes into database */
    override def createRealtyObject(
        dto: CreateRealtyObjectDTO,
        userId: UserId): ZIO[DataSource with RealtyObjectRepository, SQLException, RealtyObject] =
        RealtyObjectRepository.create(
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
          userId
        )

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
        } yield RealtyObjectInfoDTO(
          obj.id,
          obj.location,
          obj.roomsNumber,
          obj.segment,
          obj.floorCount,
          obj.wallMaterial,
          obj.floorNumber,
          obj.totalArea,
          obj.kitchenArea,
          obj.gotBalcony,
          obj.condition,
          obj.distanceFromMetro,
          obj.calculatedValue,
          obj.createdAt,
          obj.updatedAt
        )

}

object RealtyObjectServiceLive {
    lazy val layer: ULayer[RealtyObjectServiceLive] = ZLayer.succeed(RealtyObjectServiceLive())
}
