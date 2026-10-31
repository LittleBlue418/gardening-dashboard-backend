package models
package common

import play.api.libs.json._

sealed trait MonthName
object MonthName {
  case object January extends MonthName
  case object February extends MonthName
  case object March extends MonthName
  case object April extends MonthName
  case object May extends MonthName
  case object June extends MonthName
  case object July extends MonthName
  case object August extends MonthName
  case object September extends MonthName
  case object October extends MonthName
  case object November extends MonthName
  case object December extends MonthName

  // JSON conversion: converts to/from string
  implicit val format: Format[MonthName] = new Format[MonthName] {
    def reads(json: JsValue): JsResult[MonthName] = json match {
      case JsString("January")   => JsSuccess(January)
      case JsString("February")  => JsSuccess(February)
      case JsString("March")     => JsSuccess(March)
      case JsString("April")     => JsSuccess(April)
      case JsString("May")       => JsSuccess(May)
      case JsString("June")      => JsSuccess(June)
      case JsString("July")      => JsSuccess(July)
      case JsString("August")    => JsSuccess(August)
      case JsString("September") => JsSuccess(September)
      case JsString("October")   => JsSuccess(October)
      case JsString("November")  => JsSuccess(November)
      case JsString("December")  => JsSuccess(December)
      case _                     => JsError("Invalid month name")
    }

    def writes(month: MonthName): JsValue = month match {
      case January   => JsString("January")
      case February  => JsString("February")
      case March     => JsString("March")
      case April     => JsString("April")
      case May       => JsString("May")
      case June      => JsString("June")
      case July      => JsString("July")
      case August    => JsString("August")
      case September => JsString("September")
      case October   => JsString("October")
      case November  => JsString("November")
      case December  => JsString("December")
    }
  }
}
