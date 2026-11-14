package models

import play.api.libs.json._
import models.common.Id

case class Bed(
    _id: Id = Id.generate(),
    name: String,
)

object Bed {
  implicit val format: OFormat[Bed] = Json.format[Bed]
}