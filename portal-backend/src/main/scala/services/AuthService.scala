package services
import dao.entities.auth.{User, UserSession}
import dao.repositories.auth.{UserRepository, UserSessionRepository}
import dto.auth.{AuthUserDTO, CheckSessionDTO, CreateUserDTO}
import zio.{RIO, Task, ZIO}

import java.sql.SQLException
import javax.sql.DataSource

trait AuthService {

    /** Create user and return it
      */
    def createUser(createUserDTO: CreateUserDTO): ZIO[DataSource with UserRepository, SQLException, User]

    /** Try to find user with given login/password. Returns new UserSession if succeed */
    def authUser(
        authUserDTO: AuthUserDTO): RIO[DataSource with UserRepository with UserSessionRepository, Option[UserSession]]

    /** Check if sessionId is exists in database */
    def checkSession(checkSessionDTO: CheckSessionDTO): RIO[DataSource with UserSessionRepository, Boolean]

}

object AuthService {

    /** Create user and return it
      */
    def createUser(
        createUserDTO: CreateUserDTO): ZIO[DataSource with UserRepository with AuthService, SQLException, User] =
        ZIO.serviceWithZIO[AuthService](_.createUser(createUserDTO))

    /** Try to find user with given login/password. Returns new UserSession if succeed */
    def authUser(authUserDTO: AuthUserDTO): ZIO[
      DataSource with UserRepository with UserSessionRepository with AuthService,
      Throwable,
      Option[UserSession]] =
        ZIO.serviceWithZIO[AuthService](_.authUser(authUserDTO))

    /** Check if sessionId is exists in database */
    def checkSession(checkSessionDTO: CheckSessionDTO)
        : ZIO[DataSource with UserSessionRepository with AuthService, Throwable, Boolean] =
        ZIO.serviceWithZIO[AuthService](_.checkSession(checkSessionDTO))

}
