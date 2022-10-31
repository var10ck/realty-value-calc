import App.appEnv
import configuration.ApplicationConfig
import dao.repositories.auth.{UserRepository, UserSessionRepository}
import dao.repositories.realty.RealtyObjectRepository
import db.{LiquibaseService, LiquibaseServiceLive, zioTestDS}
import services.{AuthService, RealtyObjectService}
import zio._
import zio.Console.printLine

object Main extends ZIOAppDefault {
  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] =
      App.server
          .provideLayer(appEnv)
}