package models
package common

import play.api.libs.json._

// Kinds of equipment. A new kind is a new case here, not a new class.
sealed trait EquipmentType
object EquipmentType {
    case object HeatMat extends EquipmentType
    case object GrowLamp extends EquipmentType

    implicit val format: Format[EquipmentType] = new Format[EquipmentType] {
        def reads(json: JsValue): JsResult[EquipmentType] = json match {
            case JsString("heat-mat")  => JsSuccess(HeatMat)
            case JsString("grow-lamp") => JsSuccess(GrowLamp)
            case _                     => JsError("Invalid equipment type")
        }

        def writes(equipment: EquipmentType): JsValue = equipment match {
            case HeatMat  => JsString("heat-mat")
            case GrowLamp => JsString("grow-lamp")
        }
    }
}
