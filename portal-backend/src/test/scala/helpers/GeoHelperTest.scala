package helpers
import services.GeoSuggestionServiceDaDataTest.suite
import zio.ZIO
import zio.test.{assertTrue, ZIOSpecDefault}

object GeoHelperTest extends ZIOSpecDefault {

    def spec = suite("GeoHelper Test")(
      test("get lat and lon") {
          for {
              polygon <- ZIO.from(GeoHelper.makeSquareAroundLocation("55.641473", "37.519063", 400))
              _ <- zio.Console.printLine(polygon)
          } yield assertTrue(true)
      }
    )
}
