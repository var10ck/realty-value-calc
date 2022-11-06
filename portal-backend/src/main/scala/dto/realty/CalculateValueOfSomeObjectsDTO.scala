package dto.realty
import zio.json.JsonCodec

case class CalculateValueOfSomeObjectsDTO(objectsIds: List[String])

object CalculateValueOfSomeObjectsDTO {
    implicit val codec: JsonCodec[CalculateValueOfSomeObjectsDTO] =
        zio.json.DeriveJsonCodec.gen[CalculateValueOfSomeObjectsDTO]
}
