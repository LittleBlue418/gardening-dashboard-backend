package models

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

// One user's own version of a growing location: what their house, conservatory,
// greenhouse or outside space is like. One row per user per location.
case class UserLocation(
    _id: ObjectId = new ObjectId(),
    userId: ObjectId, // User._id
    location: Location,
    label: String, // "House"
    icon: String, // "🏠"
    description: String, // "Warm (~20°C) but dim."
    equipment: List[String], // "heat mat", "grow lamp"
)

object UserLocation {
  implicit val format: OFormat[UserLocation] = Json.format[UserLocation]
}
