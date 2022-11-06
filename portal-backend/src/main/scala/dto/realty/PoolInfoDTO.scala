package dto.realty
import dao.entities.realty.{RealtyObjectPool, RealtyObjectPoolId}
import zio.json.JsonCodec

case class PoolInfoDTO(id: RealtyObjectPoolId, name: String)

object PoolInfoDTO {
    implicit val codec: JsonCodec[PoolInfoDTO] = zio.json.DeriveJsonCodec.gen[PoolInfoDTO]

    def fromEntity(pool: RealtyObjectPool): PoolInfoDTO = PoolInfoDTO(pool.id, pool.name)
}
