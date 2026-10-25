package models

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

case class Plant(
    _id: ObjectId = new ObjectId(),
    name: String,
    variety: String,
    notes: List[String],
    icon: List[String],
    startIndoors: List[MonthName],
    sowOutdoors: List[MonthName],
    weeksTillHarvest: Int,
    greenHouse: Boolean,
    haveSeeds: Boolean,
)

object Plant {
  implicit val format: OFormat[Plant] = Json.format[Plant]
}
