package models
package usergrowsettings

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

// One user's grow settings: their own locations and the equipment they own.
// One document per user, so userId is stored once, not on every row.
case class UserGrowSettings(
    _id: ObjectId = new ObjectId(),
    userId: ObjectId, // User._id
    locations: List[UserLocation],
    equipment: List[UserEquipment],
)

object UserGrowSettings {
  implicit val format: OFormat[UserGrowSettings] = Json.format[UserGrowSettings]
}
