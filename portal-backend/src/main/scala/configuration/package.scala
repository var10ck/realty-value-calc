import io.netty.handler.ssl.{SslContext, SslContextBuilder}
import zhttp.service.server.ServerSSLHandler.ctxFromCert
import zio._
import zio.config._
import zio.config.ReadError
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource

import scala.io.Source

package object configuration {

    case class ApplicationConfig(api: Api, liquibase: LiquibaseConfig, db2: DbConfig, integration: IntegrationConfig)

    case class LiquibaseConfig(changeLog: String)
    case class Api(host: String, port: Int)
    case class DbConfig(driver: String, url: String, user: String, password: String)

    case class IntegrationConfig(daData: DaData, cian: Cian)

    case class DaData(daDataUrl: String, daDataApiKey: String)

    case class Cian(url: String)

    object ApplicationConfig {
        def live: ZLayer[Any, ReadError[String], ApplicationConfig] = ZLayer {
            read {
                descriptor[ApplicationConfig].from(
                  TypesafeConfigSource.fromResourcePath
                )
            }
        }

        def test: ZLayer[Any, ReadError[String], ApplicationConfig] = ZLayer {
            read {
                descriptor[ApplicationConfig].from(
                  TypesafeConfigSource.fromHoconFilePath("src/main/resources/application.test.conf")
                )
            }
        }

        lazy val sslContext: SslContext = SslContextBuilder
            .forClient()
            .protocols("TLSv1.3", "TLSv1.2")
            .build()
    }
}
