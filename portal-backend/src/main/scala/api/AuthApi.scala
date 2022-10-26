package api
import dao.entities.auth.User
import dto.auth._
import exceptions.BodyParsingException
import services.AuthService
import zio.ZIO
import zio.json.DecoderOps
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
              failure => Response.status(Status.InternalServerError),
              success => Response.ok
            )

        case req @ Method.GET -> !! / "auth" / "user" =>
            (for {
                requestBody <- req.bodyAsString
                dto <- ZIO.fromEither(requestBody.fromJson[AuthUserDTO]).orElseFail(BodyParsingException("AuthUserDTO"))
                session <- AuthService.authUser(dto)
            } yield session).fold(
              failure => Response.status(Status.BadRequest),
              success => Response.ok
            )

    }

}
