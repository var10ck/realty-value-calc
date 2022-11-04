package dao.entities.realty
import dao.entities.auth.UserId
import zio.ZIO
import zio.json.{DeriveJsonCodec, JsonCodec}

import java.time.format.DateTimeFormatter

case class RealtyObjectPool(
    id: RealtyObjectPoolId,
    name: String,
    userId: UserId
)

object RealtyObjectPool {
    implicit val codec: JsonCodec[RealtyObjectPool] = DeriveJsonCodec.gen[RealtyObjectPool]

    def make(name:String, userId: UserId): ZIO[Any, Nothing, RealtyObjectPool] = RealtyObjectPoolId.random.map(RealtyObjectPool(_, name, userId))

    def makeName: String = {
        val dateStr = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss"))
        s"pool-$dateStr"
    }

    def make(userId: UserId): ZIO[Any, Nothing, RealtyObjectPool] = {
        val dateStr = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss"))
        val name = s"pool-$dateStr"
        RealtyObjectPoolId.random.map(RealtyObjectPool(_, name, userId))
    }
}
