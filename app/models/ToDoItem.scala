package models

import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

case class ToDoItem(
    _id: ObjectId = new ObjectId(),
    title: String,
    done: Boolean,
    toDoTimescale: ToDoTimescale,
    priority: ToDoPriority
)

object ToDoItem {
  implicit val format: OFormat[ToDoItem] = Json.format[ToDoItem]
}