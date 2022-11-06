package dto.corrections
import dao.entities.corrections.CorrectionId
import zio.json.JsonCodec

case class UpdateNumericCorrectionDTO(
    id: CorrectionId,
    fieldName: Option[String] = None,
    referenceValue: Option[String] = None,
    referenceValueType: Option[Int] = None,
    analogueValue: Option[String] = None,
    analogueValueType: Option[Int] = None,
    correction: Option[Int] = None,
    correctionType: Option[String] = None
)

object UpdateNumericCorrectionDTO {
    implicit val codec: JsonCodec[UpdateNumericCorrectionDTO] = zio.json.DeriveJsonCodec.gen[UpdateNumericCorrectionDTO]
}
