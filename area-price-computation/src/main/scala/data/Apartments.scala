package data

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
    coordinates: HouseParameters)

case class WayToMetro(minutes: Option[Int], metro: Option[String], walkType: Option[String])

object WayToMetro {
    implicit val encoder: JsonEncoder[WayToMetro] = DeriveJsonEncoder.gen[WayToMetro]
    implicit val decoder: JsonDecoder[WayToMetro] = DeriveJsonDecoder.gen[WayToMetro]
}

object Apartment {
    implicit val encoder: JsonEncoder[Apartment] = DeriveJsonEncoder.gen[Apartment]
    implicit val decoder: JsonDecoder[Apartment] = DeriveJsonDecoder.gen[Apartment]
}

sealed trait ApartmentsType

case object NewBuilding extends ApartmentsType

case object OldBuilding extends ApartmentsType

object ApartmentsType {
    implicit val encoder: JsonEncoder[ApartmentsType] = DeriveJsonEncoder.gen[ApartmentsType]
    implicit val decoder: JsonDecoder[ApartmentsType] = DeriveJsonDecoder.gen[ApartmentsType]
}

sealed trait Material

case object Brick extends Material

case object Monolith extends Material

case object Panel extends Material

object Material {
    implicit val encoder: JsonEncoder[Material] = DeriveJsonEncoder.gen[Material]
    implicit val decoder: JsonDecoder[Material] = DeriveJsonDecoder.gen[Material]
}

sealed trait Condition

case object NotFinished extends Condition

case object NewRenovation extends Condition

case object OldRenovation extends Condition

object Condition {
    implicit val encoder: JsonEncoder[Condition] = DeriveJsonEncoder.gen[Condition]
    implicit val decoder: JsonDecoder[Condition] = DeriveJsonDecoder.gen[Condition]
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
