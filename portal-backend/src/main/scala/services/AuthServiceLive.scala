package services
import com.github.t3hnar.bcrypt._
import dao.entities.auth.{User, UserSession, UserSessionId}
import dao.repositories.auth._
import dto.auth.{AuthUserDTO, CheckSessionDTO, CreateUserDTO}
import helpers.DateHelper
import zio.{RIO, ULayer, ZIO, ZLayer}

import java.sql._
import javax.sql._

final case class AuthServiceLive() extends AuthService {

    /** Create user and return it
      */
    override def createUser(createUserDTO: CreateUserDTO): ZIO[DataSource with UserRepository, SQLException, User] = {
        val parsedBirthdate = DateHelper.stringToLocalDate(createUserDTO.birthdate)
        UserRepository.create(
          login = createUserDTO.login,
          password = createUserDTO.password,
          firstName = createUserDTO.firstName,
          lastName = createUserDTO.lastName,
          birthdate = parsedBirthdate)
    }

    /** Try to find user with given login/password. Returns new UserSession if succeed */
    override def authUser(authUserDTO: AuthUserDTO)
        : RIO[DataSource with UserRepository with UserSessionRepository, Option[UserSession]] =
        for {
            userOpt <- UserRepository.getByLogin(authUserDTO.login)
            user <- ZIO.fromOption(userOpt).mapError(e => exceptions.UserNotFound("login", authUserDTO.login))
            s <- ZIO.when(user.passwordHash == authUserDTO.password.bcryptBounded(user.salt)){
                UserSessionRepository.create(user.id)
            }
        } yield s

    /** Check if sessionId is exists in database */
    override def checkSession(checkSessionDTO: CheckSessionDTO): RIO[DataSource with UserSessionRepository, Boolean] =
        for{
            sessionId <- UserSessionId.fromString(checkSessionDTO.sessionId)
            session <- UserSessionRepository.get(sessionId)
            result <- ZIO.from(session.isDefined)
        }    yield result
}

object AuthServiceLive{
    def layer: ULayer[AuthServiceLive] = ZLayer.succeed(AuthServiceLive())
}
