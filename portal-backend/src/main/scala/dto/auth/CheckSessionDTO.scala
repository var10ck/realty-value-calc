package dto.auth

case class CheckSessionDTO (sessionId: String)

object CheckSessionDTO{
    implicit val codec = zio.json.DeriveJsonCodec.gen[CheckSessionDTO]
}
