package services
import configuration.ApplicationConfig
import helpers.GeoHelper.GeoPolygon
import helpers.typed.Uri.syntax.stringToUriType
import io.netty.handler.codec.http.{HttpHeaderNames, HttpHeaderValues}
import io.netty.handler.ssl.{ApplicationProtocolConfig, SslContextBuilder}
import zhttp.http.{Headers, Response}
import zhttp.http.Method.{GET, POST}
import zhttp.service.client.ClientSSLHandler.ClientSSLOptions
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.{ULayer, ZIO, ZLayer}

import javax.net.ssl.TrustManagerFactory
import scala.jdk.CollectionConverters.IterableHasAsJava



final case class SearchRealtyServiceCian () extends SearchRealtyService {

    val config: ZIO[ApplicationConfig, Nothing, configuration.Cian] =
        zio.config.getConfig[ApplicationConfig].map(_.integration.cian)

    def searchRealtyInSquare(geoPolygon: GeoPolygon): ZIO[EventLoopGroup with ChannelFactory with ApplicationConfig, Throwable, String] = {
        val query = s"ajax/map/roundabout/?currency=2&engine_version=2&offer_type=2&region=1&wp=1&room2=1&in_polygon%5B0%5D=${geoPolygon.bottomLeft},${geoPolygon.topLeft},${geoPolygon.bottomRight},${geoPolygon.topRight}"
        val sslContext = SslContextBuilder.forClient()
//            .applicationProtocolConfig(ApplicationProtocolConfig.Protocol.)
            .protocols("TLSv1.3", "TLSv1.2")
//            .ciphers(List("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA").asJava)
            .build()
        for {
            config <- config
            url = s"${config.url}" / query
            headers = Headers(
                HttpHeaderNames.USER_AGENT -> "PostmanRuntime/7.29.2"
            )
            _ <- zio.Console.printLine(s"send get request on $url")
            response <- Client.request(
              url.raw,
              method = GET,
                ssl = ClientSSLOptions.CustomSSL(sslContext = sslContext),
              headers = headers
            )
            responseBody <- response.body.asString
        } yield responseBody
    }

}

object SearchRealtyServiceCian{
    lazy val layer: ULayer[SearchRealtyService] = ZLayer.succeed(SearchRealtyServiceCian())
}