package models
package user

import play.api.libs.json._
import models.common.Id

case class User(
  _id: Id = Id.generate(),
  email: String,
  name: String,
  pictureUrl: Option[String],
  createdAt: Long = System.currentTimeMillis(),
  savedPlantIds: List[Id] = Nil, // Plant._id of each plant in "My plants"
  selectedPlantIds: List[Id] = Nil, // the saved plants they're growing this year (always a subset of saved); drives the timeline
)

object User {
  implicit val format: OFormat[User] = Json.format[User]
}