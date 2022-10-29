package object exceptions {
    sealed abstract class CustomException(message: String = "") extends Throwable(message)

    // Database exceptions
    class EntityNotFound(entityName: String, field: String, value: String)
        extends CustomException(s"Entity \"$entityName\" with $field = $value not found")

    case class UserNotFound(field: String, value: String) extends EntityNotFound("User", field, value)

    case class RealtyObjectNotFound(field: String, value: String) extends EntityNotFound("RealtyObject", field, value)

    case class BodyParsingException(dtoName: String) extends CustomException(s"Cannot parse body as $dtoName")

    case class DataValidationException(message: String) extends CustomException(message)

    case class InvalidIdException(entityName: String) extends CustomException(s"Invalid $entityName id")

    // Excel
    case class ExcelParsingException(message: String) extends CustomException(message)

    // Headers exception
    case class HeaderNotSetException(headerName: String) extends CustomException(s"Header $headerName is not set")

    // Authorization
    case class UserUnauthorizedException() extends CustomException("Unauthorized")

    case class NotEnoughRightsException(message: String) extends CustomException(message)
}
