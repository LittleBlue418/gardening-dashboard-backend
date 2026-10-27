package models

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

// A plant and its year: every sowing, planting and harvest is one event.
case class Plant(
    _id: ObjectId = new ObjectId(),
    name: String,
    variety: Option[String],
    icon: List[String],
    notes: List[String], // general notes, not tied to a month
    weeksTillHarvest: Option[Int], // None for perennials (rhubarb, fruit)
    events: List[PlantEvent],
)

object Plant {
  implicit val format: OFormat[Plant] = Json.format[Plant]
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
