package models

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

case class PlantingRecordYear(
    _id: ObjectId = new ObjectId(),
    year: Int,
    notes: List[String],
    months: List[PlantingPlanMonth]
)

object PlantingRecordYear {
  implicit val format: OFormat[PlantingRecordYear] = Json.format[PlantingRecordYear]
}