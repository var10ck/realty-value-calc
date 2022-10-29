import api.{AuthApi, FileUploadApi, RealtyObjectApi}
import configuration.ApplicationConfig
import dao.repositories.auth.{UserRepository, UserRepositoryLive, UserSessionRepository, UserSessionRepositoryLive}
import dao.repositories.realty.{RealtyObjectRepository, RealtyObjectRepositoryLive}
import db.{DataSource, LiquibaseService, LiquibaseServiceLive, zioTestDS}
import liquibase.Liquibase
import services.{AuthService, AuthServiceLive, RealtyObjectService, RealtyObjectServiceLive}
import zio.{ExitCode, Random}

object App {


    type AppEnvironment = UserRepository
        with UserSessionRepository with AuthService with DataSource with Random with ApplicationConfig with Liquibase
        with LiquibaseService with RealtyObjectRepository with RealtyObjectService


    val appEnv = ApplicationConfig.test >+> zioTestDS >+> LiquibaseServiceLive.layer >+> UserRepositoryLive.layer >+>
        UserSessionRepositoryLive.layer >+> AuthServiceLive.layer >+> LiquibaseServiceLive.liquibaseLayer >+>
        RealtyObjectRepositoryLive.layer >+> RealtyObjectServiceLive.layer

    val httpApp = AuthApi.api ++ FileUploadApi.api ++ RealtyObjectApi.api

    val server = {
        for {
            config <- zio.config.getConfig[ApplicationConfig]
            _ <- LiquibaseService.performMigration *>
                zhttp.service.Server.start(config.api.port, httpApp)
        } yield ExitCode.success
    }
}
