package services
import configuration.ApplicationConfig
import dto.integration.{SuggestionRequestDTO, SuggestionResponseDTO}
import helpers.typed.Uri.syntax.stringToUriType
import io.circe
import io.netty.handler.codec.http
import io.netty.handler.codec.http.{HttpHeaderNames, HttpHeaderValues}
import zhttp.http.{Body, Headers}
import zhttp.http.Method.{GET, POST}
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.{ULayer, ZIO, ZLayer}
import io.circe.syntax.{EncoderOps, _}
import io.circe._

final case class GeoSuggestionServiceDaData() extends GeoSuggestionService {
    val config: ZIO[ApplicationConfig, Nothing, configuration.DaData] =
        zio.config.getConfig[ApplicationConfig].map(_.integration.daData)

    def getCoordinatedByAddress(address: String)
        : ZIO[EventLoopGroup with ChannelFactory with ApplicationConfig, Throwable, SuggestionResponseDTO] = {

        for {
            config <- config
            headers = Headers(
              HttpHeaderNames.AUTHORIZATION -> s"Token ${config.daDataApiKey}",
              HttpHeaderNames.ACCEPT -> HttpHeaderValues.APPLICATION_JSON,
              HttpHeaderNames.CONTENT_TYPE -> HttpHeaderValues.APPLICATION_JSON
            )
            url = s"${config.daDataUrl}" / "suggestions/api/4_1/rs/suggest/address"
            response <- Client.request(
              url.raw,
              method = POST,
              headers = headers,
              content = Body.fromCharSequence(SuggestionRequestDTO(address).asJson.toString()))
            body <- response.body.asString
            _ <- zio.Console.printLine(body)
            dto <- ZIO.fromEither(circe.jawn.decode[SuggestionResponseDTO](body))
                .orElseFail(exceptions.BodyParsingException("SuggestionResponseDTO"))
        } yield dto
    }

}

object GeoSuggestionServiceDaData {
    lazy val layer: ULayer[GeoSuggestionServiceDaData] = ZLayer.succeed(GeoSuggestionServiceDaData())
}
