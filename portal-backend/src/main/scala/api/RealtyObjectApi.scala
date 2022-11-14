package api
import dao.entities.realty.RealtyObjectId
import dao.repositories.integration.AnalogueObjectRepository
import dto.realty._
import exceptions._
import helpers.AuthHelper._
import helpers.HttpExceptionHandlers.{basicAuthExceptionHandler, bodyParsingExceptionHandler, lastResortHandler}
import services.{GeoSuggestionService, RealtyObjectService}
import zhttp.http._
import zio.ZIO
import zio.json.EncoderOps
import zio.stream.ZStream

import java.io.File

object RealtyObjectApi {

    val api = Http.collectZIO[Request] {

        /** Import RealtyObjects from xlsx-file */
        case req @ Method.PUT -> !! / "realty" / "objects" / "import" =>
            withUserContextZIO(req) { user =>
                RealtyObjectService.importFromXlsx(req.body.asStream, user.id)
            }.fold(
              importFromXlsxExceptionHandler,
              poolId => Response.json(poolId.toJson)
            )

        /** Export all RealtyObjects of User to xlsx */
        case req @ Method.GET -> !! / "realty" / "objects" / "export" =>
            withUserContextZIO(req) { user =>
                for {
                    tempFile <- RealtyObjectService.exportRealtyObjectsOfUserToXlsx(user)
                } yield tempFile
            }.fold(
              exportRealtyObjectsToXlsxExceptionsHandler,
              file => makeXlsxFileResponseFromFile(file)
            )

        /** Export all RealtyObjects of User to xlsx */
        case req @ Method.GET -> !! / "realty" / "objects" / "export" / poolId =>
            withUserContextZIO(req) { user =>
                for {
                    tempFile <- RealtyObjectService.exportPoolOfObjectsToXlsx(user, poolId)
                } yield tempFile
            }.fold(
              exportRealtyObjectsToXlsxExceptionsHandler,
              file => makeXlsxFileResponseFromFile(file)
            )

        case req @ Method.GET -> !! / "realty" / "objects" / "exportSome" =>
            withUserContextAndDtoZIO(req) { (user, dto: ExportSomeObjectsDTO) =>
                RealtyObjectService.exportSelectedObjectsToXlsx(dto, user.id)
            }.fold(
                exportRealtyObjectsToXlsxExceptionsHandler,
                file => makeXlsxFileResponseFromFile(file)
            )

        /** Get all RealtyObject created by attempting User */
        case req @ Method.GET -> !! / "realty" / "objects" =>
            withUserContextZIO(req) { user =>
                for {
                    objects <- RealtyObjectService.getRealtyObjectsForUser(user.id)
                    res <- ZIO.foreachPar(objects) { obj =>
                        for {
                            analogs <- AnalogueObjectRepository.getAllByRealtyObjectId(obj.id)
                            analogsDto = analogs.map(AnalogueObjectInfoDTO.fromEntity)
                            objInfo <- ZIO.attempt(RealtyObjectInfoDTO.fromEntityWithAnalogs(obj, analogsDto))
                        } yield objInfo
                    }
                } yield res
            }.fold(
              realtyObjectActionsBasicHandler,
              objectsList => Response.json(objectsList.toJson)
            )

        /** Create RealtyObject */
        case req @ Method.POST -> !! / "realty" / "objects" / "create" =>
            withUserContextAndDtoZIO(req) { (user, dto: CreateRealtyObjectDTO) =>
                for {
                    coordinates <- GeoSuggestionService.getCoordinatedByAddress(dto.location)
                    realtyObject <- RealtyObjectService.createRealtyObject(
                      dto,
                      user.id,
                      Some(coordinates.lat),
                      Some(coordinates.lon))
                } yield realtyObject
            }.fold(
              realtyObjectActionsBasicHandler,
              realtyObject => Response.json(RealtyObjectInfoDTO.fromEntity(realtyObject).toJson)
            )

        /** Get info about RealtyObject */
        case req @ Method.GET -> !! / "realty" / "objects" / objectId =>
            withUserContextZIO(req) { user =>
                RealtyObjectService.getRealtyObjectInfo(objectId, user.id)
            }.fold(
              realtyObjectActionsBasicHandler,
              dto => Response.json(dto.toJson)
            )

        /** Delete RealtyObject */
        case req @ Method.DELETE -> !! / "realty" / "objects" / objectId =>
            withUserContextZIO(req) { user =>
                for {
                    realtyId <- RealtyObjectId.fromString(objectId)
                    _ <- RealtyObjectService.deleteRealtyObject(realtyId, user.id)
                } yield ()
            }.fold(
              realtyObjectActionsBasicHandler,
              _ => Response.ok
            )

        /** Update RealtyObject's info */
        case req @ Method.PATCH -> !! / "realty" / "objects" =>
            withUserContextAndDtoZIO(req) { (user, dto: UpdateRealtyObjectDTO) =>
                for {
                    _ <- RealtyObjectService.updateRealtyObjectInfo(dto, user.id)
                } yield ()
            }.fold(
              realtyObjectActionsBasicHandler,
              _ => Response.ok
            )

        /** Calculate value for all objects on pool */
        case req @ Method.POST -> !! / "realty" / "objects" / "calculatePool" =>
            withUserContextAndDtoZIO(req) { (user, dto: CalculateObjectsInPoolDTO) =>
                RealtyObjectService.calculateAllInPool(dto.poolId, user.id, dto.withCorrections, 3)
            }.fold(
              realtyObjectActionsBasicHandler,
              _ => Response.ok
            )

        /** Calculate value for set of selected objects */
        case req @ Method.POST -> !! / "realty" / "objects" / "calculateSome" =>
            withUserContextAndDtoZIO(req) { (user, dto: CalculateValueOfSomeObjectsDTO) =>
                RealtyObjectService.calculateForSome(dto, user.id, 3)
            }.fold(
              realtyObjectActionsBasicHandler,
              _ => Response.ok
            )

    } @@ Middleware.debug

    private val importFromXlsxExceptionHandler: Throwable => Response =
        basicAuthExceptionHandler orElse [Throwable, Response] { case e: ExcelParsingException =>
            Response.text(s"invalid file format: ${e.getMessage}").setStatus(Status.BadRequest)
        } orElse lastResortHandler

    private val exportRealtyObjectsToXlsxExceptionsHandler: Throwable => Response =
        basicAuthExceptionHandler orElse [Throwable, Response] { case e: ExcelWritingException =>
            Response
                .text(s"Error occurred while exporting realty objects to xlsx file: ${e.getMessage}")
                .setStatus(Status.InternalServerError)
        } orElse lastResortHandler

    private val realtyObjectActionsBasicHandler: Throwable => Response =
        basicAuthExceptionHandler orElse bodyParsingExceptionHandler orElse [Throwable, Response] {
            case e: RealtyObjectNotFound =>
                Response
                    .text(s"Realty object with ${e.field} = ${e.value} not found")
                    .setStatus(Status.BadRequest)
        } orElse lastResortHandler

    private def makeXlsxFileResponseFromFile(file: File): Response =
        Response(body = Body.fromStream(ZStream.fromFile(file)))
            .addHeader(HeaderNames.contentDisposition, HeaderValues.attachment)
            .addHeader(
              HeaderNames.contentType,
              "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
}
