package dao.repositories.corrections
import dao.entities.corrections.{CorrectionConstant, CorrectionId}
import io.getquill.context.ZioJdbc.QIO
import zio.{ULayer, ZIO}

import java.sql.SQLException
import javax.sql.DataSource

trait CorrectionConstantRepository {

    def create(
        fieldName: String,
        referenceValue: String,
        analogueValue: String,
        correction: Int,
        correctionType: String,
        isEnabled: Boolean): QIO[CorrectionConstant]

    def getBy(id: CorrectionId): QIO[Option[CorrectionConstant]]

    def getAll: QIO[List[CorrectionConstant]]

    def getAllEnabled: QIO[List[CorrectionConstant]]

    def delete(id: CorrectionId): QIO[Unit]

    def update(
        id: CorrectionId,
        fieldName: Option[String],
        referenceValue: Option[String],
        analogueValue: Option[String],
        correction: Option[Int],
        correctionType: Option[String],
        isEnabled: Option[Boolean]
    ): QIO[Unit]
}

object CorrectionConstantRepository {
    def create(
        fieldName: String,
        referenceValue: String,
        analogueValue: String,
        correction: Int,
        correctionType: String,
        isEnabled: Boolean): ZIO[DataSource with CorrectionConstantRepository, SQLException, CorrectionConstant] =
        ZIO.serviceWithZIO[CorrectionConstantRepository](
          _.create(fieldName, referenceValue, analogueValue, correction, correctionType, isEnabled))

    def getBy(
        id: CorrectionId): ZIO[DataSource with CorrectionConstantRepository, SQLException, Option[CorrectionConstant]] =
        ZIO.serviceWithZIO[CorrectionConstantRepository](_.getBy(id))

    def getAll: ZIO[DataSource with CorrectionConstantRepository, SQLException, List[CorrectionConstant]] =
        ZIO.serviceWithZIO[CorrectionConstantRepository](_.getAll)

    def getAllEnabled: ZIO[DataSource with CorrectionConstantRepository, SQLException, List[CorrectionConstant]] =
        ZIO.serviceWithZIO[CorrectionConstantRepository](_.getAllEnabled)

    def delete(id: CorrectionId): ZIO[DataSource with CorrectionConstantRepository, SQLException, Unit] =
        ZIO.serviceWithZIO[CorrectionConstantRepository](_.delete(id))

    def update(
        id: CorrectionId,
        fieldName: Option[String] = None,
        referenceValue: Option[String] = None,
        analogueValue: Option[String] = None,
        correction: Option[Int] = None,
        correctionType: Option[String] = None,
        isEnabled: Option[Boolean] = None
    ): ZIO[DataSource with CorrectionConstantRepository, SQLException, Unit] =
        ZIO.serviceWithZIO[CorrectionConstantRepository](
          _.update(id, fieldName, referenceValue, analogueValue, correction, correctionType, isEnabled))

    lazy val live: ULayer[CorrectionConstantRepository] = CorrectionConstantRepositoryLive.layer
}
