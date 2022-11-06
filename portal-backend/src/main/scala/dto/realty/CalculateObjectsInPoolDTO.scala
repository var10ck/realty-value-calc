package dto.realty
import zio.json.JsonCodec

case class CalculateObjectsInPoolDTO(poolId: String, withCorrections: Boolean)

object CalculateObjectsInPoolDTO {
    implicit val codec: JsonCodec[CalculateObjectsInPoolDTO] = zio.json.DeriveJsonCodec.gen[CalculateObjectsInPoolDTO]
}
