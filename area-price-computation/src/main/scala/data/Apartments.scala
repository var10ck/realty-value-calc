package data

import zio.json._

/** Поля апартаментов составлены на основе приложения к ТЗ
  */
case class Apartment(
    location: String,
    roomsNumber: Int,
    apartmentsType: ApartmentsType,
    material: Material,
    floors: Int,
    area: Double,
    kitchenArea: Double,
    balcony: Boolean,
    distanceToMetro: Double,
    condition: Condition)

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
