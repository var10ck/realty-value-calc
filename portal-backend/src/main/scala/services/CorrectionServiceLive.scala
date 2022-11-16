package services
import dao.entities.corrections.{CorrectionConstant, CorrectionId, CorrectionNumeric}
import dao.repositories.corrections.{CorrectionConstantRepository, CorrectionNumericRepository}
import dto.corrections.{CreateConstantCorrectionDTO, CreateNumericCorrectionDTO, UpdateConstantCorrectionDTO, UpdateNumericCorrectionDTO}
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

    override def createConstant(dto: CreateConstantCorrectionDTO)
        : ZIO[DataSource with CorrectionConstantRepository, SQLException, CorrectionConstant] =
        CorrectionConstantRepository.create(
          dto.fieldName,
          dto.referenceValue,
          dto.analogueValue,
          dto.correction,
          dto.correctionType,
          dto.isEnabled
        )

    override def deleteNumeric(id: String): ZIO[DataSource with CorrectionNumericRepository, Throwable, Unit] = {
        for {
            typedId <- CorrectionId.fromString(id)
            _ <- CorrectionNumericRepository.delete(typedId)
        } yield ()
    }

    override def deleteConstant(id: String): ZIO[DataSource with CorrectionConstantRepository, Throwable, Unit] =
        for {
            typedId <- CorrectionId.fromString(id)
            _ <- CorrectionConstantRepository.delete(typedId)
        } yield ()

    override def getNumericBy(
        id: String): ZIO[DataSource with CorrectionNumericRepository, Throwable, CorrectionNumeric] =
        for {
            typedId <- CorrectionId.fromString(id)
            correctionOpt <- CorrectionNumericRepository.getBy(typedId)
            correction <- ZIO.fromOption(correctionOpt).orElseFail(exceptions.CorrectionNotFound("id", id))
        } yield correction

    override def getConstantBy(id: String): ZIO[DataSource with CorrectionConstantRepository, Throwable, CorrectionConstant] =
        for {
            typedId <- CorrectionId.fromString(id)
            correctionOpt <- CorrectionConstantRepository.getBy(typedId)
            correction <- ZIO.fromOption(correctionOpt).orElseFail(exceptions.CorrectionNotFound("id", id))
        } yield correction

    override def getAllNumeric
        : ZIO[DataSource with CorrectionNumericRepository, SQLException, List[CorrectionNumeric]] =
        CorrectionNumericRepository.getAll

    override def getAllConstant: ZIO[DataSource with CorrectionConstantRepository, SQLException, List[CorrectionConstant]] =
        CorrectionConstantRepository.getAll

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

    override def updateConstant(
        dto: UpdateConstantCorrectionDTO): ZIO[DataSource with CorrectionConstantRepository, SQLException, Unit] =
        CorrectionConstantRepository.update(
          dto.id,
          dto.fieldName,
          dto.referenceValue,
          dto.analogueValue,
          dto.correction,
          dto.correctionType,
          dto.isEnabled
        )
}

object CorrectionServiceLive {
    val layer: ULayer[CorrectionService] = ZLayer.succeed(CorrectionServiceLive())
}
