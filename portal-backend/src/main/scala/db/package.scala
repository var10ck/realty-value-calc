//import com.typesafe.config.Config
import configuration.ApplicationConfig
import com.zaxxer.hikari.HikariDataSource
import io.getquill._
import io.getquill.context.ZioJdbc
import io.getquill.util.LoadConfig
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.{ClassLoaderResourceAccessor, CompositeResourceAccessor, FileSystemResourceAccessor}
import zio.{RIO, URIO, ZIO, ZLayer, _}
import zio.macros.accessible

package object db {

  type DataSource = javax.sql.DataSource

  object Ctx extends PostgresZioJdbcContext(NamingStrategy(Escape, Literal))

  def hikariDS: HikariDataSource = JdbcContextConfig(LoadConfig("db")).dataSource

  val zioDS: ZLayer[Any, Throwable, DataSource] = ZLayer.succeed(hikariDS)


    trait LiquibaseService{
        def performMigration: RIO[Liquibase, Unit]
    }


    final case class LiquibaseServiceLive() extends LiquibaseService {
        override def performMigration: RIO[Liquibase, Unit] = LiquibaseService.liquibase.map(_.update("dev"))

    }

    object LiquibaseServiceLive{
        def layer: ULayer[LiquibaseServiceLive] = ZLayer.succeed(LiquibaseServiceLive())

        def liquibaseLayer: ZLayer[Any with Scope with DataSource with ApplicationConfig, Throwable, Liquibase] =
            ZLayer.fromZIO(
              for {
                  config <- zio.config.getConfig[ApplicationConfig]
                  liquibase <- mkLiquibase(config)
              } yield liquibase
            )

        def mkLiquibase(config: ApplicationConfig): ZIO[Any with Scope with DataSource, Throwable, Liquibase] = for {
            ds <- ZIO.environment[DataSource].map(_.get)
            fileAccessor <- ZIO.from(new FileSystemResourceAccessor())
            classLoader <- ZIO.from(classOf[LiquibaseService].getClassLoader)
            classLoaderAccessor <- ZIO.from(new ClassLoaderResourceAccessor(classLoader))
            fileOpener <- ZIO.from(new CompositeResourceAccessor(fileAccessor, classLoaderAccessor))
            jdbcConn <- ZIO.acquireRelease(ZIO.from(new JdbcConnection(ds.getConnection)))(c => ZIO.succeed(c.close()))
            liqui <- ZIO.from(new Liquibase(config.liquibase.changeLog, fileOpener, jdbcConn))
        } yield liqui


    }

    object LiquibaseService{
        def performMigration: ZIO[Liquibase with LiquibaseService, Throwable, Unit] =
            ZIO.serviceWithZIO[LiquibaseService](_.performMigration)

        def liquibase: URIO[Liquibase, Liquibase] = ZIO.service[Liquibase]
    }
}
