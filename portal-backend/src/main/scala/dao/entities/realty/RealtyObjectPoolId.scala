package dao.entities.realty

import zio.json.JsonCodec
import zio.{Random, Task, UIO, ZIO}

import java.util.UUID

/** Primary key for RealtyObjectPoolId entity */
case class RealtyObjectPoolId (id: UUID) extends AnyVal

object RealtyObjectPoolId{

    /** Generates a Random UUID and wraps it in the RealtyObjectPoolId type. */
    def random: UIO[RealtyObjectPoolId] = Random.nextUUID.map(RealtyObjectPoolId.apply)

    /** Allows a UUID to be parsed from a string which is then wrapped in the RealtyObjectPoolId type.
     */
    def fromString(id: String): Task[RealtyObjectPoolId] =
        ZIO.attempt {
            RealtyObjectPoolId(UUID.fromString(id))
        }

    /** Derives a codec allowing a UUID to be (de)serialized as an RealtyObjectPoolId. */
    implicit val codec: JsonCodec[RealtyObjectPoolId] = JsonCodec[UUID].transform(RealtyObjectPoolId.apply, _.id)
}
