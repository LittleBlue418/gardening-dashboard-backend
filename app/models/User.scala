package models

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

case class User(
  _id: ObjectId = new ObjectId(),
  googleId: String,
  email: String,
  name: String,
  pictureUrl: Option[String],
  createdAt: Long = System.currentTimeMillis()
)

object User {
  implicit val format: OFormat[User] = Json.format[User]
}