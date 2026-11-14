package controllers.api

import org.scalatestplus.play._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test._

import java.nio.file.{Files, Path, Paths}
import scala.jdk.CollectionConverters._

// Runs the real routes and controllers against a throwaway copy of temp-db, so writes never touch the real data.
class ApiControllerSpec extends PlaySpec {

  private val holly = "000000000000000000000001"
  private val tomato = "a00000000000000000000001" // saved and selected in users.json

  private def withApp[T](test: (Application, Path) => T): T = {
    val dir = Files.createTempDirectory("tempdb-test")
    Files.list(Paths.get("temp-db")).iterator().asScala.filter(_.toString.endsWith(".json")).foreach(f => Files.copy(f, dir.resolve(f.getFileName)))
    val app = new GuiceApplicationBuilder().configure("tempdb.dir" -> dir.toString).build()
    try running(app)(test(app, dir))
    finally Files.list(dir).iterator().asScala.foreach(Files.delete(_))
  }

  private def get(app: Application, path: String, user: Option[String] = None) =
    route(app, FakeRequest(GET, path).withHeaders(user.map(UserAction.Header -> _).toSeq: _*)).get

  private def send(app: Application, method: String, path: String, user: String = holly) =
    route(app, FakeRequest(method, path).withHeaders(UserAction.Header -> user)).get

  "GET /api/plants" should {
    "list plants, filtered by category or foragability" in withApp { (app, _) =>
      val all = contentAsJson(get(app, "/api/plants")).as[JsArray].value
      val forage = contentAsJson(get(app, "/api/plants?category=forage")).as[JsArray].value
      val foragable = contentAsJson(get(app, "/api/plants?foragable=true")).as[JsArray].value
      all.size must be > forage.size
      forage.foreach(plant => (plant \ "category").as[String] mustBe "forage")
      foragable.foreach(plant => (plant \ "foraging").toOption.exists(_ != JsNull) mustBe true)
      foragable.size must be > forage.size // garden plants that also grow wild
    }

    "reject an unknown category" in withApp { (app, _) =>
      status(get(app, "/api/plants?category=weeds")) mustBe BAD_REQUEST
    }
  }

  "GET /api/plants/:id" should {
    "return one plant, 400 for a malformed id and 404 for an unknown one" in withApp { (app, _) =>
      (contentAsJson(get(app, s"/api/plants/$tomato")) \ "name").as[String] mustBe "Tomato"
      status(get(app, "/api/plants/not-an-id")) mustBe BAD_REQUEST
      status(get(app, "/api/plants/ffffffffffffffffffffffff")) mustBe NOT_FOUND
    }
  }

  "GET /api/jobs?month=" should {
    "only return jobs for that month" in withApp { (app, _) =>
      val may = contentAsJson(get(app, "/api/jobs?month=May")).as[JsArray].value
      may must not be empty
      may.foreach(job => (job \ "months").as[List[String]] must contain("May"))
      status(get(app, "/api/jobs?month=Maytember")) mustBe BAD_REQUEST
    }
  }

  "the /api/me endpoints" should {
    "need a known user in the X-User-Id header" in withApp { (app, _) =>
      status(get(app, "/api/me")) mustBe UNAUTHORIZED
      status(get(app, "/api/me", Some("not-an-id"))) mustBe UNAUTHORIZED
      status(get(app, "/api/me", Some("ffffffffffffffffffffffff"))) mustBe UNAUTHORIZED
      (contentAsJson(get(app, "/api/me", Some(holly))) \ "name").as[String] mustBe "Holly"
    }

    "return the climate the user's grow settings point at" in withApp { (app, _) =>
      (contentAsJson(get(app, "/api/me/climate", Some(holly))) \ "location").as[String] mustBe "Stockholm, Sweden"
      contentAsJson(get(app, "/api/me/garden-months", Some(holly))).as[JsArray].value.size mustBe 12
    }
  }

  "My plants" should {
    "save, select, unselect and remove a plant, keeping selected inside saved, and store it" in withApp { (app, dir) =>
      val basil = (contentAsJson(get(app, "/api/plants?category=herb")).as[JsArray].value
        .find(p => (p \ "name").as[String] == "Basil").get \ "_id").as[String]
      val lists = (r: scala.concurrent.Future[play.api.mvc.Result]) => contentAsJson(r)

      status(send(app, PUT, s"/api/me/selected-plants/$basil")) mustBe CONFLICT // not saved yet

      (lists(send(app, PUT, s"/api/me/saved-plants/$basil")) \ "savedPlantIds").as[List[String]] must contain(basil)
      (lists(send(app, PUT, s"/api/me/selected-plants/$basil")) \ "selectedPlantIds").as[List[String]] must contain(basil)
      (lists(send(app, DELETE, s"/api/me/selected-plants/$basil")) \ "selectedPlantIds").as[List[String]] must not contain basil

      status(send(app, PUT, s"/api/me/selected-plants/$basil")) mustBe OK
      val removed = lists(send(app, DELETE, s"/api/me/saved-plants/$basil"))
      (removed \ "savedPlantIds").as[List[String]] must not contain basil
      (removed \ "selectedPlantIds").as[List[String]] must not contain basil // removing also unselects

      status(send(app, PUT, "/api/me/saved-plants/ffffffffffffffffffffffff")) mustBe NOT_FOUND

      // The change is in the (copied) users.json, not just in memory.
      val stored = Json.parse(Files.readString(dir.resolve("users.json"))).as[JsArray].value.head
      (stored \ "savedPlantIds").as[List[String]] must contain(tomato)
      (stored \ "savedPlantIds").as[List[String]] must not contain basil
    }
  }
}
