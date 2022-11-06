package api
import dto.corrections.{CreateNumericCorrectionDTO, UpdateNumericCorrectionDTO}
import helpers.AuthHelper.{withUserContextAndDtoZIO, withUserContextZIO}
import helpers.HttpExceptionHandlers._
import services.CorrectionService
import zhttp.http.{Http, Method, Request, Response, Status}
import zhttp.http._
import zio.json.EncoderOps

object CorrectionApi {

    val api = Http.collectZIO[Request] {
        case req @ Method.PUT -> !! / "realty" / "corrections" / "numeric" =>
            withUserContextAndDtoZIO(req) { (user, dto: CreateNumericCorrectionDTO) =>
                dto match {
                    case createNumericDTO: CreateNumericCorrectionDTO =>
                        CorrectionService.createNumeric(createNumericDTO)
                }
            }.fold(
              correctionExceptionsHandler,
              _ => Response.ok
            )

        case req @ Method.PATCH -> !! / "realty" / "corrections" / "numeric" =>
            withUserContextAndDtoZIO(req) { (_, dto: UpdateNumericCorrectionDTO) =>
                CorrectionService.updateNumeric(dto)
            }.fold(
              correctionExceptionsHandler,
              _ => Response.ok
            )

        case req @ Method.DELETE -> !! / "realty" / "corrections" / "numeric" / correctionId =>
            withUserContextZIO(req) { _ => CorrectionService.deleteNumeric(correctionId) }
                .fold(
                  correctionExceptionsHandler,
                    _ => Response.ok
                )

        case req @ Method.GET -> !! / "realty" / "corrections" / "numeric" =>
            withUserContextZIO(req) { _ =>
                CorrectionService.getAllNumeric
            }.fold(
              correctionExceptionsHandler,
              corrections => Response.json(corrections.toJson)
            )
    }

    private val correctionExceptionsHandler =
        basicAuthExceptionHandler orElse bodyParsingExceptionHandler orElse lastResortHandler

}
