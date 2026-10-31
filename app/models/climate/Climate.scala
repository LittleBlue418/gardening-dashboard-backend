package models
package climate

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._
import models.common._

// The growing climate for one location. One row per location, so a second
// garden somewhere else is a new row, not a schema change.
case class Climate(
    _id: ObjectId = new ObjectId(),
    location: String,
    hardinessZones: List[HardinessZone],
    lastSpringFrost: FrostDate,
    firstAutumnFrost: FrostDate,
    vegetationPeriod: VegetationPeriod,
    coldestNight: Option[ColdestNight], // the yearly low that sets the USDA zone
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

// When a frost happens. `median` is the usual date (1 year in 2). `safeDate` is
// the date to plan around, right 9 years in 10: the last spring frost is over by
// then, or the first autumn frost hasn't come yet. `confirmed` is false until an
// official source backs it up.
case class FrostDate(
    median: MonthDay,
    safeDate: MonthDay,
    confirmed: Boolean,
    source: Option[String],
)

object FrostDate {
  implicit val format: OFormat[FrostDate] = Json.format[FrostDate]
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

// The coldest night of each winter, averaged over a normal period. This is what a USDA hardiness
// zone measures (7a = -17.8 to -15.0 C), so plants' frost tolerance can be compared with it.
case class ColdestNight(
    averageC: Double,
    worstC: Double, // the single coldest winter in the period
    source: Option[String],
)

object ColdestNight {
  implicit val format: OFormat[ColdestNight] = Json.format[ColdestNight]
}

// Which zone map a hardiness zone comes from.
sealed trait HardinessSystem
object HardinessSystem {
    case object Swedish extends HardinessSystem
    case object Usda extends HardinessSystem

    implicit val format: Format[HardinessSystem] = new Format[HardinessSystem] {
        def reads(json: JsValue): JsResult[HardinessSystem] = json match {
            case JsString("swedish") => JsSuccess(Swedish)
            case JsString("usda")    => JsSuccess(Usda)
            case _                   => JsError("Invalid hardiness system")
        }

        def writes(system: HardinessSystem): JsValue = system match {
            case Swedish => JsString("swedish")
            case Usda    => JsString("usda")
        }
    }
}
