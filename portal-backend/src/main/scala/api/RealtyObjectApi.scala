package api
import dao.entities.realty.RealtyObjectId
import dto.realty.{CreateRealtyObjectDTO, RealtyObjectInfoDTO, UpdateRealtyObjectDTO}
import exceptions._
import helpers.AuthHelper._
import helpers.HttpExceptionHandlers.{basicAuthExceptionHandler, bodyParsingExceptionHandler, lastResortHandler}
import services.{GeoSuggestionService, RealtyObjectService}
import zhttp.http._
import zio.ZIO
import zio.json.{DecoderOps, EncoderOps}
import zio.stream.ZStream

object RealtyObjectApi {

    val api = Http.collectZIO[Request] {

        /** Import RealtyObjects from xlsx-file */
        case req @ Method.PUT -> !! / "realty" / "objects" / "import" =>
            withUserContextZIO(req) { user =>
                RealtyObjectService.importFromXlsx(req.body.asStream, user.id)
            }.fold(
              importFromXlsxExceptionHandler,
              _ => Response.ok
            )

        /** Export all RealtyObjects of User to xlsx */
        case req @ Method.GET -> !! / "realty" / "objects" / "export" =>
            withUserContextZIO(req) { user =>
                for {
                    tempFile <- RealtyObjectService.exportRealtyObjectsOfUserToXlsx(user)
                } yield tempFile
            }.fold(
              exportRealtyObjectsToXlsxExceptionsHandler,
              file =>
                  Response(body = Body.fromStream(ZStream.fromFile(file)))
                      .addHeader(HeaderNames.contentDisposition, HeaderValues.attachment)
                      .addHeader(
                        HeaderNames.contentType,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                      )
            )

        /** Export all RealtyObjects of User to xlsx */
        case req @ Method.GET -> !! / "realty" / "objects" / "export" / poolId=>
            withUserContextZIO(req) { user =>
                for {
                    tempFile <- RealtyObjectService.exportPoolOfObjectsToXlsx(user, poolId)
                } yield tempFile
            }.fold(
                exportRealtyObjectsToXlsxExceptionsHandler,
                file =>
                    Response(body = Body.fromStream(ZStream.fromFile(file)))
                        .addHeader(HeaderNames.contentDisposition, HeaderValues.attachment)
                        .addHeader(
                            HeaderNames.contentType,
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
            )

//        case

        /** Get all RealtyObject created by attempting User */
        case req @ Method.GET -> !! / "realty" / "objects" =>
            withUserContextZIO(req) { user =>
                for {
                    objects <- RealtyObjectService.getRealtyObjectsForUser(user.id)
                    res <- ZIO.foreach(objects) { obj => ZIO.from(RealtyObjectInfoDTO.fromEntity(obj)) }
                } yield res
            }.fold(
              authExceptionHandler,
              objectsList => Response.json(objectsList.toJson)
            )

        /** Create RealtyObject */
        case req @ Method.POST -> !! / "realty" / "objects" / "create" =>
            withUserContextZIO(req) { user =>
                for {
                    requestBody <- req.body.asString
                    dto <- ZIO
                        .fromEither(requestBody.fromJson[CreateRealtyObjectDTO])
                        .orElseFail(BodyParsingException("CreateRealtyObjectDTO"))
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
            withUserContextZIO(req) { user =>
                for {
                    body <- req.body.asString
                    dto <- ZIO
                        .fromEither(body.fromJson[UpdateRealtyObjectDTO])
                        .orElseFail(BodyParsingException("UpdateRealtyObjectDTO"))
                    _ <- RealtyObjectService.updateRealtyObjectInfo(dto, user.id)
                } yield ()
            }.fold(
              realtyObjectActionsBasicHandler,
              _ => Response.ok
            )

    } @@ Middleware.debug

    private val authExceptionHandler: Throwable => Response = basicAuthExceptionHandler orElse lastResortHandler

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
}
