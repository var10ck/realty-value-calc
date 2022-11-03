package dto.integration.cian
import helpers.GeoHelper
import services.SearchRealtyService
import services.SearchRealtyServiceCianTest.{suite, test}
import zio.ZIO
import zio.json.{DecoderOps, EncoderOps}
import zio.test.{ZIOSpecDefault, assertTrue}

object CianSearchRequestDTOTest extends ZIOSpecDefault {

    def spec =
        suite("CianSearchRequestDTO Test")(
          test("make cian json request body") {

              val geoPolygon = GeoHelper.makeSquareAroundLocation("55.641473", "37.519063", 10000)
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
                  jsonQueryBody <-  ZIO.from(queryBody.toJson)
                  _ <- zio.Console.printLine(jsonQueryBody)
              } yield assertTrue(jsonQueryBody.fromJson[CianJsonQuery].isRight)
          }
        )

}
