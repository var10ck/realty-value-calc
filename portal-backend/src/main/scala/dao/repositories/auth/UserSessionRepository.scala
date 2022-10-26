package dao.repositories.auth
import dao.entities.auth.{UserId, UserSession, UserSessionId}
import io.getquill.context.ZioJdbc.QIO
import zio.{Task, ZIO}

import java.sql.SQLException
import java.time.LocalDateTime
import javax.sql.DataSource

trait UserSessionRepository {

    /** create new session for User */
    def create(
        userId: UserId,
        validFrom: LocalDateTime = LocalDateTime.now(),
        validUntil: LocalDateTime = LocalDateTime.MAX
    ): QIO[UserSession]

    /** delete session */
    def delete(userSessionId: UserSessionId): QIO[Unit]

    /** find user session if its exists */
    def get(userSessionId: UserSessionId): QIO[Option[UserSession]]

    /** find all user's sessions */
    def findForUser(userId: UserId): QIO[List[UserSession]]

}

object UserSessionRepository {

    /** create new session for User */
    def create(
        userId: UserId,
        validFrom: LocalDateTime = LocalDateTime.now(),
        validUntil: LocalDateTime = LocalDateTime.MAX
    ): ZIO[DataSource with UserSessionRepository, SQLException, UserSession] =
        ZIO.serviceWithZIO[UserSessionRepository](_.create(userId, validFrom, validUntil))

    /** delete session */
    def delete(userSessionId: UserSessionId): ZIO[DataSource with UserSessionRepository, SQLException, Unit] =
        ZIO.serviceWithZIO[UserSessionRepository](_.delete(userSessionId))

    /** find user session if its exists */
    def get(
        userSessionId: UserSessionId): ZIO[DataSource with UserSessionRepository, SQLException, Option[UserSession]] =
        ZIO.serviceWithZIO[UserSessionRepository](_.get(userSessionId))

    /** find all user's sessions */
    def findForUser(userId: UserId): ZIO[DataSource with UserSessionRepository, SQLException, List[UserSession]] =
        ZIO.serviceWithZIO[UserSessionRepository](_.findForUser(userId))
}
