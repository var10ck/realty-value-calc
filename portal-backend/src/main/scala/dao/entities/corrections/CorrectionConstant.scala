package dao.entities.corrections
import zio.ZIO
import zio.json.JsonCodec

case class CorrectionConstant(
    id: CorrectionId,
    fieldName: String,
    referenceValue: String,
    analogueValue: String,
    correction: Int,
    correctionType: String,
    isEnabled: Boolean
) extends Correction

object CorrectionConstant {
    implicit val codec: JsonCodec[CorrectionConstant] = zio.json.DeriveJsonCodec.gen[CorrectionConstant]

    def make(
        fieldName: String,
        referenceValue: String,
        analogueValue: String,
        correction: Int,
        correctionType: String,
        isEnabled: Boolean): ZIO[Any, Nothing, CorrectionConstant] =
        CorrectionId.random.map(
          CorrectionConstant(
            _,
            fieldName,
            referenceValue,
            analogueValue,
            correction,
            correctionType,
            isEnabled
          )
        )
}
