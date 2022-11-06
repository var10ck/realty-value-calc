package services
import configuration.ApplicationConfig
import dto.integration.cian.{Apartment, CianJsonQuery, CianSearchRequestDTO}
import helpers.GeoHelper.GeoPolygon
import helpers.aggregatorParser.CianParser
import helpers.typed.Uri
import helpers.typed.Uri.syntax.stringToUriType
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.ssl.{SslContext, SslContextBuilder}
import zhttp.http.Method.POST
import zhttp.http.{Body, Headers, Response}
import zhttp.service.client.ClientSSLHandler.ClientSSLOptions
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.json.EncoderOps
import zio.{ULayer, ZIO, ZLayer}

final case class SearchRealtyServiceCian() extends SearchRealtyService {

    val config: ZIO[ApplicationConfig, Nothing, configuration.Cian] =
        zio.config.getConfig[ApplicationConfig].map(_.integration.cian)

    def makeRequest(
        uri: Uri,
        sslContext: SslContext,
        queryBody: CianJsonQuery,
        headers: Headers): ZIO[EventLoopGroup with ChannelFactory, Throwable, Response] = Client.request(
      uri.raw,
      method = POST,
      ssl = ClientSSLOptions.CustomSSL(sslContext = sslContext),
      content = Body.fromCharSequence(queryBody.toJson),
      headers = headers
    )

    private val sslContext = SslContextBuilder
        .forClient()
        .protocols("TLSv1.3", "TLSv1.2")
        .build()

    override def searchRealtyInSquare(
        geoPolygon: GeoPolygon,
        rooms: List[Int],
        totalAreaGte: Int = 5,
        totalAreaLte: Int = 100,
        balconiesGte: Int = 0,
        floorGte: Int = 1,
        floorLte: Int = 100,
        pages: Int = 1): ZIO[EventLoopGroup with ChannelFactory with ApplicationConfig, Throwable, Seq[Apartment]] = {
        val query = s"search-offers/v2/search-offers-desktop/"
        val headers = Headers(
          HttpHeaderNames.USER_AGENT -> "PostmanRuntime/7.29.2"
        )
        val searchCoordinates = List(
          geoPolygon.bottomLeft.lonLatTuple,
          geoPolygon.topLeft.lonLatTuple,
          geoPolygon.bottomRight.lonLatTuple,
          geoPolygon.topRight.lonLatTuple
        )
        val queryBodies: List[CianJsonQuery] = (1 to pages).map { i =>
            CianSearchRequestDTO.make(
              coordinates = searchCoordinates,
              rooms = rooms,
              totalAreaGte = totalAreaGte,
              totalAreaLte = totalAreaLte,
              balconiesGt = balconiesGte,
              floorGte = floorGte,
              floorLte = Some(floorLte),
              page = i
            )
        }.toList
        for {
            config <- config
            url = s"${config.url}" / query

            _ <- zio.Console.printLine(s"send POST request on $url")
            responses <- ZIO.foreachPar(queryBodies){ queryBody =>
                makeRequest(url, sslContext, queryBody, headers)
            }
            apartments <- ZIO.foreachPar(responses){ response =>
                for{

                responseBody <- response.body.asString
                apartments <- CianParser.parseJsonString(responseBody)
                } yield apartments
            }.map(_.flatten)
        } yield apartments
    }

}

object SearchRealtyServiceCian {
    lazy val layer: ULayer[SearchRealtyService] = ZLayer.succeed(SearchRealtyServiceCian())
}
