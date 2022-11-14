package services
import dao.entities.corrections.{CorrectionId, CorrectionNumeric}
import dao.repositories.corrections.CorrectionNumericRepository
import dto.corrections.{CreateNumericCorrectionDTO, UpdateNumericCorrectionDTO}
import zio.{ULayer, ZIO, ZLayer}

import java.sql.SQLException
import javax.sql.DataSource

case class CorrectionServiceLive() extends CorrectionService {
    override def createNumeric(
        dto: CreateNumericCorrectionDTO
    ): ZIO[DataSource with CorrectionNumericRepository, SQLException, CorrectionNumeric] =
        CorrectionNumericRepository.create(
          dto.fieldName,
          dto.referenceValue,
          dto.referenceValueType,
          dto.analogueValue,
          dto.analogueValueType,
          dto.correction,
          dto.correctionType,
          dto.isEnabled)

    override def deleteNumeric(id: String): ZIO[DataSource with CorrectionNumericRepository, Throwable, Unit] = {
        for {
            typedId <- CorrectionId.fromString(id)
            _ <- CorrectionNumericRepository.delete(typedId)
        } yield ()
    }

    override def getNumericBy(
        id: String): ZIO[DataSource with CorrectionNumericRepository, Throwable, CorrectionNumeric] =
        for {
            typedId <- CorrectionId.fromString(id)
            correctionOpt <- CorrectionNumericRepository.getBy(typedId)
            correction <- ZIO.fromOption(correctionOpt).orElseFail(exceptions.CorrectionNotFound("id", id))
        } yield correction

    override def getAllNumeric
        : ZIO[DataSource with CorrectionNumericRepository, SQLException, List[CorrectionNumeric]] =
        CorrectionNumericRepository.getAll

    override def updateNumeric(
        dto: UpdateNumericCorrectionDTO): ZIO[DataSource with CorrectionNumericRepository, SQLException, Unit] =
        CorrectionNumericRepository.update(
          dto.id,
          dto.fieldName,
          dto.referenceValue,
          dto.referenceValueType,
          dto.analogueValue,
          dto.analogueValueType,
          dto.correction,
          dto.correctionType,
          dto.isEnabled
        )
}

object CorrectionServiceLive {
    val layer: ULayer[CorrectionService] = ZLayer.succeed(CorrectionServiceLive())
}
