package dto.integration
import io.circe.Codec
import io.circe.generic.semiauto._

case class SuggestionRequestDTO(query: String, count: Int = 1)

object SuggestionRequestDTO {
    implicit val codec: Codec[SuggestionRequestDTO] = deriveCodec
}
