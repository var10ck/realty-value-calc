package api

import dao.entities.realty.RealtyObjectPoolId
import helpers.AuthHelper._
import helpers.HttpExceptionHandlers.{basicAuthExceptionHandler, bodyParsingExceptionHandler, lastResortHandler}
import services.AnalyticsService
import zhttp.http._
import zio.json.EncoderOps

object AnalyticsApi {

    val api = Http.collectZIO[Request] {

        /** Get default analytics data */
        case req @ Method.GET -> !! / "realty" / "analytics" / "default" =>
            withUserContextZIO(req) { user =>
                AnalyticsService.getDefaultAnalytics(user.id)
            }.fold(
                handler,
                data => Response.json(data.toJson)
            )

        case req @ Method.GET -> !! / "realty" / "analytics" / "avgPriceOfRealtyByRoomsNumberForPool" / poolId =>
            withUserContextZIO(req) { user =>
                for {
                    poolIdTyped <- RealtyObjectPoolId.fromString(poolId)
                    avgPriceByRoomsNumberForPool <- AnalyticsService.getAvgPriceOfRealtyByRoomsNumberForPool(poolIdTyped)
                } yield avgPriceByRoomsNumberForPool
            }.fold(
                handler,
                data => Response.json(data.toJson)
            )

    }

    val handler = basicAuthExceptionHandler orElse bodyParsingExceptionHandler orElse lastResortHandler

}
