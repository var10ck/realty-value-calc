package services
import configuration.ApplicationConfig
import dto.integration.SuggestionResponseDTO
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.{Task, ULayer, ZIO}

trait GeoSuggestionService {

    def getCoordinatedByAddress(address: String)
        : ZIO[EventLoopGroup with ChannelFactory with ApplicationConfig, Throwable, SuggestionResponseDTO]

}
object GeoSuggestionService {

    def getCoordinatedByAddress(address: String): ZIO[
      EventLoopGroup with ChannelFactory with ApplicationConfig with GeoSuggestionService,
      Throwable,
      SuggestionResponseDTO] =
        ZIO.serviceWithZIO[GeoSuggestionService](_.getCoordinatedByAddress(address))


    lazy val daDataLive: ULayer[GeoSuggestionService] = GeoSuggestionServiceDaData.layer
}
