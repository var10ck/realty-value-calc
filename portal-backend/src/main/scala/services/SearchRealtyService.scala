package services
import configuration.ApplicationConfig
import helpers.GeoHelper.GeoPolygon
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.{ULayer, ZIO}

trait SearchRealtyService {

    def searchRealtyInSquare(geoPolygon: GeoPolygon): ZIO[EventLoopGroup with ChannelFactory with ApplicationConfig, Throwable, String]

}

object SearchRealtyService{
    def searchRealtyInSquare(geoPolygon: GeoPolygon)
        : ZIO[EventLoopGroup with ChannelFactory with ApplicationConfig with SearchRealtyService, Throwable, String] =
        ZIO.serviceWithZIO[SearchRealtyService](_.searchRealtyInSquare(geoPolygon))

    def cian: ULayer[SearchRealtyService] = SearchRealtyServiceCian.layer
}