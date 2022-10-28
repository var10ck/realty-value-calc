import api.AuthApi
import configuration.ApplicationConfig
import dao.repositories.auth.{UserRepository, UserRepositoryLive, UserSessionRepository, UserSessionRepositoryLive}
import db.{DataSource, LiquibaseService, LiquibaseServiceLive, zioTestDS}
import liquibase.Liquibase
import services.{AuthService, AuthServiceLive}
import zio.{ExitCode, Random}

object App {


    type AppEnvironment = UserRepository
        with UserSessionRepository with AuthService with DataSource with Random with ApplicationConfig with Liquibase
        with LiquibaseService


    val appEnv = ApplicationConfig.test >+> zioTestDS >+> LiquibaseServiceLive.layer >+> UserRepositoryLive.layer >+>
        UserSessionRepositoryLive.layer >+> AuthServiceLive.layer >+> LiquibaseServiceLive.liquibaseLayer

    val httpApp = AuthApi.api

    val server = {
        for {
            config <- zio.config.getConfig[ApplicationConfig]
            _ <- LiquibaseService.performMigrationWithDropAll *>
                zhttp.service.Server.start(config.api.port, httpApp)
        } yield ExitCode.success
//        (LiquibaseService.performMigration *> zhttp.service.Server.start(8080, httpApp))
//    .provideLayer(appEnv)
    }
}
