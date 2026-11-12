package models

import play.api.libs.json._
import models.common.{Id, MonthName}
import models.plant.{Plant, PlantingAction}

case class PlantingPlanPlant(
    _id: Id = Id.generate(),
    taskTitle: String,
    taskDescription: String,
    plantingAction: PlantingAction,
    plant: Plant
)

object PlantingPlanPlant {
  implicit val format: OFormat[PlantingPlanPlant] = Json.format[PlantingPlanPlant]
}


case class PlantingPlanTask(
    _id: Id = Id.generate(),
    taskTitle: String,
    taskDescription: String,
    icon: String
)

object PlantingPlanTask {
  implicit val format: OFormat[PlantingPlanTask] = Json.format[PlantingPlanTask]
}

case class PlantingPlanWeek(
    _id: Id = Id.generate(),
    title: String,
    plant: List[PlantingPlanPlant],
    tasks: List[PlantingPlanTask]
)

object PlantingPlanWeek {
  implicit val format: OFormat[PlantingPlanWeek] = Json.format[PlantingPlanWeek]
}

case class PlantingPlanMonth(
    _id: Id = Id.generate(),
    name: MonthName,
    icon: String,
    notes: List[String],
    weeks: List[PlantingPlanWeek]
)

object PlantingPlanMonth {
  implicit val format: OFormat[PlantingPlanMonth] = Json.format[PlantingPlanMonth]
}