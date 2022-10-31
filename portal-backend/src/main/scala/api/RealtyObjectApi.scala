package api
import dao.entities.realty.RealtyObjectId
import dto.realty.{CreateRealtyObjectDTO, RealtyObjectCreatedDTO, UpdateRealtyObjectDTO}
import exceptions._
import helpers.AuthHelper._
import helpers.HttpExceptionHandlers.{basicAuthExceptionHandler, bodyParsingExceptionHandler, lastResortHandler}
import services.RealtyObjectService
import zhttp.http._
import zio.ZIO
import zio.json.{DecoderOps, EncoderOps}
import zio.stream.ZStream

import java.io.File

object RealtyObjectApi {

    val api = Http.collectZIO[Request] {

        /** Import RealtyObjects from xlsx-file */
        case req @ Method.PUT -> !! / "realty" / "objects" / "import" =>
            withUserContextZIO(req) { user =>
                RealtyObjectService.importFromXlsx(req.bodyAsStream, user.id)
            }.fold(
              importFromXlsxExceptionHandler,
              _ => Response.ok
            )

        case req @ Method.GET -> !! / "realty" / "objects" / "export" =>
            withUserContextZIO(req) { user =>
                for {
                    tempFile <- RealtyObjectService.exportRealtyObjectsOfUserToXlsx(user)
                } yield tempFile
            }.fold(
              exportRealtyObjectsToXlsxExceptionsHandler,
              file => Response(data = HttpData.fromStream(ZStream.fromFile(file)))
                  .addHeader(HeaderNames.contentDisposition, HeaderValues.attachment)
                  .addHeader(HeaderNames.contentType, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            )

        /** Get all RealtyObject created by attempting User */
        case req @ Method.GET -> !! / "realty" / "objects" =>
            withUserContextZIO(req) { user =>
                RealtyObjectService.getRealtyObjectsForUser(user.id)
            }.fold(
              authExceptionHandler,
              objectsList => Response.json(objectsList.toJson)
            )

        /** Create RealtyObject */
        case req @ Method.POST -> !! / "realty" / "objects" / "create" =>
            withUserContextZIO(req) { user =>
                for {
                    requestBody <- req.bodyAsString
                    dto <- ZIO
                        .fromEither(requestBody.fromJson[CreateRealtyObjectDTO])
                        .orElseFail(BodyParsingException("CreateRealtyObjectDTO"))
                    realtyObject <- RealtyObjectService.createRealtyObject(dto, user.id)
                } yield realtyObject
            }.fold(
              basicAuthExceptionHandler,
              realtyObject => Response.json(RealtyObjectCreatedDTO.fromEntity(realtyObject).toJson)
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
                    body <- req.bodyAsString
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

    val exportApiTest = Http.collectHttp[Request]{
        case req @ Method.GET -> !! / "realty" / "objects" / "testfile" =>
            Http.fromFile(new File("C:\\Users\\nguen\\Projects\\scala\\realty-value-calc\\testData\\testData.xlsx"))
    }

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
