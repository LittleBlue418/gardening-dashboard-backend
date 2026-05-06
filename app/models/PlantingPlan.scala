package models

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

case class PlantingPlanPlant(
    _id: ObjectId = new ObjectId(),
    taskTitle: String,
    taskDescription: String,
    plantingAction: PlantingAction,
    plant: Plant
)

object PlantingPlanPlant {
  implicit val format: OFormat[PlantingPlanPlant] = Json.format[PlantingPlanPlant]
}


case class PlantingPlanTask(
    _id: ObjectId = new ObjectId(),
    taskTitle: String,
    taskDescription: String,
    icon: String
)

object PlantingPlanTask {
  implicit val format: OFormat[PlantingPlanTask] = Json.format[PlantingPlanTask]
}

case class PlantingPlanWeek(
    _id: ObjectId = new ObjectId(),
    title: String,
    plant: List[PlantingPlanPlant],
    tasks: List[PlantingPlanTask]
)

object PlantingPlanWeek {
  implicit val format: OFormat[PlantingPlanWeek] = Json.format[PlantingPlanWeek]
}

case class PlantingPlanMonth(
    _id: ObjectId = new ObjectId(),
    name: MonthName,
    icon: String,
    notes: List[String],
    weeks: List[PlantingPlanWeek]
)

object PlantingPlanMonth {
  implicit val format: OFormat[PlantingPlanMonth] = Json.format[PlantingPlanMonth]
}