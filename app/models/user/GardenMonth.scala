package models
package user

import play.api.libs.json._
import models.common._

// One month of the garden year for one user: a short summary and their own extra jobs.
// One row per (userId, month). Plant sowing/harvest timing lives on Plant.methods (each method's events), and
// recurring garden jobs in the shared GardenJob catalogue; neither is stored here.
case class GardenMonth(
    _id: Id = Id.generate(),
    userId: Id, // User._id
    month: MonthName,
    summary: String,
    tasks: List[String], // the user's own extra jobs for this month (plain text); shared jobs are GardenJob
)

object GardenMonth {
  implicit val format: OFormat[GardenMonth] = Json.format[GardenMonth]
}
