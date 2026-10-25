package models

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

// The growing climate for one location. One row per location, so a second
// garden somewhere else is a new row, not a schema change.
case class Climate(
    _id: ObjectId = new ObjectId(),
    location: String,
    hardinessZones: List[HardinessZone],
    lastSpringFrost: FrostWindow,
    firstAutumnFrost: FrostWindow,
    vegetationPeriod: VegetationPeriod,
    frozenGroundMonths: List[MonthName],
    snowCoverMonths: List[MonthName],
    outdoorSowingFrom: MonthDay, // once the soil has thawed and is workable
    notes: List[String],
)

object Climate {
  implicit val format: OFormat[Climate] = Json.format[Climate]
}

// A zone from one zone system. `source` records where the value came from.
case class HardinessZone(
    system: HardinessSystem,
    value: String, // "3", or a range like "1-2"
    source: Option[String],
)

object HardinessZone {
  implicit val format: OFormat[HardinessZone] = Json.format[HardinessZone]
}

// A day in the year with no year attached, e.g. 15 May.
case class MonthDay(
    month: MonthName,
    day: Int,
)

object MonthDay {
  implicit val format: OFormat[MonthDay] = Json.format[MonthDay]
}

// When a frost usually happens: a window, not a single date. `confirmed` is false
// until an official source backs it up.
case class FrostWindow(
    from: MonthDay,
    to: MonthDay,
    confirmed: Boolean,
    source: Option[String],
)

object FrostWindow {
  implicit val format: OFormat[FrostWindow] = Json.format[FrostWindow]
}

// SMHI's vegetation period: days with mean temperature above +5°C. Longer than
// the frost-free period, so don't use it as the safe outdoor window.
case class VegetationPeriod(
    minDays: Int,
    maxDays: Int,
    source: Option[String],
)

object VegetationPeriod {
  implicit val format: OFormat[VegetationPeriod] = Json.format[VegetationPeriod]
}
