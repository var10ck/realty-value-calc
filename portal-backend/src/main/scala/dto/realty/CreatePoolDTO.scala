package dto.realty
import zio.json.JsonCodec

case class CreatePoolDTO (name: String)

object CreatePoolDTO{
    implicit val codec: JsonCodec[CreatePoolDTO] = zio.json.DeriveJsonCodec.gen[CreatePoolDTO]
}
