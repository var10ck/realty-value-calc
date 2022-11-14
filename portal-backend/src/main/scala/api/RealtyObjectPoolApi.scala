package api
import dao.repositories.realty.RealtyObjectPoolRepository
import dto.realty.{CreatePoolDTO, PoolInfoDTO}
import exceptions._
import helpers.AuthHelper._
import helpers.HttpExceptionHandlers.{basicAuthExceptionHandler, bodyParsingExceptionHandler, lastResortHandler}
import services.RealtyObjectPoolService
import zhttp.http._
import zio.ZIO
import zio.json.{DecoderOps, EncoderOps}

object RealtyObjectPoolApi {

    val api = Http.collectZIO[Request] {
        /** Create new RealtyObjectPool */
        case req @ Method.PUT -> !! / "realty" / "pool" =>
            withUserContextZIO(req) { user =>
                for {
                    body <- req.body.asString
                    dto <- ZIO
                        .fromEither(body.fromJson[CreatePoolDTO])
                        .orElseFail(BodyParsingException())
                    newPool <- RealtyObjectPoolRepository.create(dto.name, user.id)
                } yield newPool
            }.fold(
                realtyObjectsPoolExceptionHandler,
                newPool => Response.json(newPool.toJson)
            )

        case req @ Method.GET -> !! / "realty" / "pools" =>
            withUserContextZIO(req){user =>
              RealtyObjectPoolService.getAllOfUser(user.id)
            }.fold(
                realtyObjectsPoolExceptionHandler,
                pools => Response.json(pools.toJson)
            )

        /** Get RealtyObjectPool */
        case req @ Method.GET -> !! / "realty" / "pool" / poolId =>
            withUserContextZIO(req) { user =>
                RealtyObjectPoolService.get(poolId, user.id)
            }.fold(
                realtyObjectsPoolExceptionHandler,
                pool => Response.json(pool.toJson)
            )

    } @@ Middleware.debug

    private val realtyObjectsPoolExceptionHandler = basicAuthExceptionHandler orElse
        bodyParsingExceptionHandler orElse [Throwable, Response] {
        case e: exceptions.RealityObjectsPoolNotFound => Response.text(s"Pool of Realty objects with ${e.field} = ${e.value} not found")
        .setStatus(Status.BadRequest)
    } orElse lastResortHandler

}
