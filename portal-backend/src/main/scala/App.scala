import api.{AuthApi, FileUploadApi, RealtyObjectApi}
import configuration.ApplicationConfig
import dao.repositories.auth.{UserRepository, UserSessionRepository}
import dao.repositories.realty.RealtyObjectRepository
import db.{DataSource, LiquibaseService, LiquibaseServiceLive, zioLiveDS, zioTestDS}
import liquibase.Liquibase
import services.{AuthService, RealtyObjectService}
import zio.{ExitCode, Scope, ZIOAppArgs, ZLayer}

object App {


    type AppEnvironment = UserRepository
        with UserSessionRepository with AuthService with DataSource with ApplicationConfig with Liquibase
        with LiquibaseService with RealtyObjectRepository with RealtyObjectService


    // TODO: replace ApplicationConfig.test to ApplicationConfig.live, zioTestDS to zioLiveDS for production deployment
    val appEnv = ApplicationConfig.live >+> zioLiveDS >+> LiquibaseService.live >+> UserRepository.live >+>
        UserSessionRepository.live >+> AuthService.live >+> LiquibaseServiceLive.liquibaseLayer >+>
        RealtyObjectRepository.live >+> RealtyObjectService.live >+> Scope.default

    val httpApp = AuthApi.api ++ FileUploadApi.api ++ RealtyObjectApi.api

    val server = {
        for {
            config <- zio.config.getConfig[ApplicationConfig]
            _ <- LiquibaseService.performMigration *>
                zhttp.service.Server.start(config.api.port, httpApp)
        } yield ExitCode.success
    }
}
