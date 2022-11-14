package dto.realty
import dao.entities.realty.RealtyObjectId
import zio.json.{DeriveJsonCodec, JsonCodec}

case class ExportSomeObjectsDTO (objectIds: List[RealtyObjectId])

object ExportSomeObjectsDTO{
    implicit val codec: JsonCodec[ExportSomeObjectsDTO] = DeriveJsonCodec.gen[ExportSomeObjectsDTO]
}