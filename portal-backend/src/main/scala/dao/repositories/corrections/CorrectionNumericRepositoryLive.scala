package dao.repositories.corrections
import dao.entities.corrections.{CorrectionId, CorrectionNumeric}
import io.getquill.context.ZioJdbc.QIO
import zio.{ULayer, ZLayer}
import zio.metrics.Metric

final case class CorrectionNumericRepositoryLive() extends CorrectionNumericRepository {

    import db.Ctx._

    override def create(
        fieldName: String,
        referenceValue: String,
        referenceValueType: Int,
        analogueValue: String,
        analogueValueType: Int,
        correction: Int,
        correctionType: String): QIO[CorrectionNumeric] = {
        for {
            correction <- CorrectionNumeric.make(
              fieldName,
              referenceValue,
              referenceValueType,
              analogueValue,
              analogueValueType,
              correction,
              correctionType)
            _ <- run(query[CorrectionNumeric].insertValue(lift(correction)))
            _ <- Metric.counter("correctionNumeric.created").increment
        } yield correction
    }

    override def getBy(id: CorrectionId): QIO[Option[CorrectionNumeric]] =
        run(query[CorrectionNumeric].filter(_.id == lift(id))).map(_.headOption)

    override def getAll: QIO[List[CorrectionNumeric]] = run(query[CorrectionNumeric]).map(_.toList)

    override def delete(id: CorrectionId): QIO[Unit] = run(
      query[CorrectionNumeric].filter(_.id == lift(id)).delete).unit

    override def update(
        id: CorrectionId,
        fieldName: Option[String] = None,
        referenceValue: Option[String] = None,
        referenceValueType: Option[Int] = None,
        analogueValue: Option[String] = None,
        analogueValueType: Option[Int] = None,
        correction: Option[Int] = None,
        correctionType: Option[String] = None): QIO[Unit] =
        run(
          dynamicQuery[CorrectionNumeric]
              .filter(_.id == lift(id))
              .update(
                setOpt(_.fieldName, fieldName),
                setOpt(_.referenceValue, referenceValue),
                setOpt(_.referenceValueType, referenceValueType),
                setOpt(_.analogueValue, analogueValue),
                setOpt(_.analogueValueType, analogueValueType),
                setOpt(_.correction, correction),
                setOpt(_.correctionType, correctionType)
              )
        ).unit
}

object CorrectionNumericRepositoryLive {
    val layer: ULayer[CorrectionNumericRepository] = ZLayer.succeed(CorrectionNumericRepositoryLive())
}
