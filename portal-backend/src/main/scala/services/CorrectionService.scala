package services
import dao.entities.corrections.{CorrectionConstant, CorrectionId, CorrectionNumeric}
import dao.repositories.corrections.{CorrectionConstantRepository, CorrectionNumericRepository}
import dto.corrections.{CreateConstantCorrectionDTO, CreateNumericCorrectionDTO, UpdateConstantCorrectionDTO, UpdateNumericCorrectionDTO}
import zio.{ULayer, ZIO}

import java.sql.SQLException
import javax.sql.DataSource

trait CorrectionService {

    def createNumeric(
        dto: CreateNumericCorrectionDTO
    ): ZIO[DataSource with CorrectionNumericRepository, SQLException, CorrectionNumeric]

    def createConstant(dto: CreateConstantCorrectionDTO)
        : ZIO[DataSource with CorrectionConstantRepository, SQLException, CorrectionConstant]

    def deleteNumeric(id: String): ZIO[DataSource with CorrectionNumericRepository, Throwable, Unit]

    def deleteConstant(id: String): ZIO[DataSource with CorrectionConstantRepository, Throwable, Unit]

    def getNumericBy(id: String): ZIO[DataSource with CorrectionNumericRepository, Throwable, CorrectionNumeric]

    def getConstantBy(id: String): ZIO[DataSource with CorrectionConstantRepository, Throwable, CorrectionConstant]

    def getAllNumeric: ZIO[DataSource with CorrectionNumericRepository, SQLException, List[CorrectionNumeric]]

    def getAllConstant: ZIO[DataSource with CorrectionConstantRepository, SQLException, List[CorrectionConstant]]

    def updateNumeric(
        dto: UpdateNumericCorrectionDTO): ZIO[DataSource with CorrectionNumericRepository, SQLException, Unit]

    def updateConstant(
        dto: UpdateConstantCorrectionDTO): ZIO[DataSource with CorrectionConstantRepository, SQLException, Unit]

}

object CorrectionService {
    def createNumeric(
        dto: CreateNumericCorrectionDTO
    ): ZIO[DataSource with CorrectionNumericRepository with CorrectionService, SQLException, CorrectionNumeric] =
        ZIO.serviceWithZIO[CorrectionService](_.createNumeric(dto))

    def createConstant(dto: CreateConstantCorrectionDTO)
        : ZIO[DataSource with CorrectionConstantRepository with CorrectionService, SQLException, CorrectionConstant] =
        ZIO.serviceWithZIO[CorrectionService](_.createConstant(dto))

    def deleteNumeric(
        id: String): ZIO[DataSource with CorrectionNumericRepository with CorrectionService, Throwable, Unit] =
        ZIO.serviceWithZIO[CorrectionService](_.deleteNumeric(id))

    def deleteConstant(
        id: String): ZIO[DataSource with CorrectionConstantRepository with CorrectionService, Throwable, Unit] =
        ZIO.serviceWithZIO[CorrectionService](_.deleteConstant(id))

    def getNumericBy(id: String)
        : ZIO[DataSource with CorrectionNumericRepository with CorrectionService, Throwable, CorrectionNumeric] =
        ZIO.serviceWithZIO[CorrectionService](_.getNumericBy(id))

    def getConstantBy(id: String)
        : ZIO[DataSource with CorrectionConstantRepository with CorrectionService, Throwable, CorrectionConstant] =
        ZIO.serviceWithZIO[CorrectionService](_.getConstantBy(id))

    def getAllNumeric: ZIO[
      DataSource with CorrectionNumericRepository with CorrectionService,
      SQLException,
      List[CorrectionNumeric]] = ZIO.serviceWithZIO[CorrectionService](_.getAllNumeric)

    def getAllConstant: ZIO[
      DataSource with CorrectionConstantRepository with CorrectionService,
      SQLException,
      List[CorrectionConstant]] = ZIO.serviceWithZIO[CorrectionService](_.getAllConstant)

    def updateNumeric(dto: UpdateNumericCorrectionDTO)
        : ZIO[DataSource with CorrectionNumericRepository with CorrectionService, SQLException, Unit] =
        ZIO.serviceWithZIO[CorrectionService](_.updateNumeric(dto))

    def updateConstant(dto: UpdateConstantCorrectionDTO)
        : ZIO[DataSource with CorrectionConstantRepository with CorrectionService, SQLException, Unit] = ZIO.serviceWithZIO[CorrectionService](_.updateConstant(dto))

    def live: ULayer[CorrectionService] = CorrectionServiceLive.layer
}
