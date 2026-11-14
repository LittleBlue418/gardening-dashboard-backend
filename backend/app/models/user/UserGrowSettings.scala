package models
package user

import play.api.libs.json._
import models.common._

// One user's grow settings: their climate, their own locations and the equipment they own.
// One document per user, so userId is stored once, not on every row.
case class UserGrowSettings(
    _id: Id = Id.generate(),
    userId: Id, // User._id
    climateId: Id, // Climate._id: neighbours share one climate row; its location is the garden's location
    locations: List[UserLocation],
    equipment: List[UserEquipment],
)

object UserGrowSettings {
  implicit val format: OFormat[UserGrowSettings] = Json.format[UserGrowSettings]
}
