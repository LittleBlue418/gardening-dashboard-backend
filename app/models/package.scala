package object models {
  import org.mongodb.scala.bson.ObjectId
  import play.api.libs.json._
  import java.util.Date

  // Shared ObjectId format
  implicit val objectIdFormat: Format[ObjectId] = new Format[ObjectId] {
    def reads(json: JsValue): JsResult[ObjectId] = json match {
      case JsString(s) => JsSuccess(new ObjectId(s))
      case _           => JsError("Expected ObjectId as string")
    }
    def writes(o: ObjectId): JsValue = JsString(o.toHexString)
  }

  // Shared Date format
  implicit val dateFormat: Format[Date] = new Format[Date] {
    def reads(json: JsValue): JsResult[Date] = json match {
      case JsNumber(n) => JsSuccess(new Date(n.toLong))
      case _           => JsError("Expected Date as timestamp (number)")
    }
    def writes(d: Date): JsValue = JsNumber(d.getTime)
  }
}
