package models
package user

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

case class User(
  _id: ObjectId = new ObjectId(),
  googleId: String,
  email: String,
  name: String,
  pictureUrl: Option[String],
  createdAt: Long = System.currentTimeMillis(),
  savedPlantIds: List[ObjectId] = Nil, // Plant._id of each plant in "My plants"
  selectedPlantIds: List[ObjectId] = Nil, // the saved plants they're growing this year (always a subset of saved); drives the timeline
)

object User {
  implicit val format: OFormat[User] = Json.format[User]
}