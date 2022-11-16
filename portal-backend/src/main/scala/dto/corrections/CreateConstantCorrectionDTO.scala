package dto.corrections
import zio.json.{DeriveJsonCodec, JsonCodec}

case class CreateConstantCorrectionDTO(
    fieldName: String,
    referenceValue: String,
    analogueValue: String,
    correction: Int,
    correctionType: String,
    isEnabled: Boolean)

object CreateConstantCorrectionDTO {
    implicit val codec: JsonCodec[CreateConstantCorrectionDTO] = DeriveJsonCodec.gen[CreateConstantCorrectionDTO]
}
