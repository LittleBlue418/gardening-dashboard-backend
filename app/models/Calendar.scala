package models

import java.util.Date
import org.mongodb.scala.bson.ObjectId
import play.api.libs.json._

case class CalendarItem(
    _id: ObjectId = new ObjectId(),
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
    _id: ObjectId = new ObjectId(),
    bed: Bed,
    slots: List[CalendarItem],
)

object CalendarBedRow {
  implicit val format: OFormat[CalendarBedRow] = Json.format[CalendarBedRow]
}

case class CalendarYear(
    _id: ObjectId = new ObjectId(),
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
