package models

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

case class Bed(
    _id: ObjectId = new ObjectId(),
    name: String,
    size: Int,
    location: String, // maybe of type location
    facing: String, // maybe define compass directions?
)

object Bed {
  implicit val format: OFormat[Bed] = Json.format[Bed]
}