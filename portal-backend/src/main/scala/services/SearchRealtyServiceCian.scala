package services
import configuration.ApplicationConfig
import dto.integration.cian.CianSearchRequestDTO
import helpers.GeoHelper.GeoPolygon
import helpers.typed.Uri.syntax.stringToUriType
import io.netty.handler.codec.http.{HttpHeaderNames, HttpHeaderValues}
import io.netty.handler.ssl.{ApplicationProtocolConfig, SslContextBuilder}
import zhttp.http.{Body, Headers, Response}
import zhttp.http.Method.{GET, POST}
import zhttp.service.client.ClientSSLHandler.ClientSSLOptions
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.json.EncoderOps
import zio.{ULayer, ZIO, ZLayer}

import javax.net.ssl.TrustManagerFactory
import scala.jdk.CollectionConverters.IterableHasAsJava



final case class SearchRealtyServiceCian () extends SearchRealtyService {

    val config: ZIO[ApplicationConfig, Nothing, configuration.Cian] =
        zio.config.getConfig[ApplicationConfig].map(_.integration.cian)

    def searchRealtyInSquare(geoPolygon: GeoPolygon): ZIO[EventLoopGroup with ChannelFactory with ApplicationConfig, Throwable, String] = {
//        val query = s"ajax/map/roundabout/?currency=2&engine_version=2&offer_type=2&region=1&wp=1&room2=1&in_polygon%5B0%5D=${geoPolygon.bottomLeft},${geoPolygon.topLeft},${geoPolygon.bottomRight},${geoPolygon.topRight}"
        val query = s"search-offers/v2/search-offers-desktop/"
        val sslContext = SslContextBuilder.forClient()
            .protocols("TLSv1.3", "TLSv1.2")
            .build()
        val searchCoordinates = List(
          geoPolygon.bottomLeft.lonLatTuple,
            geoPolygon.topLeft.lonLatTuple,
            geoPolygon.bottomRight.lonLatTuple,
            geoPolygon.topRight.lonLatTuple
        )
        val queryBody = CianSearchRequestDTO.make(
            coordinates = searchCoordinates,
            rooms = List(1),
            totalAreaGte = 40,
            totalAreaLte = 60,
            balconiesGt = 1,
            floorGte = 1,
            floorLte = Some(20)
        )
        for {
            config <- config
            url = s"${config.url}" / query
            headers = Headers(
                HttpHeaderNames.USER_AGENT -> "PostmanRuntime/7.29.2"
            )
            _ <- zio.Console.printLine(s"send POST request on $url")
            response <- Client.request(
              url.raw,
              method = POST,
                ssl = ClientSSLOptions.CustomSSL(sslContext = sslContext),
              content = Body.fromCharSequence(queryBody.toJson),
              headers = headers
            )
            responseBody <- response.body.asString
        } yield responseBody
    }

}

object SearchRealtyServiceCian{
    lazy val layer: ULayer[SearchRealtyService] = ZLayer.succeed(SearchRealtyServiceCian())
}