package services
import configuration.ApplicationConfig
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.Scope
import zio.test.{assertTrue, ZIOSpecDefault}

object GeoSuggestionServiceDaDataTest extends ZIOSpecDefault {

    def spec =
        suite("GeoSuggestionServiceDaData Test")(
          test("get lat and lon") {
              for {
                  res <- GeoSuggestionService.getCoordinatedByAddress("ул Профсоюзная 110к4")
                  _ <- zio.Console.printLine(s"lat = ${res.lat}, lon = ${res.lon}")
              } yield assertTrue(res.lat.nonEmpty && res.lon.nonEmpty)
          }
        ).provide(
          ApplicationConfig.live,
          Scope.default,
          GeoSuggestionService.daDataLive,
          EventLoopGroup.auto(),
          ChannelFactory.auto
        )

}
