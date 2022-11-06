package dao.entities.corrections
import zio.ZIO
import zio.json.JsonCodec

case class CorrectionNumeric(
    id: CorrectionId,
    fieldName: String,
    referenceValue: String,
    referenceValueType: Int,
    analogueValue: String,
    analogueValueType: Int,
    correction: Int,
    correctionType: String
) extends Correction

object CorrectionNumeric {

    implicit val codec: JsonCodec[CorrectionNumeric] = zio.json.DeriveJsonCodec.gen[CorrectionNumeric]

    def make(
        fieldName: String,
        referenceValue: String,
        referenceValueType: Int,
        analogueValue: String,
        analogueValueType: Int,
        correction: Int,
        correctionType: String): ZIO[Any, Nothing, CorrectionNumeric] =
        CorrectionId.random.map(
          CorrectionNumeric(
            _,
            fieldName,
            referenceValue,
            referenceValueType,
            analogueValue,
            analogueValueType,
            correction,
            correctionType))

    lazy val valueTypes: Map[String, Int] = Map(
      "range" -> 1,
      "lt" -> 2,
      "lte" -> 3,
      "gt" -> 4,
      "gte" -> 5,
      "eq" -> 6
    )

    lazy val correctionTypes: Map[String, Int] = Map(
      "percent" -> 1,
      "unit" -> 2
    )
}
