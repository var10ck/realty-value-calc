package dto.corrections
import dao.entities.corrections.CorrectionId
import zio.json.JsonCodec

case class UpdateCorrectionDTO(
    id: CorrectionId,
    fieldName: Option[String] = None,
    referenceValue: Option[String] = None,
    referenceValueType: Option[Int] = None,
    analogueValue: Option[String] = None,
    analogueValueType: Option[Int] = None,
    correction: Option[Int] = None,
    correctionType: Option[String] = None
)

object UpdateCorrectionDTO {
    implicit val codec: JsonCodec[UpdateCorrectionDTO] = zio.json.DeriveJsonCodec.gen[UpdateCorrectionDTO]
}
