package models
package user

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._
import models.common._

// One month of the garden year for one user: a short summary and the jobs to do.
// One row per (userId, month). Plant sowing/harvest timing lives on Plant.methods (each method's events), not here.
case class GardenMonth(
    _id: ObjectId = new ObjectId(),
    userId: ObjectId, // User._id
    month: MonthName,
    summary: String,
    tasks: List[String], // plain text for now; later a task type
)

object GardenMonth {
  implicit val format: OFormat[GardenMonth] = Json.format[GardenMonth]
}
