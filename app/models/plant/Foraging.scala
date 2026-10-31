package models
package plant

import play.api.libs.json._
import models.common._

// How to find and pick a plant in the wild. Any Plant can have it: wild-only plants (category Forage)
// and garden plants that also grow wild (elder, wild garlic…), so nothing is stored twice. Taken from
// Niki Sjölund, "Vildplockat" (2017); `bookPage` is a page in that book, not the garden book.
case class Foraging(
    kind: WildKind,
    harvest: List[HarvestWindow], // what you pick and when; parts can differ (birch: sap in spring, leaves all season)
    picking: String, // how and where to pick, in English
    habitat: String, // where it grows, e.g. "Rock outcrops, gravel, shores and forest edges."
    distribution: String, // where in Sweden, e.g. "Coast up to central Sweden."
    identification: String, // how to recognise it
    lookAlikes: List[LookAlike], // empty when the book names none
    safety: Option[String], // a warning from the book (poisonous parts, protected species…)
    bookPage: Int, // page in "Vildplockat"
)

object Foraging {
  implicit val format: OFormat[Foraging] = Json.format[Foraging]
}

// One part of the plant and the months it's picked.
case class HarvestWindow(
    part: HarvestPart,
    months: List[MonthName],
    note: Option[String], // e.g. "Before the leaves open."
)

object HarvestWindow {
  implicit val format: OFormat[HarvestWindow] = Json.format[HarvestWindow]
}

// Something it can be mistaken for. `poisonous` drives the red warning on cards.
case class LookAlike(
    name: String, // English name (Swedish name in brackets only if there's no English one)
    poisonous: Boolean,
    howToTell: String, // how to tell them apart, in English
)

object LookAlike {
  implicit val format: OFormat[LookAlike] = Json.format[LookAlike]
}

// The book's entry type.
sealed abstract class WildKind(val key: String)
object WildKind {
    case object Herb extends WildKind("herb")
    case object Mushroom extends WildKind("mushroom")
    case object Tree extends WildKind("tree")
    case object Bush extends WildKind("bush")
    case object Berry extends WildKind("berry")
    case object Root extends WildKind("root")
    case object Lichen extends WildKind("lichen")

    val all: List[WildKind] = List(Herb, Mushroom, Tree, Bush, Berry, Root, Lichen)

    implicit val format: Format[WildKind] = new Format[WildKind] {
        def reads(json: JsValue): JsResult[WildKind] = json match {
            case JsString(key) => all.find(_.key == key).map(JsSuccess(_)).getOrElse(JsError("Invalid wild kind: " + key))
            case _             => JsError("Invalid wild kind")
        }
        def writes(kind: WildKind): JsValue = JsString(kind.key)
    }
}

// The part of a plant that's picked. Drives the colours on the forager timeline.
sealed abstract class HarvestPart(val key: String)
object HarvestPart {
    case object Leaves extends HarvestPart("leaves")
    case object Shoots extends HarvestPart("shoots")
    case object Flowers extends HarvestPart("flowers")
    case object Buds extends HarvestPart("buds")
    case object Berries extends HarvestPart("berries")
    case object Fruit extends HarvestPart("fruit")
    case object Nuts extends HarvestPart("nuts")
    case object Seeds extends HarvestPart("seeds")
    case object Roots extends HarvestPart("roots")
    case object Sap extends HarvestPart("sap")
    case object Needles extends HarvestPart("needles")
    case object Bark extends HarvestPart("bark")
    case object WholeMushroom extends HarvestPart("mushroom")
    case object Whole extends HarvestPart("whole") // the whole plant above ground

    val all: List[HarvestPart] = List(Leaves, Shoots, Flowers, Buds, Berries, Fruit, Nuts, Seeds, Roots, Sap, Needles, Bark, WholeMushroom, Whole)

    implicit val format: Format[HarvestPart] = new Format[HarvestPart] {
        def reads(json: JsValue): JsResult[HarvestPart] = json match {
            case JsString(key) => all.find(_.key == key).map(JsSuccess(_)).getOrElse(JsError("Invalid harvest part: " + key))
            case _             => JsError("Invalid harvest part")
        }
        def writes(part: HarvestPart): JsValue = JsString(part.key)
    }
}
