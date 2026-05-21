package models

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

case class ToDoItem(
    _id: ObjectId = new ObjectId(),
    userId: String,
    title: String,
    done: Boolean,
    timescale: Timescale,
    priority: Priority
)

object ToDoItem {
  implicit val format: OFormat[ToDoItem] = Json.format[ToDoItem]
}