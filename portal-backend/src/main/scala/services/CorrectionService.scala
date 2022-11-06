package services
import dao.entities.corrections.{CorrectionId, CorrectionNumeric}
import dao.repositories.corrections.CorrectionNumericRepository
import dto.corrections.{CreateNumericCorrectionDTO, UpdateNumericCorrectionDTO}
import zio.{ULayer, ZIO}

import java.sql.SQLException
import javax.sql.DataSource

trait CorrectionService {

    def createNumeric(
        dto: CreateNumericCorrectionDTO
    ): ZIO[DataSource with CorrectionNumericRepository, SQLException, CorrectionNumeric]

    def deleteNumeric(id: String): ZIO[DataSource with CorrectionNumericRepository, Throwable, Unit]

    def getNumericBy(
        id: String): ZIO[DataSource with CorrectionNumericRepository, Throwable, CorrectionNumeric]

    def getAllNumeric: ZIO[DataSource with CorrectionNumericRepository, SQLException, List[CorrectionNumeric]]

    def updateNumeric(dto: UpdateNumericCorrectionDTO): ZIO[DataSource with CorrectionNumericRepository, SQLException, Unit]

}

object CorrectionService {
    def createNumeric(
        dto: CreateNumericCorrectionDTO
    ): ZIO[DataSource with CorrectionNumericRepository with CorrectionService, SQLException, CorrectionNumeric] =
        ZIO.serviceWithZIO[CorrectionService](_.createNumeric(dto))

    def deleteNumeric(
        id: String): ZIO[DataSource with CorrectionNumericRepository with CorrectionService, Throwable, Unit] =
        ZIO.serviceWithZIO[CorrectionService](_.deleteNumeric(id))

    def getNumericBy(id: String): ZIO[
      DataSource with CorrectionNumericRepository with CorrectionService,
        Throwable,
        CorrectionNumeric] =
        ZIO.serviceWithZIO[CorrectionService](_.getNumericBy(id))

    def getAllNumeric: ZIO[
      DataSource with CorrectionNumericRepository with CorrectionService,
      SQLException,
      List[CorrectionNumeric]] = ZIO.serviceWithZIO[CorrectionService](_.getAllNumeric)

    def updateNumeric(dto: UpdateNumericCorrectionDTO)
        : ZIO[DataSource with CorrectionNumericRepository with CorrectionService, SQLException, Unit] =
        ZIO.serviceWithZIO[CorrectionService](_.updateNumeric(dto))

    def live: ULayer[CorrectionService] = CorrectionServiceLive.layer
}
