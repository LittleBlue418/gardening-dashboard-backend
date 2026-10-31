package models

import org.scalatestplus.play._
import play.api.libs.json._

import java.nio.file.{Files, Paths}

// Guards the temp DB against model changes and bad data: every plant in plants.json must
// parse, and the book facts copied into it must make sense.
class PlantJsonSpec extends PlaySpec {

  private lazy val plants: List[Plant] =
    Json.parse(Files.readString(Paths.get("temp-db/plants.json"))).validate[List[Plant]] match {
      case JsSuccess(parsed, _) => parsed
      case JsError(errors)      => fail(errors.mkString("\n"))
    }

  private def label(plant: Plant): String = plant.name + plant.variety.fold("")(" - " + _)

  private lazy val withFacts: List[(Plant, GrowingFacts)] =
    plants.flatMap(plant => plant.growing.map(plant -> _))

  "temp-db/plants.json" should {
    "parse into Plant models" in {
      plants must not be empty
    }

    "have one plant per name + variety" in {
      val duplicates = plants.groupBy(label).collect { case (key, group) if group.size > 1 => key }
      duplicates mustBe empty
    }

    "have a Swedish name, scientific name and family on every plant with book facts" in {
      val incomplete = withFacts.collect {
        case (plant, _) if plant.swedishName.isEmpty || plant.scientificName.isEmpty || plant.family.isEmpty => label(plant)
      }
      incomplete mustBe empty
    }

    "have a description on every plant with book facts" in {
      val missing = withFacts.collect { case (plant, _) if plant.description.forall(_.isBlank) => label(plant) }
      missing mustBe empty
    }

    "keep Swedish text in swedishName only" in {
      val swedishLetters = "[åäöÅÄÖ]".r
      val bad = plants.flatMap { plant =>
        (List(plant.name) ++ plant.variety ++ plant.description ++ plant.howToEat ++ plant.notes ++ plant.methods.flatMap(_.events).map(_.note) ++
          plant.recommendedVarieties.flatMap(variety => variety.ripening ++ variety.pollination))
          .filter(text => swedishLetters.findFirstIn(text).isDefined)
          .map(text => label(plant) + ": " + text.take(60))
      }
      bad mustBe empty
    }

    "point every image at a file in image-db (when the folder exists locally)" in {
      val folder = Paths.get("image-db")
      // image-db is gitignored, so a fresh clone has no images; only check when it's there.
      if (Files.isDirectory(folder)) {
        val missing = plants.flatMap(plant => plant.image.filterNot(file => Files.exists(folder.resolve(file))).map(label(plant) + ": " + _))
        missing mustBe empty
      }
    }

    "only credit a photo that exists" in {
      val bad = plants.collect { case plant if plant.imageCredit.isDefined && plant.image.isEmpty => label(plant) }
      bad mustBe empty
    }

    "have at least one growing method per plant, each with events" in {
      val bad = plants.collect { case plant if plant.methods.isEmpty || plant.methods.exists(_.events.isEmpty) => label(plant) }
      bad mustBe empty
    }

    "use each start method once per plant" in {
      val bad = plants.collect { case plant if plant.methods.map(_.start).distinct.size != plant.methods.size => label(plant) }
      bad mustBe empty
    }

    "start seed methods with a sowing and the others with a planting" in {
      val bad = plants.flatMap { plant =>
        plant.methods.collect {
          case method if method.start == StartMethod.Seed && !method.events.exists(_.action == PlantingAction.Sow) =>
            label(plant) + ": seed method without a sowing"
          case method if method.start != StartMethod.Seed && method.events.exists(_.action == PlantingAction.Sow) =>
            label(plant) + ": " + method.start.key + " method with a sowing"
        }
      }
      bad mustBe empty
    }

    "list each recommended variety once per plant, with zones within 1-8" in {
      val bad = plants.flatMap { plant =>
        val names = plant.recommendedVarieties.map(_.name)
        val duplicate = if (names.distinct.size != names.size) List(label(plant) + ": duplicate variety") else Nil
        duplicate ++ plant.recommendedVarieties.collect {
          case variety if variety.swedishZones.exists(z => z.min < 1 || z.max > 8 || z.min > z.max) => label(plant) + ": " + variety.name
        }
      }
      bad mustBe empty
    }

    "use each Swedish name once" in {
      val duplicates = plants.flatMap(_.swedishName).groupBy(identity).collect { case (name, group) if group.size > 1 => name }
      duplicates mustBe empty
    }

    "have ranges with min <= max" in {
      val bad = withFacts.flatMap { case (plant, facts) =>
        List(
          "germinationDays" -> facts.germinationDays,
          "plantSpacingCm"  -> facts.plantSpacingCm,
          "rowSpacingCm"    -> facts.rowSpacingCm,
          "daysToHarvest"   -> facts.daysToHarvest,
          "survivesDownToC" -> facts.survivesDownToC,
          "heightCm"        -> facts.heightCm,
          "swedishZones"    -> facts.swedishZones,
        ).collect { case (field, Some(range)) if range.min > range.max => label(plant) + ": " + field }
      }
      bad mustBe empty
    }

    "keep Swedish zones within 1-8" in {
      val bad = withFacts.collect { case (plant, facts) if facts.swedishZones.exists(z => z.min < 1 || z.max > 8) => label(plant) }
      bad mustBe empty
    }

    "store frost tolerance as zero or below" in {
      val bad = withFacts.collect { case (plant, facts) if facts.survivesDownToC.exists(_.max > 0) => label(plant) }
      bad mustBe empty
    }

    "name exactly one source per fact box: a book page or another source" in {
      val bad = withFacts.collect { case (plant, facts) if facts.bookPage.isDefined == facts.otherSource.isDefined => label(plant) }
      bad mustBe empty
    }

    "point every fact box at a real page of the book (532 pages)" in {
      val bad = withFacts.collect { case (plant, facts) if facts.bookPage.exists(page => page < 1 || page > 532) => label(plant) }
      bad mustBe empty
    }
  }

  "temp-db/climate.json" should {
    "parse into Climate models with a coldest night colder than zero" in {
      Json.parse(Files.readString(Paths.get("temp-db/climate.json"))).validate[List[Climate]] match {
        case JsSuccess(rows, _) =>
          rows must not be empty
          rows.foreach(row => row.coldestNight.foreach(night => night.worstC must be <= night.averageC))
        case JsError(errors) => fail(errors.mkString("\n"))
      }
    }
  }

  "GrowingFacts" should {
    "round-trip the book's fact box for broad bean (p. 245)" in {
      val facts = GrowingFacts(
        germinationDays = Some(IntRange(10, 14)),
        minGerminationTempC = Some(5),
        plantSpacingCm = Some(IntRange(10, 15)),
        rowSpacingCm = Some(IntRange(50, 60)),
        daysToHarvest = Some(IntRange(70, 90)),
        survivesDownToC = Some(IntRange(-8, -7)),
        heightCm = None,
        swedishZones = None,
        goodIntercrop = false,
        sowInRounds = false,
        cutAndComeAgain = false,
        bookPage = Some(245),
        otherSource = None,
      )
      Json.toJson(facts).as[GrowingFacts] mustBe facts
    }

    "reject a fact box with a missing flag" in {
      val json = Json.obj("bookPage" -> 245, "goodIntercrop" -> false, "sowInRounds" -> false)
      json.validate[GrowingFacts].isError mustBe true
    }
  }

  "StartMethod" should {
    "round-trip every method" in {
      StartMethod.all.foreach(method => Json.toJson(method).as[StartMethod] mustBe method)
    }

    "reject an unknown method" in {
      JsString("cuttings").validate[StartMethod].isError mustBe true
    }
  }

  "PlantFamily" should {
    "read and write the Latin name" in {
      Json.toJson[PlantFamily](PlantFamily.Fabaceae) mustBe JsString("Fabaceae")
      JsString("Fabaceae").as[PlantFamily] mustBe PlantFamily.Fabaceae
      JsString("Beans").validate[PlantFamily].isError mustBe true
    }

    "round-trip every family" in {
      PlantFamily.all.foreach(family => Json.toJson(family).as[PlantFamily] mustBe family)
    }

    "list each family once" in {
      PlantFamily.all.map(_.latinName).distinct.size mustBe PlantFamily.all.size
    }
  }
}
