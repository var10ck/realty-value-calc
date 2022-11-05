import aggregatorParser.CianParser
import data.Apartment
import zio._

object Main extends ZIOAppDefault {
    override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] = for {
//        json <- CianParser.getPageApartmentsJson(1)
//        apartments <- CianParser.parseJson(json)
        apartments <- iterateAllApartments
        _ <- Console.printLine(apartments)
    } yield ()

    def iterateAllApartments: ZIO[Any, Throwable, Seq[Apartment]] = {
        def loop(apartments: List[Apartment], refPage: Ref[Int]): ZIO[Any, Throwable, Seq[Apartment]] = {
            refPage
                .getAndUpdate(_ + 1)
                .flatMap(CianParser.getPageApartmentsJson)
                .flatMap(CianParser.parseJson)
                .flatMap {
                    case Nil => ZIO.attempt(apartments)
                    case seq => loop(apartments ++ seq, refPage)
                }
        }
        for {
            ref <- Ref.make(1)
            apartments <- loop(List.empty[Apartment], ref)
//            list = List.fill(10)(loop(List.empty[Apartment], ref))
//            apartments <- ZIO.collectAllPar(list)
        } yield apartments
//            .flatten
    }

}
