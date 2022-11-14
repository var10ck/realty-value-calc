package dao.repositories.corrections
import dao.entities.corrections.{CorrectionId, CorrectionNumeric}
import io.getquill.context.ZioJdbc.QIO
import zio.{ULayer, ZIO}

import java.sql.SQLException
import javax.sql.DataSource

trait CorrectionNumericRepository {

    def create(
        fieldName: String,
        referenceValue: String,
        referenceValueType: Int,
        analogueValue: String,
        analogueValueType: Int,
        correction: Int,
        correctionType: String,
        isEnabled: Boolean): QIO[CorrectionNumeric]

    def getBy(id: CorrectionId): QIO[Option[CorrectionNumeric]]

    def getAll: QIO[List[CorrectionNumeric]]

    def getAllEnabled: QIO[List[CorrectionNumeric]]

    def delete(id: CorrectionId): QIO[Unit]

    def update(
        id: CorrectionId,
        fieldName: Option[String],
        referenceValue: Option[String],
        referenceValueType: Option[Int],
        analogueValue: Option[String],
        analogueValueType: Option[Int],
        correction: Option[Int],
        correctionType: Option[String],
        isEnabled: Option[Boolean]
    ): QIO[Unit]

}

object CorrectionNumericRepository {

    def create(
        fieldName: String,
        referenceValue: String,
        referenceValueType: Int,
        analogueValue: String,
        analogueValueType: Int,
        correction: Int,
        correctionType: String,
        isEnabled: Boolean): ZIO[DataSource with CorrectionNumericRepository, SQLException, CorrectionNumeric] =
        ZIO.serviceWithZIO[CorrectionNumericRepository](
          _.create(
            fieldName,
            referenceValue,
            referenceValueType,
            analogueValue,
            analogueValueType,
            correction,
            correctionType,
            isEnabled))

    def getBy(
        id: CorrectionId): ZIO[DataSource with CorrectionNumericRepository, SQLException, Option[CorrectionNumeric]] =
        ZIO.serviceWithZIO[CorrectionNumericRepository](_.getBy(id))

    def getAll: ZIO[DataSource with CorrectionNumericRepository, SQLException, List[CorrectionNumeric]] =
        ZIO.serviceWithZIO[CorrectionNumericRepository](_.getAll)

    def getAllEnabled: ZIO[DataSource with CorrectionNumericRepository, SQLException, List[CorrectionNumeric]] =
        ZIO.serviceWithZIO[CorrectionNumericRepository](_.getAllEnabled)

    def delete(id: CorrectionId): ZIO[DataSource with CorrectionNumericRepository, SQLException, Unit] =
        ZIO.serviceWithZIO[CorrectionNumericRepository](_.delete(id))

    def update(
        id: CorrectionId,
        fieldName: Option[String],
        referenceValue: Option[String],
        referenceValueType: Option[Int],
        analogueValue: Option[String],
        analogueValueType: Option[Int],
        correction: Option[Int],
        correctionType: Option[String],
        isEnabled: Option[Boolean]
    ): ZIO[DataSource with CorrectionNumericRepository, SQLException, Unit] =
        ZIO.serviceWithZIO[CorrectionNumericRepository](
          _.update(
            id,
            fieldName,
            referenceValue,
            referenceValueType,
            analogueValue,
            analogueValueType,
            correction,
            correctionType,
            isEnabled))

    def live: ULayer[CorrectionNumericRepository] = CorrectionNumericRepositoryLive.layer
}
