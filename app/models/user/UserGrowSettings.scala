package models
package user

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._
import models.common._

// One user's grow settings: their climate, their own locations and the equipment they own.
// One document per user, so userId is stored once, not on every row.
case class UserGrowSettings(
    _id: ObjectId = new ObjectId(),
    userId: ObjectId, // User._id
    climateId: ObjectId, // Climate._id: neighbours share one climate row; its location is the garden's location
    locations: List[UserLocation],
    equipment: List[UserEquipment],
)

object UserGrowSettings {
  implicit val format: OFormat[UserGrowSettings] = Json.format[UserGrowSettings]
}
