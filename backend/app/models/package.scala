package object models {
  import play.api.libs.json._
  import java.util.Date

  // Shared Date format
  implicit val dateFormat: Format[Date] = new Format[Date] {
    def reads(json: JsValue): JsResult[Date] = json match {
      case JsNumber(n) => JsSuccess(new Date(n.toLong))
      case _           => JsError("Expected Date as timestamp (number)")
    }
    def writes(d: Date): JsValue = JsNumber(d.getTime)
  }
}
