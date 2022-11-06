package services
import configuration.ApplicationConfig
import helpers.GeoHelper
import helpers.GeoHelper.{GeoPolygon, Point}
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.{Scope, ZIO}
import zio.test.{assertTrue, ZIOSpecDefault}

import java.net.{HttpURLConnection, URL}

object SearchRealtyServiceCianTest extends ZIOSpecDefault {

    def spec =
        suite("SearchRealtyServiceCian Test")(
          test("get flats suggestions") {
              for {
                  polygon <- ZIO.from(GeoHelper.makeSquareAroundLocation("55.641473", "37.519063", 10000))
                  _ <- ZIO.debug(polygon)
                  res <- SearchRealtyService.searchRealtyInSquare(polygon, List(1,2), pages = 6)
                  _ <- ZIO.succeed(res.foreach(println))
                  _ <- zio.Console.printLine(res.length)

              } yield assertTrue(res.nonEmpty)
          }
        ).provide(
          ApplicationConfig.live,
          Scope.default,
          SearchRealtyService.cian,
          EventLoopGroup.auto(),
          ChannelFactory.auto
        )

}
