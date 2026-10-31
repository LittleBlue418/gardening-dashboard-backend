package models
package plant

import play.api.libs.json._

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
