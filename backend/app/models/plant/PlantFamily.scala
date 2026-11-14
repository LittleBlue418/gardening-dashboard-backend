package models
package plant

import play.api.libs.json._

// Botanical family (fungi and lichens included, for foraged plants). Drives crop rotation: plants in the same family share pests and
// diseases, so they shouldn't follow each other in a bed. JSON uses the Latin name,
// which never changes; `swedishName` is the name the book uses (it sometimes uses two).
sealed abstract class PlantFamily(val latinName: String, val swedishName: String)
object PlantFamily {
    case object Agaricaceae extends PlantFamily("Agaricaceae", "champinjonväxter")
    case object Aizoaceae extends PlantFamily("Aizoaceae", "isörtsväxter")
    case object Albatrellaceae extends PlantFamily("Albatrellaceae", "fårtickor")
    case object Amaranthaceae extends PlantFamily("Amaranthaceae", "amarantväxter") // book also: mållväxter
    case object Amaryllidaceae extends PlantFamily("Amaryllidaceae", "amaryllisväxter") // book: backlöksväxter
    case object Apiaceae extends PlantFamily("Apiaceae", "flockblommiga")
    case object Asparagaceae extends PlantFamily("Asparagaceae", "sparrisväxter")
    case object Asteraceae extends PlantFamily("Asteraceae", "korgblommiga")
    case object Betulaceae extends PlantFamily("Betulaceae", "björkväxter")
    case object Boletaceae extends PlantFamily("Boletaceae", "soppar")
    case object Brassicaceae extends PlantFamily("Brassicaceae", "korsblommiga") // book also: kålväxter
    case object Campanulaceae extends PlantFamily("Campanulaceae", "klockväxter")
    case object Cantharellaceae extends PlantFamily("Cantharellaceae", "kantareller")
    case object Caprifoliaceae extends PlantFamily("Caprifoliaceae", "kaprifolväxter") // book also: vänderotsväxter
    case object Caryophyllaceae extends PlantFamily("Caryophyllaceae", "nejlikväxter")
    case object Cladoniaceae extends PlantFamily("Cladoniaceae", "bägarlavar")
    case object Convolvulaceae extends PlantFamily("Convolvulaceae", "vindeväxter")
    case object Crassulaceae extends PlantFamily("Crassulaceae", "fetbladsväxter")
    case object Cucurbitaceae extends PlantFamily("Cucurbitaceae", "gurkväxter")
    case object Cupressaceae extends PlantFamily("Cupressaceae", "cypressväxter")
    case object Elaeagnaceae extends PlantFamily("Elaeagnaceae", "havtornsväxter")
    case object Ericaceae extends PlantFamily("Ericaceae", "ljungväxter")
    case object Fabaceae extends PlantFamily("Fabaceae", "ärtväxter")
    case object Fagaceae extends PlantFamily("Fagaceae", "bokväxter")
    case object Fomitopsidaceae extends PlantFamily("Fomitopsidaceae", "tickor")
    case object Grossulariaceae extends PlantFamily("Grossulariaceae", "ripsväxter")
    case object Hydnaceae extends PlantFamily("Hydnaceae", "taggsvampar")
    case object Lamiaceae extends PlantFamily("Lamiaceae", "kransblommiga")
    case object Liliaceae extends PlantFamily("Liliaceae", "liljeväxter")
    case object Malvaceae extends PlantFamily("Malvaceae", "malvaväxter")
    case object Morchellaceae extends PlantFamily("Morchellaceae", "murklor")
    case object Myricaceae extends PlantFamily("Myricaceae", "porsväxter")
    case object Oleaceae extends PlantFamily("Oleaceae", "syrenväxter")
    case object Onagraceae extends PlantFamily("Onagraceae", "dunörtsväxter")
    case object Oxalidaceae extends PlantFamily("Oxalidaceae", "harsyreväxter")
    case object Pinaceae extends PlantFamily("Pinaceae", "tallväxter")
    case object Plantaginaceae extends PlantFamily("Plantaginaceae", "grobladsväxter")
    case object Pleurotaceae extends PlantFamily("Pleurotaceae", "musslingar")
    case object Poaceae extends PlantFamily("Poaceae", "gräs")
    case object Polygonaceae extends PlantFamily("Polygonaceae", "slideväxter")
    case object Polypodiaceae extends PlantFamily("Polypodiaceae", "stensöteväxter")
    case object Portulacaceae extends PlantFamily("Portulacaceae", "portlakväxter")
    case object Primulaceae extends PlantFamily("Primulaceae", "viveväxter")
    case object Rosaceae extends PlantFamily("Rosaceae", "rosväxter")
    case object Rubiaceae extends PlantFamily("Rubiaceae", "måreväxter")
    case object Russulaceae extends PlantFamily("Russulaceae", "kremlor och riskor")
    case object Rutaceae extends PlantFamily("Rutaceae", "vinruteväxter")
    case object Schisandraceae extends PlantFamily("Schisandraceae", "schisandraväxter")
    case object Solanaceae extends PlantFamily("Solanaceae", "potatisväxter")
    case object Sparassidaceae extends PlantFamily("Sparassidaceae", "blomkålssvampar")
    case object Ulmaceae extends PlantFamily("Ulmaceae", "almväxter")
    case object Urticaceae extends PlantFamily("Urticaceae", "nässelväxter")
    case object Verbenaceae extends PlantFamily("Verbenaceae", "verbenaväxter")
    case object Violaceae extends PlantFamily("Violaceae", "violväxter")

    val all: List[PlantFamily] = List(
        Agaricaceae, Aizoaceae, Albatrellaceae, Amaranthaceae, Amaryllidaceae, Apiaceae, Asparagaceae,
        Asteraceae, Betulaceae, Boletaceae, Brassicaceae, Campanulaceae, Cantharellaceae, Caprifoliaceae,
        Caryophyllaceae, Cladoniaceae, Convolvulaceae, Crassulaceae, Cucurbitaceae, Cupressaceae, Elaeagnaceae,
        Ericaceae, Fabaceae, Fagaceae, Fomitopsidaceae, Grossulariaceae, Hydnaceae, Lamiaceae,
        Liliaceae, Malvaceae, Morchellaceae, Myricaceae, Oleaceae, Onagraceae, Oxalidaceae,
        Pinaceae, Plantaginaceae, Pleurotaceae, Poaceae, Polygonaceae, Polypodiaceae, Portulacaceae,
        Primulaceae, Rosaceae, Rubiaceae, Russulaceae, Rutaceae, Schisandraceae, Solanaceae,
        Sparassidaceae, Ulmaceae, Urticaceae, Verbenaceae, Violaceae,
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
