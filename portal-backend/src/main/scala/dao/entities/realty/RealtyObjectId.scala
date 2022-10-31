package dao.entities.realty
import zio.json.JsonCodec
import zio.{Random, Task, UIO, ZIO}

import java.util.UUID

/** Primary key for RealtyObject entity */
case class RealtyObjectId (id: UUID) extends AnyVal

object RealtyObjectId{

    /** Generates a Random UUID and wraps it in the RealtyObjectId type. */
    def random: UIO[RealtyObjectId] = Random.nextUUID.map(RealtyObjectId(_))

    /** Allows a UUID to be parsed from a string which is then wrapped in the RealtyObjectId type.
      */
    def fromString(id: String): Task[RealtyObjectId] =
        ZIO.attempt {
            RealtyObjectId(UUID.fromString(id))
        }

    /** Derives a codec allowing a UUID to be (de)serialized as an RealtyObjectId. */
    implicit val codec: JsonCodec[RealtyObjectId] = JsonCodec[UUID].transform(RealtyObjectId(_), _.id)
}
