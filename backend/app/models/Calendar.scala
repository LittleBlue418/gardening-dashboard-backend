package models

import java.util.Date
import play.api.libs.json._
import models.common.Id
import models.plant.Plant

case class CalendarItem(
    _id: Id = Id.generate(),
    plant: Plant,
    plannedStart: Date,
    plannedTransplant: Date,
    actualStart: Date,
    actualTransplant: Date,
)

object CalendarItem {
  implicit val format: OFormat[CalendarItem] = Json.format[CalendarItem]
}

case class CalendarBedRow(
    _id: Id = Id.generate(),
    bed: Bed,
    slots: List[CalendarItem],
)

object CalendarBedRow {
  implicit val format: OFormat[CalendarBedRow] = Json.format[CalendarBedRow]
}

case class CalendarYear(
    _id: Id = Id.generate(),
    year: Int,
    seedlingSlots: List[CalendarItem],
    frontGarden: List[CalendarBedRow],
    sideGarden: List[CalendarBedRow],
    backGarden: List[CalendarBedRow],
    greenHouse: List[CalendarBedRow],
    wildArea: List[CalendarBedRow],
)

object CalendarYear {
  implicit val format: OFormat[CalendarYear] = Json.format[CalendarYear]
}
