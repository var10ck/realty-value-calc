package dto.corrections
import zio.json.{JsonCodec, jsonHint}

//sealed abstract class CreateCorrectionDTO

case class CreateNumericCorrectionDTO(
    fieldName: String,
    referenceValue: String,
    referenceValueType: Int,
    analogueValue: String,
    analogueValueType: Int,
    correction: Int,
    correctionType: String
)

//object CreateCorrectionDTO{
//    implicit val codec: JsonCodec[CreateCorrectionDTO] = zio.json.DeriveJsonCodec.gen[CreateCorrectionDTO]
//}

object CreateNumericCorrectionDTO {
    implicit val codec: JsonCodec[CreateNumericCorrectionDTO] = zio.json.DeriveJsonCodec.gen[CreateNumericCorrectionDTO]
}
