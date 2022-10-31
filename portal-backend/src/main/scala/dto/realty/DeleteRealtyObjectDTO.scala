package dto.realty
import zio.json.JsonCodec

case class DeleteRealtyObjectDTO (realtyObjectId: String)

object DeleteRealtyObjectDTO{
    implicit val codec: JsonCodec[DeleteRealtyObjectDTO] = zio.json.DeriveJsonCodec.gen[DeleteRealtyObjectDTO]
}
