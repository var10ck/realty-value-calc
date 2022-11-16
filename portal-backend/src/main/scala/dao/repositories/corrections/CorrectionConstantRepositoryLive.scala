package dao.repositories.corrections
import dao.entities.corrections.{CorrectionConstant, CorrectionId}
import io.getquill.context.ZioJdbc.QIO
import zio.{ULayer, ZLayer}
import zio.metrics.Metric

final case class CorrectionConstantRepositoryLive () extends CorrectionConstantRepository{

    import db.Ctx._

    override def create(
        fieldName: String,
        referenceValue: String,
        analogueValue: String,
        correction: Int,
        correctionType: String,
        isEnabled: Boolean)
        : QIO[CorrectionConstant] = for {
            correction <- CorrectionConstant.make(
              fieldName,
              referenceValue,
              analogueValue,
              correction,
              correctionType,
              isEnabled)
            _ <- run(query[CorrectionConstant].insertValue(lift(correction)))
            _ <- Metric.counter("CorrectionConstant.created").increment
        } yield correction

    override def getBy(
        id: CorrectionId): QIO[Option[
      CorrectionConstant]] = run(query[CorrectionConstant].filter(_.id == lift(id))).map(_.headOption)

    override def getAll: QIO[
      List[CorrectionConstant]] = run(query[CorrectionConstant]).map(_.toList)

    override def getAllEnabled: QIO[
      List[CorrectionConstant]] = run(query[CorrectionConstant].filter(_.isEnabled)).map(_.toList)

    override def delete(
        id: CorrectionId): QIO[Unit] = run(
      query[CorrectionConstant].filter(_.id == lift(id)).delete).unit

    override def update(
        id: CorrectionId,
        fieldName: Option[String],
        referenceValue: Option[String],
        analogueValue: Option[String],
        correction: Option[Int],
        correctionType: Option[String],
        isEnabled: Option[Boolean]): QIO[Unit] = run(
          dynamicQuery[CorrectionConstant]
              .filter(_.id == lift(id))
              .update(
                setOpt(_.fieldName, fieldName),
                setOpt(_.referenceValue, referenceValue),
                setOpt(_.analogueValue, analogueValue),
                setOpt(_.correction, correction),
                setOpt(_.correctionType, correctionType),
                setOpt(_.isEnabled, isEnabled)
              )
        ).unit
}

object CorrectionConstantRepositoryLive{
    val layer: ULayer[CorrectionConstantRepository] = ZLayer.succeed(CorrectionConstantRepositoryLive())
}