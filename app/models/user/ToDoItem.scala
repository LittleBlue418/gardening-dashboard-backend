package models
package user

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

sealed trait Timescale
object Timescale {
  case object Short extends Timescale
  case object Medium extends Timescale
  case object Long extends Timescale

  implicit val format: Format[Timescale] = new Format[Timescale] {
    def reads(json: JsValue): JsResult[Timescale] = json match {
      case JsString("short")  => JsSuccess(Short)
      case JsString("medium") => JsSuccess(Medium)
      case JsString("long")   => JsSuccess(Long)
      case _                  => JsError("Invalid timescale")
    }

    def writes(timescale: Timescale): JsValue = timescale match {
      case Short => JsString("short")
      case Medium => JsString("medium")
      case Long  => JsString("long")
    }
  }
}

sealed trait Priority
object Priority {
    case object P1 extends Priority
    case object P2 extends Priority
    case object P3 extends Priority

    implicit val format: Format[Priority] = new Format[Priority] {
        def reads(json: JsValue): JsResult[Priority] = json match {
            case JsNumber(n) if n == 1 => JsSuccess(P1)
            case JsNumber(n) if n == 2 => JsSuccess(P2)
            case JsNumber(n) if n == 3 => JsSuccess(P3)
            case _ => JsError("Invalid priority level, must be 1, 2, or 3")
        }

        def writes(level: Priority): JsValue = level match {
            case P1 => JsNumber(1)
            case P2 => JsNumber(2)
            case P3 => JsNumber(3)
        }
    }
}
