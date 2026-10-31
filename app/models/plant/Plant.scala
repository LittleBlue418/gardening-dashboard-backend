package models
package plant

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._
import models.common._

// A plant and its year. Each growing method (seed, sets, ...) has its own events: every sowing, planting and harvest is one event.
case class Plant(
    _id: ObjectId = new ObjectId(),
    name: String,
    swedishName: Option[String], // e.g. "Morot". None until filled in from the book
    scientificName: Option[String], // e.g. "Vicia faba"
    variety: Option[String],
    family: Option[PlantFamily], // for crop rotation
    lifecycle: Lifecycle, // as grown: Annual | Perennial
    category: PlantCategory, // what it's grown for: Vegetable | Herb | Fruit | Ornamental
    form: GrowthForm, // its shape: Herbaceous | Bush | Tree
    icon: List[String],
    notes: List[String], // general notes, not tied to a month
    description: Option[String], // what it is, in a sentence or two (summarised from the book)
    howToEat: Option[String], // how it's cooked or eaten; None when the book doesn't say
    image: Option[String], // file name in image-db/ (gitignored); None when there's no photo
    imageCredit: Option[ImageCredit], // for free-licence photos (e.g. Wikimedia Commons); None for Holly's own book photos
    growing: Option[GrowingFacts], // the book's fact box; None until filled in
    foraging: Option[Foraging], // how to find and pick it in the wild; None if it isn't foraged
    methods: List[GrowingMethod], // one per way of starting it (seed, sets, ...); empty only for wild-only (Forage) plants
    recommendedVarieties: List[RecommendedVariety], // named varieties worth buying, with their zones; may be empty
)

object Plant {
  implicit val format: OFormat[Plant] = Json.format[Plant]
}

// A named variety worth growing, e.g. apple 'Silva'. The name is a proper noun, so it stays exactly
// as sold (it may be Swedish); the other text is English.
case class RecommendedVariety(
    name: String,
    swedishZones: Option[IntRange], // Swedish growing zones it's sold for, e.g. 1-6
    ripening: Option[String], // e.g. "Late October; stores until Christmas"
    pollination: Option[String], // e.g. "Self-fertile" or "Pollinated by Discovery, Filippa, Mio"
    source: String,
)

object RecommendedVariety {
  implicit val format: OFormat[RecommendedVariety] = Json.format[RecommendedVariety]
}

// Who took a free-licence photo and under which licence. CC BY / BY-SA require showing this.
case class ImageCredit(
    author: String,
    license: String, // e.g. "CC BY-SA 3.0", "Public domain"
    licenseUrl: Option[String], // None for public domain
    sourceUrl: String, // the photo's page, e.g. on Wikimedia Commons
)

object ImageCredit {
  implicit val format: OFormat[ImageCredit] = Json.format[ImageCredit]
}

// One way of growing a plant: what it starts from, how hard that is, and that route's year of events.
case class GrowingMethod(
    start: StartMethod,
    difficulty: GrowingDifficulty, // Easy | Regular | Effort | Challenging | VeryChallenging
    events: List[PlantEvent],
)

object GrowingMethod {
  implicit val format: OFormat[GrowingMethod] = Json.format[GrowingMethod]
}

// What happens to a plant, in which months, where, and the advice for those months.
case class PlantEvent(
    action: PlantingAction,
    months: List[MonthName],
    locations: List[Location], // in order: ["house", "conservatory"] = germinate in the house, then move
    equipment: List[EquipmentType], // kit this event uses, e.g. HeatMat, GrowLamp
    note: String,
)

object PlantEvent {
  implicit val format: OFormat[PlantEvent] = Json.format[PlantEvent]
}
