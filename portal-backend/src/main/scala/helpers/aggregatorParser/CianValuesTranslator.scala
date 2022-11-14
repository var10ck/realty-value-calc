package helpers.aggregatorParser

object CianValuesTranslator {

    def wallMaterialTranslate(s: String): String = Map(
      "кирпич" -> "brick",
      "монолит" -> "monolith",
      "панель" -> "panel",
      "смешанный" -> "monolithBrick"
    )(s.toLowerCase)

    def segmentTranslate(s: String): String = Map(
      "новостройка" -> "newBuildingFlatSale",
      "современное жилье" -> "newBuildingFlatSale",
      "старый жилой фонд" -> "flatSale"
    )(s.toLowerCase)
}
