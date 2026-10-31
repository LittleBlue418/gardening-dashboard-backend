package models
package common

import play.api.libs.json._

// An inclusive range, e.g. 10–15 cm. A single value is min == max.
case class IntRange(
    min: Int,
    max: Int,
)

object IntRange {
  implicit val format: OFormat[IntRange] = Json.format[IntRange]
}
