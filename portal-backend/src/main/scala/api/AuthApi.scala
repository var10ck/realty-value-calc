package api
import dao.entities.auth.User
import dto.auth._
import exceptions.BodyParsingException
import services.AuthService
import zio.ZIO
import zio.json.{DecoderOps, EncoderOps}
import zhttp.http._

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
              _ => Response.status(Status.InternalServerError),
              _ => Response.ok
            )

        case req @ Method.GET -> !! / "auth" / "user" =>
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

    }

}
