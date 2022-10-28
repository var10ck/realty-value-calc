package api
import dao.entities.auth.User
import dto.auth._
import exceptions.{BodyParsingException, DataValidationException}
import services.AuthService
import zio.ZIO
import zio.json.{DecoderOps, EncoderOps}
import zhttp.http._

import java.time.format.DateTimeParseException

object AuthApi {

    val api = Http.collectZIO[Request] {
        case req @ Method.POST -> !! / "auth" / "register" =>
            (for {
                requestBody <- req.bodyAsString
                dto <- ZIO
                    .fromEither(requestBody.fromJson[CreateUserDTO])
                    .orElseFail(BodyParsingException("CreateUserDTO"))
                createdUser <- AuthService.createUser(dto)
            } yield createdUser).fold(
              {
                  case _: DateTimeParseException =>
                      Response
                          .text("Date format is invalid. Should be in format - dd.MM.yyyy")
                          .setStatus(Status.BadRequest)
                  case DataValidationException(message) => Response.text(message).setStatus(Status.BadRequest)
                  case _ => Response.status(Status.InternalServerError)
              },
//              err => Response.text(err.toString),
              user => Response.json(user.toJson)
            )

        case req @ Method.POST -> !! / "auth" / "user" =>
            for {
                requestBody <- req.bodyAsString
                dto <- ZIO.fromEither(requestBody.fromJson[AuthUserDTO]).orElseFail(BodyParsingException("AuthUserDTO"))
                sessionOpt <- AuthService.authUser(dto)
            } yield sessionOpt match {
                case Some(session) =>
//                    Response.json(SessionCreatedDTO(session).toJson)
                    Response.ok.setHeaders(Headers.setCookie(Cookie("userSessionId", session.id.toString)))
                case None => Response.status(Status.BadRequest)
            }

        case req @ Method.DELETE -> !! / "auth" / "user" / userId =>
            AuthService.deleteUser(userId)
            .fold(
//                err => Response.text(err.toString),
              _ => Response.status(Status.InternalServerError),
              _ => Response.ok
            )

    } @@ Middleware.debug

}
