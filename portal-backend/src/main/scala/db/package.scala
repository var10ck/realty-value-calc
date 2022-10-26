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



  @accessible
  object LiquibaseService {

    type LiquibaseService = Service

    type Liqui = Liquibase

    trait Service {
      def performMigration: RIO[Liqui, Unit]
    }

    class Impl extends Service {

      override def performMigration: RIO[Liqui, Unit] = liquibase.map(_.update("dev"))
    }
     
    def mkLiquibase(config: ApplicationConfig): ZIO[Any with Scope with DataSource, Throwable, Liqui] = for {
      ds <- ZIO.environment[DataSource].map(_.get)
      fileAccessor <-  ZIO.from(new FileSystemResourceAccessor())
      classLoader <- ZIO.from(classOf[Service].getClassLoader)
      classLoaderAccessor <- ZIO.from(new ClassLoaderResourceAccessor(classLoader))
      fileOpener <- ZIO.from(new CompositeResourceAccessor(fileAccessor, classLoaderAccessor))
      jdbcConn <- ZIO.acquireRelease(ZIO.from(new JdbcConnection(ds.getConnection)))(c => ZIO.succeed(c.close()))
      liqui <- ZIO.from(new Liquibase(config.liquibase.changeLog, fileOpener, jdbcConn))
    } yield liqui


    val liquibaseLayer: ZLayer[Any with Scope with DataSource with ApplicationConfig, Throwable, Liqui] = ZLayer.fromZIO(
      for {
        config <- zio.config.getConfig[ApplicationConfig]
        liquibase <- mkLiquibase(config)
      } yield liquibase
    )


    def liquibase: URIO[Liqui, Liquibase] = ZIO.succeed[Liquibase]

    val live: ULayer[LiquibaseService] = ZLayer.succeed(new Impl)

  }
}
