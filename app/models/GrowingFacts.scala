package models

import play.api.libs.json._

// An inclusive range, e.g. 10–15 cm. A single value is min == max.
case class IntRange(
    min: Int,
    max: Int,
)

object IntRange {
  implicit val format: OFormat[IntRange] = Json.format[IntRange]
}

// The fact box the book prints beside every plant (key on p. 243). One per plant,
// taken from the book (`bookPage`), or from another source (`otherSource`) for plants the book
// doesn't cover, such as apple and plum. Exactly one of the two is set. A range field is None when
// the book doesn't give it (e.g. no germination facts for plants grown from sets).
case class GrowingFacts(
    germinationDays: Option[IntRange], // sowing until the seedling shows
    minGerminationTempC: Option[Int], // lowest soil temperature the seed germinates at
    plantSpacingCm: Option[IntRange],
    rowSpacingCm: Option[IntRange],
    daysToHarvest: Option[IntRange], // sowing until harvest; longest in the north
    survivesDownToC: Option[IntRange], // frost it survives for a short time, e.g. -8 to -7
    heightCm: Option[IntRange], // grown height; the berry fact boxes give it
    swedishZones: Option[IntRange], // Swedish growing zones it can be grown in, e.g. 1-5 (hardiest variety the book recommends); perennials only
    goodIntercrop: Boolean, // fast enough to sow between slow crops
    sowInRounds: Boolean, // succession sowing works
    cutAndComeAgain: Boolean, // harvested in rounds most of the season
    bookPage: Option[Int],
    otherSource: Option[String], // e.g. "Svensk Trädgård faktablad 9; E-planta variety pages"
)

object GrowingFacts {
  implicit val format: OFormat[GrowingFacts] = Json.format[GrowingFacts]
}
