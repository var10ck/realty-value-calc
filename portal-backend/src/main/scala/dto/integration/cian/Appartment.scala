package dto.integration.cian

import zio.json._

/** Поля апартаментов составлены на основе приложения к ТЗ
  */
case class Apartment(
    aggregatorID: Option[Int],
    URL: Option[String],
    location: Option[Coordinates],
    price: Option[Int],
    roomsNumber: Option[Int],
    apartmentsType: Option[String],
    material: Option[String],
    floor: Option[Int],
    area: Option[Double],
    kitchenArea: Option[Double],
    balcony: Boolean,
    distanceToMetro: Seq[WayToMetro],
    condition: Option[String],
    coordinates: HouseParameters){

    override def toString: String = {
        s"""aggregatorID: $aggregatorID
           |url: $URL,
           |location: $location,
           |price: $price,
           |roomsNumber: $roomsNumber
           |apartmentsType: $apartmentsType
           |material: $material
           |floor: $floor
           |area: $area
           |kitchenArea: $kitchenArea
           |balcony: $balcony
           |distanceToMetro: $distanceToMetro
           |condition: $condition
           |coordinates: $coordinates
           |""".stripMargin
    }
}

case class WayToMetro(minutes: Option[Int], metro: Option[String], walkType: Option[String])

object WayToMetro {
    implicit val encoder: JsonEncoder[WayToMetro] = DeriveJsonEncoder.gen[WayToMetro]
    implicit val decoder: JsonDecoder[WayToMetro] = DeriveJsonDecoder.gen[WayToMetro]
}

object Apartment {
    implicit val encoder: JsonEncoder[Apartment] = DeriveJsonEncoder.gen[Apartment]
    implicit val decoder: JsonDecoder[Apartment] = DeriveJsonDecoder.gen[Apartment]
}

case class Coordinates(lng: Double, lat: Double)

object Coordinates {
    implicit val encoder: JsonEncoder[Coordinates] = DeriveJsonEncoder.gen[Coordinates]
    implicit val decoder: JsonDecoder[Coordinates] = DeriveJsonDecoder.gen[Coordinates]
}

case class HouseParameters(floors: Option[Int], params: Seq[Param])

object HouseParameters {
    implicit val encoder: JsonEncoder[HouseParameters] = DeriveJsonEncoder.gen[HouseParameters]
    implicit val decoder: JsonDecoder[HouseParameters] = DeriveJsonDecoder.gen[HouseParameters]
}

case class Param(paramType: Option[String], title: Option[String])

object Param {
    implicit val encoder: JsonEncoder[Param] = DeriveJsonEncoder.gen[Param]
    implicit val decoder: JsonDecoder[Param] = DeriveJsonDecoder.gen[Param]
}
