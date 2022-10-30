package helpers
import exceptions._
import zhttp.http.{Response, Status}

object HttpExceptionHandlers {

    val basicAuthExceptionHandler: Throwable => Response = {
        case _: HeaderNotSetException =>
            Response.text("Header userSessionId is not set").setStatus(Status.Unauthorized)
        case _: UserUnauthorizedException => Response.text("session not found").setStatus(Status.BadRequest)
        case _ => Response.text("Unknown exception").setStatus(Status.InternalServerError)
    }

}
