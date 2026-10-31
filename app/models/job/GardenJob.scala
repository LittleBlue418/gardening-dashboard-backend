package models
package job

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._
import models.common._

// A recurring garden job and when to do it, e.g. "Dig heavy clay in autumn". One shared catalogue (like
// plants), not per user: each user sees the jobs that apply to them. A job with no plantIds applies to
// everyone; otherwise only to users growing one of those plants. A job with a location only shows for
// users who have that location (e.g. greenhouse jobs).
case class GardenJob(
    _id: ObjectId = new ObjectId(),
    task: String, // one short English sentence, e.g. "Dig heavy clay soil in autumn."
    months: List[MonthName],
    category: JobCategory,
    plantIds: List[ObjectId], // Plant._id; empty = a general job
    location: Option[Location], // where it happens; None = anywhere (planning, tools)
    bookPage: Option[Int], // set when the job comes from the book...
    otherSource: Option[String], // ...or this, when it doesn't
)

object GardenJob {
  implicit val format: OFormat[GardenJob] = Json.format[GardenJob]
}

// What kind of job it is, for grouping and icons.
sealed abstract class JobCategory(val key: String)
object JobCategory {
    case object Planning extends JobCategory("planning")
    case object Soil extends JobCategory("soil")
    case object Compost extends JobCategory("compost")
    case object Sowing extends JobCategory("sowing") // green manure, catch crops; not individual vegetables
    case object Care extends JobCategory("care") // weeding, watering, feeding, mulching, thinning, staking
    case object Protection extends JobCategory("protection") // fleece, nets, winter cover, pest barriers
    case object Pruning extends JobCategory("pruning")
    case object HarvestStorage extends JobCategory("harvest-storage")
    case object SeedSaving extends JobCategory("seed-saving")
    case object Tools extends JobCategory("tools")

    val all: List[JobCategory] = List(Planning, Soil, Compost, Sowing, Care, Protection, Pruning, HarvestStorage, SeedSaving, Tools)

    implicit val format: Format[JobCategory] = new Format[JobCategory] {
        def reads(json: JsValue): JsResult[JobCategory] = json match {
            case JsString(key) => all.find(_.key == key).map(JsSuccess(_)).getOrElse(JsError("Invalid job category: " + key))
            case _             => JsError("Invalid job category")
        }

        def writes(category: JobCategory): JsValue = JsString(category.key)
    }
}
