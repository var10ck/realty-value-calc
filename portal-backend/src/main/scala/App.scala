import api.{AuthApi, CorrectionApi, RealtyObjectApi, RealtyObjectPoolApi}
import configuration.ApplicationConfig
import dao.repositories.auth.{UserRepository, UserSessionRepository}
import dao.repositories.corrections.CorrectionNumericRepository
import dao.repositories.integration.AnalogueObjectRepository
import dao.repositories.realty.{RealtyObjectPoolRepository, RealtyObjectPoolRepositoryLive, RealtyObjectRepository}
import db.{DataSource, LiquibaseService, LiquibaseServiceLive, zioLiveDS}
import liquibase.Liquibase
import services.{AuthService, CorrectionService, GeoSuggestionService, RealtyObjectPoolService, RealtyObjectService, SearchRealtyService}
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.{ExitCode, Scope, ZLayer}

object App {

    type AppEnvironment = ApplicationConfig
        with DataSource with LiquibaseService with UserRepository with UserSessionRepository with AuthService
        with Liquibase with RealtyObjectRepository with RealtyObjectService with Scope with GeoSuggestionService
        with EventLoopGroup with ChannelFactory with RealtyObjectPoolRepository with RealtyObjectPoolService
        with SearchRealtyService with AnalogueObjectRepository with CorrectionService with CorrectionNumericRepository

    val appEnv: ZLayer[Any with Scope, Throwable, AppEnvironment] =
        ApplicationConfig.live >+> zioLiveDS >+> LiquibaseService.live >+> UserRepository.live >+>
            UserSessionRepository.live >+> AuthService.live >+> LiquibaseServiceLive.liquibaseLayer >+>
            RealtyObjectRepository.live >+> RealtyObjectService.live >+> Scope.default >+> GeoSuggestionService.daDataLive >+>
            RealtyObjectPoolRepositoryLive.layer >+> EventLoopGroup.auto() >+> ChannelFactory.auto >+> Scope.default >+>
            RealtyObjectPoolRepository.live >+> RealtyObjectPoolService.live >+> SearchRealtyService.cian >+>
            AnalogueObjectRepository.live >+> CorrectionService.live >+> CorrectionNumericRepository.live

    val httpApp = AuthApi.api ++ RealtyObjectApi.api ++ RealtyObjectPoolApi.api ++ CorrectionApi.api

    val serverConfig = zhttp.service.Server
        .app(httpApp)
        .withObjectAggregator(Int.MaxValue)

    val server = {
        for {
            config <- zio.config.getConfig[ApplicationConfig]
            _ <- LiquibaseService.performMigration *>
                serverConfig.withPort(config.api.port).startDefault
        } yield ExitCode.success
    }
}
