package object exceptions {
    sealed abstract class CustomException(message: String = "") extends Throwable(message)

    // Database exceptions
    class EntityNotFound(entityName: String, field: String, value: String)
        extends CustomException(s"Entity \"$entityName\" with $field = $value not found")

    case class UserNotFound(field: String, value: String) extends EntityNotFound("User", field, value)

    case class RealtyObjectNotFound(field: String, value: String) extends EntityNotFound("RealtyObject", field, value)

    case class RealityObjectsPoolNotFound(field: String, value: String) extends EntityNotFound("RealityObjectsPool", field, value)

    case class CorrectionNotFound(field: String, value: String) extends EntityNotFound("Correction", field, value)

    // Invalid input
    case class BodyParsingException() extends CustomException(s"Invalid body content")

    case class DataValidationException(message: String) extends CustomException(message)

    case class InvalidIdException(entityName: String) extends CustomException(s"Invalid $entityName id")

    // Excel
    case class ExcelParsingException(message: String) extends CustomException(message)

    case class ExcelWritingException(message:String) extends CustomException(message)

    // Headers exception
    case class HeaderNotSetException(headerName: String) extends CustomException(s"Header $headerName is not set")

    // Authorization
    case class UserUnauthorizedException() extends CustomException("Unauthorized")

    case class NotEnoughRightsException(message: String) extends CustomException(message)
}
