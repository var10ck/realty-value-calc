package api
import dao.entities.realty.RealtyObjectId
import dto.realty.{CreateRealtyObjectDTO, RealtyObjectCreatedDTO}
import exceptions.{HeaderNotSetException, NotEnoughRightsException, RealtyObjectNotFound, UserUnauthorizedException}
import helpers.AuthHelper._
import services.RealtyObjectService
import zhttp.http._
import zio.ZIO
import zio.json.{DecoderOps, EncoderOps}

object RealtyObjectApi {

    val api = Http.collectZIO[Request] {
        case req @ Method.PUT -> !! / "realty" / "objects" / "import" =>
            withUserContextZIO(req) { user =>
                RealtyObjectService.importFromXlsx(req.bodyAsStream, user.id)
            }.fold(
              basicAuthExceptionHandler,
              _ => Response.ok
            )

        case req @ Method.GET -> !! / "realty" / "objects" =>
            withUserContextZIO(req) { user =>
                RealtyObjectService.getRealtyObjectsForUser(user.id)
            }.fold(
              basicAuthExceptionHandler,
              objectsList => Response.json(objectsList.toJson)
            )

        case req @ Method.POST -> !! / "realty" / "objects" / "create" =>
            withUserContextZIO(req) { user =>
                for {
                    requestBody <- req.bodyAsString
                    dto <- ZIO
                        .fromEither(requestBody.fromJson[CreateRealtyObjectDTO])
                        .orElseFail(exceptions.BodyParsingException("CreateRealtyObjectDTO"))
                    realtyObject <- RealtyObjectService.createRealtyObject(dto, user.id)
                } yield realtyObject
            }.fold(
              basicAuthExceptionHandler,
              realtyObject => Response.json(RealtyObjectCreatedDTO.fromEntity(realtyObject).toJson)
            )

        case req @ Method.DELETE -> !! / "realty" / "objects" / objectId =>
            withUserContextZIO(req) { user =>
                for {
                    realtyId <- RealtyObjectId.fromString(objectId)
                    _ <- RealtyObjectService.deleteRealtyObject(realtyId, user.id)
                } yield ()
            }.fold(
              {
                  case _: HeaderNotSetException =>
                      Response.text("Header userSessionId is not set").setStatus(Status.Unauthorized)
                  case _: UserUnauthorizedException => Response.text("session not found").setStatus(Status.Unauthorized)
                  case e: RealtyObjectNotFound =>
                      Response
                          .text(s"Realty object with ${e.field} = ${e.value} not found")
                          .setStatus(Status.BadRequest)
                  case _: NotEnoughRightsException =>
                      Response.text("User is not author of object").setStatus(Status.BadRequest)
                  case _ => Response.text("Unknown exception").setStatus(Status.InternalServerError)
              },
              _ => Response.ok
            )
    } @@ Middleware.debug

    def basicAuthExceptionHandler: Throwable => Response = {
        case _: HeaderNotSetException =>
            Response.text("Header userSessionId is not set").setStatus(Status.Unauthorized)
        case _: UserUnauthorizedException => Response.text("session not found").setStatus(Status.Unauthorized)
        case _ => Response.text("Unknown exception").setStatus(Status.InternalServerError)
    }
}
