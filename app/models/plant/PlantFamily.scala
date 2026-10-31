package models
package plant

import play.api.libs.json._

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
