package dao.entities.realty
import zio.ZIO
import zio.json.{DeriveJsonCodec, JsonCodec}

import java.time.format.DateTimeFormatter

case class RealtyObjectPool(
    id: RealtyObjectPoolId,
    name: String
)

object RealtyObjectPool {
    implicit val codec: JsonCodec[RealtyObject] = DeriveJsonCodec.gen[RealtyObject]

    def make(name:String): ZIO[Any, Nothing, RealtyObjectPool] = RealtyObjectPoolId.random.map(RealtyObjectPool(_, name))

    def make: ZIO[Any, Nothing, RealtyObjectPool] = {
        val dateStr = java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        val name = s"pool-$dateStr"
        RealtyObjectPoolId.random.map(RealtyObjectPool(_, name))
    }
}
