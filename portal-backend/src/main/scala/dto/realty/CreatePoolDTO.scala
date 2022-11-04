package dto.realty
import zio.json.JsonCodec

case class CreatePoolDTO (name: Option[String])

object CreatePoolDTO{
    implicit val codec: JsonCodec[CreatePoolDTO] = zio.json.DeriveJsonCodec.gen[CreatePoolDTO]
}
