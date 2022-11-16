package dto.corrections
import dao.entities.corrections.CorrectionId
import zio.json.JsonCodec

case class UpdateConstantCorrectionDTO(
    id: CorrectionId,
    fieldName: Option[String] = None,
    referenceValue: Option[String] = None,
    analogueValue: Option[String] = None,
    correction: Option[Int] = None,
    correctionType: Option[String] = None,
    isEnabled: Option[Boolean] = None
)

object UpdateConstantCorrectionDTO {
    implicit val codec: JsonCodec[UpdateConstantCorrectionDTO] = zio.json.DeriveJsonCodec.gen[UpdateConstantCorrectionDTO]
}
