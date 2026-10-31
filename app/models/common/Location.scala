package models
package common

import play.api.libs.json._

// Where an event happens. Each user describes their own versions in UserLocation.
sealed trait Location
object Location {
    case object House extends Location
    case object Conservatory extends Location
    case object Greenhouse extends Location
    case object Outside extends Location

    implicit val format: Format[Location] = new Format[Location] {
        def reads(json: JsValue): JsResult[Location] = json match {
            case JsString("house")           => JsSuccess(House)
            case JsString("conservatory")    => JsSuccess(Conservatory)
            case JsString("greenhouse")      => JsSuccess(Greenhouse)
            case JsString("outside")         => JsSuccess(Outside)
            case _                           => JsError("Invalid location")
        }

        def writes(location: Location): JsValue = location match {
            case House         => JsString("house")
            case Conservatory  => JsString("conservatory")
            case Greenhouse    => JsString("greenhouse")
            case Outside       => JsString("outside")
        }
    }
}
