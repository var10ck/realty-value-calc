package helpers
import dao.entities.corrections.CorrectionNumeric
import dao.entities.integration.AnalogueObject
import dao.entities.realty.RealtyObject
import dto.integration.cian.Apartment

object CorrectionHelper {

    // noinspection SpellCheckingInspection
    /** Translate numeric correction (that means correction of square meter price based on total area, kitchen area,
      * floor number or distance from metro) representation stored in DB into function, that can be applied to appraised
      * realty object and matched analogue object
      */
    def correctionNumericToFunction(correction: CorrectionNumeric): (RealtyObject, Apartment) => Double => Double =
        (realtyObject: RealtyObject, analogueObject: Apartment) =>
            (price: Double) => {
                val (objectField: Double, analogueField: Option[Double]) = correction.fieldName.toLowerCase match {
                    case "kitchenarea" => (realtyObject.kitchenArea, analogueObject.kitchenArea)
                    case "totalarea" => (realtyObject.totalArea, analogueObject.area)
                    case "floornumber" => (realtyObject.floorNumber.toDouble, analogueObject.coordinates.floors.map(_.toDouble))
                    case "distancefrommetro" =>
                        (realtyObject.distanceFromMetro.toDouble,
                        analogueObject.distanceToMetro.headOption.flatMap(_.minutes).map(_.toDouble))
                }

                val referenceValueFunc: Double => Boolean =
                    valueTypeToFunc(correction.referenceValueType)(correction.referenceValue)
                val analogueValueFunc: Double => Boolean =
                    valueTypeToFunc(correction.analogueValueType)(correction.analogueValue)

                if (analogueField.exists(p => analogueValueFunc(p)) && referenceValueFunc(objectField))
                    correctionTypeToFunc(correction.correctionType)(correction.correction.toDouble)(price)
                else price
            }

    def valueTypeToFunc(valueType: Int): String => Double => Boolean = Map(
      1 -> inRangeStr _,
      2 -> lessThenStr _,
      3 -> lessThenOrEqualStr _,
      4 -> greaterThenStr _,
      5 -> greaterThenOrEqualStr _,
      6 -> equal _
    )(valueType)

    def correctionTypeToFunc(correctionType: String)(correctionValue: Double): Double => Double = Map(
      "percent" -> ((price: Double) => price + (price * (correctionValue / 100))),
      "unit" -> ((price: Double) => price + correctionValue)
    )(correctionType.toLowerCase)

    def inRangeStr(range: String): Double => Boolean = (value: Double) => {
        val numbers = range.split("-", 2)
        val (l: Int, r: Int) = (numbers(0).toInt, numbers(1).toInt)
        value >= l && value <= r
    }

    def lessThenStr(compareWith: String): Double => Boolean = (value: Double) => value < compareWith.toDouble

    def lessThenOrEqualStr(compareWith: String): Double => Boolean = (value: Double) => value <= compareWith.toDouble

    def greaterThenStr(compareWith: String): Double => Boolean = (value: Double) => value > compareWith.toDouble

    def greaterThenOrEqualStr(compareWith: String): Double => Boolean = (value: Double) => value >= compareWith.toDouble

    def equal(compareWith: String): Double => Boolean = (value: Double) => value == compareWith.toDouble

}
