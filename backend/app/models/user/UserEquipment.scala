package models
package user

import play.api.libs.json._
import models.common._

// A piece of kit the user owns, and the locations they use it in.
case class UserEquipment(
    equipment: EquipmentType,
    label: String, // "Heat mat"
    icon: String, // "🌡️"
    description: String,
    locations: List[Location], // where it's used, e.g. House, Conservatory
)

object UserEquipment {
  implicit val format: OFormat[UserEquipment] = Json.format[UserEquipment]
}
