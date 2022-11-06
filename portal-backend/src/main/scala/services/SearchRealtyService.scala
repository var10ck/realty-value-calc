package services
import configuration.ApplicationConfig
import dto.integration.cian.Apartment
import helpers.GeoHelper.GeoPolygon
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.{ULayer, ZIO}

trait SearchRealtyService {

    def searchRealtyInSquare(
        geoPolygon: GeoPolygon,
        rooms: List[Int],
        totalAreaGte: Int = 5,
        totalAreaLte: Int = 100,
        balconiesGte: Int = 0,
        floorGte: Int = 1,
        floorLte: Int = 100,
        pages: Int): ZIO[EventLoopGroup with ChannelFactory with ApplicationConfig, Throwable, Seq[Apartment]]

}

object SearchRealtyService {
    def searchRealtyInSquare(
        geoPolygon: GeoPolygon,
        rooms: List[Int],
        totalAreaGte: Int = 5,
        totalAreaLte: Int = 100,
        balconiesGte: Int = 0,
        floorGte: Int = 1,
        floorLte: Int = 100,
        pages: Int = 1)
        : ZIO[EventLoopGroup with ChannelFactory with ApplicationConfig with SearchRealtyService, Throwable, Seq[Apartment]] =
        ZIO.serviceWithZIO[SearchRealtyService](
          _.searchRealtyInSquare(geoPolygon, rooms, totalAreaGte, totalAreaLte, balconiesGte, floorGte, floorLte, pages))

    def cian: ULayer[SearchRealtyService] = SearchRealtyServiceCian.layer
}
