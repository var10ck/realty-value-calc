package helpers
import dao.entities.auth.{User, UserSessionId}
import dao.repositories.auth.{UserRepository, UserSessionRepository}
import zhttp.http.{Request, Response, Status}
import zio.ZIO

import javax.sql.DataSource

object AuthHelper {

    def withUserContext(request: Request)(f: User => Response ): ZIO[DataSource with UserSessionRepository, Throwable, Response] =
        for{
            sessionId <- ZIO.fromOption(request.headers.headerValue("userSessionId"))
                    .orElseFail(exceptions.HeaderNotSetException("userSessionId"))
            userSessionId <- UserSessionId.fromString(sessionId)
            userOpt <- UserSessionRepository.getUser(userSessionId)
        }  yield userOpt match {
            case Some(user) => f(user)
            case None => Response.status(Status.Unauthorized)
        }

    def withUserContextZIO[R,A](request: Request)(
        f: User => ZIO[R,Throwable,A]): ZIO[R with DataSource with UserSessionRepository, Throwable, A] =
        for {
            sessionId <- ZIO
                .fromOption(request.headers.headerValue("userSessionId"))
                .orElseFail(exceptions.HeaderNotSetException("userSessionId"))
            userSessionId <- UserSessionId.fromString(sessionId)
            userOpt <- UserSessionRepository.getUser(userSessionId)
            user <- ZIO.fromOption(userOpt).orElseFail(exceptions.UserUnauthorizedException())
            result <- f(user)
        } yield result

}
