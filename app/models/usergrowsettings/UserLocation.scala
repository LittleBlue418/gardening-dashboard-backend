package models
package usergrowsettings

import play.api.libs.json._

// One user's own version of a growing location: what their house, conservatory,
// greenhouse or outside space is like. Equipment lives in UserEquipment.
case class UserLocation(
    location: Location,
    label: String, // "House"
    icon: String, // "🏠"
    description: String, // "Warm (~20°C) but dim."
)

object UserLocation {
  implicit val format: OFormat[UserLocation] = Json.format[UserLocation]
}
