package models

import play.api.libs.json._

// Display colours for one planting action (one row per action), matching the key
// on the dashboard. Kept apart from Plant so a colour change is a single edit.
case class ActionColour(
    action: PlantingAction,
    background: String, // hex, e.g. "#fff3e0"
    border: String, // hex, e.g. "#ff9800"
)

object ActionColour {
  implicit val format: OFormat[ActionColour] = Json.format[ActionColour]
}
