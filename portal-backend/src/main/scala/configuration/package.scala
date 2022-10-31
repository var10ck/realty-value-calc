import zio._
import zio.config._
import zio.config.ReadError
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfigSource

package object configuration {

    case class ApplicationConfig(api: Api, liquibase: LiquibaseConfig, db2: DbConfig)

    case class LiquibaseConfig(changeLog: String)
    case class Api(host: String, port: Int)
    case class DbConfig(driver: String, url: String, user: String, password: String)

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
    }
}
