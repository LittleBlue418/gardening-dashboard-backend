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

// WHAT happens to a plant. WHERE it happens is the event's `locations`, never part of the action,
// so "start indoors" is Sow in ["house"] and "sow outdoors" is Sow in ["outside"].
sealed trait PlantingAction
object PlantingAction {
    case object Sow extends PlantingAction // seed, wherever it's sown
    case object Transplant extends PlantingAction // seedling into its final spot
    case object PlantInGround extends PlantingAction // bulb, clove, tuber, crown, bush
    case object Harvest extends PlantingAction

    implicit val format: Format[PlantingAction] = new Format[PlantingAction] {
        def reads(json: JsValue): JsResult[PlantingAction] = json match {
            case JsString("Sow")             => JsSuccess(Sow)
            case JsString("Transplant")      => JsSuccess(Transplant)
            case JsString("Plant in ground") => JsSuccess(PlantInGround)
            case JsString("Harvest")         => JsSuccess(Harvest)
            case _                           => JsError("Invalid planting action")
        }

        def writes(action: PlantingAction): JsValue = action match {
            case Sow           => JsString("Sow")
            case Transplant    => JsString("Transplant")
            case PlantInGround => JsString("Plant in ground")
            case Harvest       => JsString("Harvest")
        }
    }
}

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

// Which zone map a hardiness zone comes from.
sealed trait HardinessSystem
object HardinessSystem {
    case object Swedish extends HardinessSystem
    case object Usda extends HardinessSystem

    implicit val format: Format[HardinessSystem] = new Format[HardinessSystem] {
        def reads(json: JsValue): JsResult[HardinessSystem] = json match {
            case JsString("swedish") => JsSuccess(Swedish)
            case JsString("usda")    => JsSuccess(Usda)
            case _                   => JsError("Invalid hardiness system")
        }

        def writes(system: HardinessSystem): JsValue = system match {
            case Swedish => JsString("swedish")
            case Usda    => JsString("usda")
        }
    }
}

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

// How long a plant lives AS GROWN, not botanically: carrots and garlic are Annual here
// because they're harvested in their first year.
sealed trait Lifecycle
object Lifecycle {
    case object Annual extends Lifecycle // sown or planted, harvested and cleared within the year
    case object Perennial extends Lifecycle // stays in the garden year after year

    implicit val format: Format[Lifecycle] = new Format[Lifecycle] {
        def reads(json: JsValue): JsResult[Lifecycle] = json match {
            case JsString("annual")    => JsSuccess(Annual)
            case JsString("perennial") => JsSuccess(Perennial)
            case _                     => JsError("Invalid lifecycle")
        }

        def writes(lifecycle: Lifecycle): JsValue = lifecycle match {
            case Annual    => JsString("annual")
            case Perennial => JsString("perennial")
        }
    }
}

// What the plant is grown for. Independent of Lifecycle and GrowthForm, so a perennial
// herb or a perennial vegetable (rhubarb) needs no special case.
sealed trait PlantCategory
object PlantCategory {
    case object Vegetable extends PlantCategory
    case object Herb extends PlantCategory
    case object Fruit extends PlantCategory
    case object Ornamental extends PlantCategory // grown for looks, shelter or wood, not to eat

    implicit val format: Format[PlantCategory] = new Format[PlantCategory] {
        def reads(json: JsValue): JsResult[PlantCategory] = json match {
            case JsString("vegetable")  => JsSuccess(Vegetable)
            case JsString("herb")       => JsSuccess(Herb)
            case JsString("fruit")      => JsSuccess(Fruit)
            case JsString("ornamental") => JsSuccess(Ornamental)
            case _                      => JsError("Invalid plant category")
        }

        def writes(category: PlantCategory): JsValue = category match {
            case Vegetable  => JsString("vegetable")
            case Herb       => JsString("herb")
            case Fruit      => JsString("fruit")
            case Ornamental => JsString("ornamental")
        }
    }
}

// The plant's shape. "Fruit tree" = Fruit + Tree, "fruit bush" = Fruit + Bush.
sealed trait GrowthForm
object GrowthForm {
    case object Herbaceous extends GrowthForm // soft-stemmed: vegetables, herbs, strawberries, rhubarb
    case object Bush extends GrowthForm // woody, several stems: currants, raspberry canes
    case object Tree extends GrowthForm // woody, one trunk: apple, plum

    implicit val format: Format[GrowthForm] = new Format[GrowthForm] {
        def reads(json: JsValue): JsResult[GrowthForm] = json match {
            case JsString("herbaceous") => JsSuccess(Herbaceous)
            case JsString("bush")       => JsSuccess(Bush)
            case JsString("tree")       => JsSuccess(Tree)
            case _                      => JsError("Invalid growth form")
        }

        def writes(form: GrowthForm): JsValue = form match {
            case Herbaceous => JsString("herbaceous")
            case Bush       => JsString("bush")
            case Tree       => JsString("tree")
        }
    }
}

// Botanical family. Drives crop rotation: plants in the same family share pests and
// diseases, so they shouldn't follow each other in a bed. JSON uses the Latin name,
// which never changes; `swedishName` is the name the book uses (it sometimes uses two).
sealed abstract class PlantFamily(val latinName: String, val swedishName: String)
object PlantFamily {
    case object Aizoaceae extends PlantFamily("Aizoaceae", "isörtsväxter")
    case object Amaranthaceae extends PlantFamily("Amaranthaceae", "amarantväxter") // book also: mållväxter
    case object Amaryllidaceae extends PlantFamily("Amaryllidaceae", "amaryllisväxter") // book: backlöksväxter
    case object Apiaceae extends PlantFamily("Apiaceae", "flockblommiga")
    case object Asparagaceae extends PlantFamily("Asparagaceae", "sparrisväxter")
    case object Asteraceae extends PlantFamily("Asteraceae", "korgblommiga")
    case object Brassicaceae extends PlantFamily("Brassicaceae", "korsblommiga") // book also: kålväxter
    case object Campanulaceae extends PlantFamily("Campanulaceae", "klockväxter")
    case object Caprifoliaceae extends PlantFamily("Caprifoliaceae", "kaprifolväxter") // book also: vänderotsväxter
    case object Convolvulaceae extends PlantFamily("Convolvulaceae", "vindeväxter")
    case object Cucurbitaceae extends PlantFamily("Cucurbitaceae", "gurkväxter")
    case object Elaeagnaceae extends PlantFamily("Elaeagnaceae", "havtornsväxter")
    case object Ericaceae extends PlantFamily("Ericaceae", "ljungväxter")
    case object Fabaceae extends PlantFamily("Fabaceae", "ärtväxter")
    case object Grossulariaceae extends PlantFamily("Grossulariaceae", "ripsväxter")
    case object Lamiaceae extends PlantFamily("Lamiaceae", "kransblommiga")
    case object Malvaceae extends PlantFamily("Malvaceae", "malvaväxter")
    case object Poaceae extends PlantFamily("Poaceae", "gräs")
    case object Polygonaceae extends PlantFamily("Polygonaceae", "slideväxter")
    case object Portulacaceae extends PlantFamily("Portulacaceae", "portlakväxter")
    case object Rosaceae extends PlantFamily("Rosaceae", "rosväxter")
    case object Rutaceae extends PlantFamily("Rutaceae", "vinruteväxter")
    case object Schisandraceae extends PlantFamily("Schisandraceae", "schisandraväxter")
    case object Solanaceae extends PlantFamily("Solanaceae", "potatisväxter")
    case object Verbenaceae extends PlantFamily("Verbenaceae", "verbenaväxter")

    val all: List[PlantFamily] = List(
        Aizoaceae, Amaranthaceae, Amaryllidaceae, Apiaceae, Asparagaceae, Asteraceae, Brassicaceae,
        Campanulaceae, Caprifoliaceae, Convolvulaceae, Cucurbitaceae, Elaeagnaceae, Ericaceae, Fabaceae,
        Grossulariaceae, Lamiaceae, Malvaceae, Poaceae, Polygonaceae, Portulacaceae, Rosaceae, Rutaceae,
        Schisandraceae, Solanaceae, Verbenaceae,
    )

    // Looked up from `all` rather than one case per family, so adding a family is one line.
    implicit val format: Format[PlantFamily] = new Format[PlantFamily] {
        def reads(json: JsValue): JsResult[PlantFamily] = json match {
            case JsString(name) =>
                all.find(_.latinName == name).map(JsSuccess(_)).getOrElse(JsError("Invalid plant family: " + name))
            case _ => JsError("Invalid plant family")
        }

        def writes(family: PlantFamily): JsValue = JsString(family.latinName)
    }
}

// How much work and skill a plant takes in a Stockholm garden.
sealed trait GrowingDifficulty
object GrowingDifficulty {
    case object Easy extends GrowingDifficulty // hardy and forgiving: chard, potatoes, perennial onions
    case object Regular extends GrowingDifficulty // sow outdoors; only the soil temperature matters
    case object Effort extends GrowingDifficulty // start indoors, harden off, plant out at the right time
    case object Challenging extends GrowingDifficulty // exact temperatures, special soil or extra steps (blanching, hand-pollinating)
    case object VeryChallenging extends GrowingDifficulty // needs specialist set-ups, e.g. running water

    implicit val format: Format[GrowingDifficulty] = new Format[GrowingDifficulty] {
        def reads(json: JsValue): JsResult[GrowingDifficulty] = json match {
            case JsString("easy")             => JsSuccess(Easy)
            case JsString("regular")          => JsSuccess(Regular)
            case JsString("effort")           => JsSuccess(Effort)
            case JsString("challenging")      => JsSuccess(Challenging)
            case JsString("very-challenging") => JsSuccess(VeryChallenging)
            case _                            => JsError("Invalid growing difficulty")
        }

        def writes(difficulty: GrowingDifficulty): JsValue = difficulty match {
            case Easy            => JsString("easy")
            case Regular         => JsString("regular")
            case Effort          => JsString("effort")
            case Challenging     => JsString("challenging")
            case VeryChallenging => JsString("very-challenging")
        }
    }
}

// What a plant is started from. A plant can have several (onion: seed or sets), each with its own
// difficulty and timing.
sealed abstract class StartMethod(val key: String)
object StartMethod {
    case object Seed extends StartMethod("seed")
    case object Sets extends StartMethod("sets") // small onion-family bulbs, bulbils
    case object Cloves extends StartMethod("cloves")
    case object Tubers extends StartMethod("tubers")
    case object Crowns extends StartMethod("crowns") // rhubarb, asparagus
    case object Plants extends StartMethod("plants") // divisions, root cuttings, bought plants

    val all: List[StartMethod] = List(Seed, Sets, Cloves, Tubers, Crowns, Plants)

    implicit val format: Format[StartMethod] = new Format[StartMethod] {
        def reads(json: JsValue): JsResult[StartMethod] = json match {
            case JsString(key) => all.find(_.key == key).map(JsSuccess(_)).getOrElse(JsError("Invalid start method: " + key))
            case _             => JsError("Invalid start method")
        }

        def writes(method: StartMethod): JsValue = JsString(method.key)
    }
}
