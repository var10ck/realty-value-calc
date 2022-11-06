package dao.entities.corrections
import dao.entities.auth.UserId
import zio.{Random, Task, UIO, ZIO}
import zio.json.JsonCodec

import java.util.UUID
import scala.language.implicitConversions

case class CorrectionId (id: UUID) extends AnyVal

object CorrectionId{

    /** Generates a Random UUID and wraps it in the CorrectionId type. */
    def random: UIO[CorrectionId] = Random.nextUUID.map(CorrectionId(_))

    /** Allows a UUID to be parsed from a string which is then wrapped in the UserId type.
      */
    def fromString(id: String): Task[CorrectionId] =
        ZIO.attempt {
            CorrectionId(UUID.fromString(id))
        }

    /** Derives a codec allowing a UUID to be (de)serialized as an UserId. */
    implicit val codec: JsonCodec[CorrectionId] = JsonCodec[UUID].transform(CorrectionId(_), _.id)

//    implicit def stringToTypedId(id:String): CorrectionId = CorrectionId(UUID.fromString(id))
}