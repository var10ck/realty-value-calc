package object exceptions {
    abstract class CustomException(message: String = "") extends Throwable(message)

    class EntityNotFound(entityName:String, field:String, value:String)
        extends CustomException(s"Entity \"$entityName\" with $field = $value not found")

    case class UserNotFound(field:String, value:String) extends EntityNotFound("User", field, value)

    case class BodyParsingException(dtoName:String)
        extends CustomException(s"Cannot parse body as $dtoName")
}
