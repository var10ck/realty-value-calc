package helpers.aggregatorParser

import dto.integration.cian._
import io.circe.parser._
import io.circe.{HCursor, Json}
import zio.{Task, ZIO}

import scala.util.Try

object CianParser extends AggregatorParser {

    def parseJsonString(jsonString:String): Task[Seq[Apartment]] =
        for{
            json <- ZIO.fromEither(parse(jsonString))
            apartments <- parseJson(json)
        } yield apartments

    def parseJson(json: Json): Task[Seq[Apartment]] = {

        val cursor: HCursor = json.hcursor

        val seqOffers = cursor.downField("data").downField("offersSerialized").as[Seq[Json]].getOrElse(Seq.empty[Json])
        val seqApartments = seqOffers map { json =>
            val under = json.hcursor.downField("geo").downField("undergrounds").as[Seq[Json]].getOrElse(Seq.empty[Json])
            val address = json.hcursor.downField("geo").downField("address").as[Seq[Json]].getOrElse(Seq.empty[Json])

            def partialApplyFunc[A](f: Json => Option[A]): Option[A] = tryParseField(json)(f).flatten

            Apartment(
              partialApplyFunc(_.hcursor.downField("cianId").as[Int].toOption),
              partialApplyFunc(_.hcursor.downField("fullUrl").as[String].toOption),
              (for {
                  lng <- json.hcursor.downField("geo").downField("coordinates").downField("lng").as[Double]
                  lat <- json.hcursor.downField("geo").downField("coordinates").downField("lat").as[Double]
              } yield Coordinates(lng, lat)).toOption,
              partialApplyFunc(_.hcursor.downField("bargainTerms").downField("priceRur").as[Int].toOption),
              partialApplyFunc(_.hcursor.downField("roomsCount").as[Int].toOption),
              partialApplyFunc(_.hcursor.downField("category").as[String].toOption),
              partialApplyFunc(_.hcursor.downField("building").downField("materialType").as[String].toOption),
              partialApplyFunc(_.hcursor.downField("floorNumber").as[Int].toOption),
              partialApplyFunc(_.hcursor.downField("totalArea").as[Double].toOption),
              partialApplyFunc(_.hcursor.downField("kitchenArea").as[Double].toOption),
              partialApplyFunc(_.hcursor.downField("balconiesCount").as[String].toOption).isDefined,
              under map { jsUnder =>
                  WayToMetro(
                    jsUnder.hcursor.downField("time").as[Int].toOption,
                    jsUnder.hcursor.downField("name").as[String].toOption,
                    jsUnder.hcursor.downField("transportType").as[String].toOption
                  )
              },
              partialApplyFunc(_.hcursor.downField("decoration").as[String].toOption),
              HouseParameters(
                partialApplyFunc(_.hcursor.downField("building").downField("floorsCount").as[Int].toOption),
                address map { add =>
                    Param(
                      add.hcursor.downField("type").as[String].toOption,
                      add.hcursor.downField("title").as[String].toOption
                    )
                }
              )
            )
        }

        ZIO.attempt(seqApartments)

    }

    private def tryParseField[A](json: Json)(f: Json => A): Option[A] = Try(f(json)).toOption

}
