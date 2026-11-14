package models

import play.api.libs.json._
import models.common.Id

case class PlantingRecordYear(
    _id: Id = Id.generate(),
    year: Int,
    notes: List[String],
    months: List[PlantingPlanMonth]
)

object PlantingRecordYear {
  implicit val format: OFormat[PlantingRecordYear] = Json.format[PlantingRecordYear]
}