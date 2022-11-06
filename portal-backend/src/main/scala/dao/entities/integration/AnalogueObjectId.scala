package dao.entities.integration
import zio.{Random, Task, UIO, ZIO}
import zio.json.JsonCodec

import java.util.UUID

/** Primary key for RealtyObject entity */
case class AnalogueObjectId (id: UUID) extends AnyVal

object AnalogueObjectId{

    /** Generates a Random UUID and wraps it in the AnalogueObjectId type. */
    def random: UIO[AnalogueObjectId] = Random.nextUUID.map(AnalogueObjectId(_))

    /** Allows a UUID to be parsed from a string which is then wrapped in the AnalogueObjectId type.
     */
    def fromString(id: String): Task[AnalogueObjectId] =
        ZIO.attempt {
            AnalogueObjectId(UUID.fromString(id))
        }

    /** Derives a codec allowing a UUID to be (de)serialized as an AnalogueObjectId. */
    implicit val codec: JsonCodec[AnalogueObjectId] = JsonCodec[UUID].transform(AnalogueObjectId(_), _.id)
}