import aggregatorParser.CianParser
import zio._

object Main extends ZIOAppDefault {
    override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] = for {
        json <- CianParser.getPageApartmentsJson(2)
        apartments <- CianParser.parseJson(json)
        _ <- Console.printLine(apartments)
    } yield ()

}
