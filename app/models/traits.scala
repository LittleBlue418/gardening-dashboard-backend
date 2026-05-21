package models

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

sealed trait PlantingAction
object PlantingAction {
    case object StartIndoors extends PlantingAction
    case object ColdSow extends PlantingAction
    case object DirectSow extends PlantingAction
    case object PlantOut extends PlantingAction
    case object Harvest extends PlantingAction

    implicit val format: Format[PlantingAction] = new Format[PlantingAction] {
        def reads(json: JsValue): JsResult[PlantingAction] = json match {
            case JsString("Start indoors") => JsSuccess(StartIndoors)
            case JsString("Cold sow") => JsSuccess(ColdSow)
            case JsString("Direct sow") => JsSuccess(DirectSow)
            case JsString("Plant out") => JsSuccess(PlantOut)
            case JsString("harvest") => JsSuccess(Harvest)
            case _                  => JsError("Invalid planting action")
        }

        def writes(action: PlantingAction): JsValue = action match {
            case StartIndoors => JsString("Start indoors")
            case ColdSow => JsString("Cold sow")
            case DirectSow => JsString("Direct sow")
            case PlantOut => JsString("Plant out")
            case Harvest => JsString("Harvest")
        }
    }
}
