package dto.integration
import io.circe.{Decoder, Encoder, HCursor}
import io.circe.generic.semiauto._

case class SuggestionResponseDTO(lat: String, lon: String)


object SuggestionResponseDTO {

    implicit val encoder: Encoder[SuggestionResponseDTO] = deriveEncoder

    implicit val decoder: Decoder[SuggestionResponseDTO] = new Decoder[SuggestionResponseDTO] {
        final def apply(c: HCursor): Decoder.Result[SuggestionResponseDTO] =
            for {
                lon <- c.downField("suggestions").downArray.downField("data").downField("geo_lon").as[String]
                lat <- c.downField("suggestions").downArray.downField("data").downField("geo_lat").as[String]
            } yield {
                new SuggestionResponseDTO(lat, lon)
            }
    }

}