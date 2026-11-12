package controllers.api

import models.common.{Id, MonthName}
import models.plant.PlantCategory
import play.api.libs.json._
import play.api.mvc._
import repositories._
import services.MyPlantsService
import services.MyPlantsService.{NotSaved, UnknownPlant, UnknownUser, Updated}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

// The dashboard's JSON API. Shared data (plants, climates, jobs) is public; anything about the user goes
// through UserAction, which reads the X-User-Id header.
@Singleton
class ApiController @Inject() (
    cc: ControllerComponents,
    userAction: UserAction,
    plants: PlantRepository,
    climates: ClimateRepository,
    jobs: GardenJobRepository,
    actionColours: ActionColourRepository,
    growSettings: UserGrowSettingsRepository,
    gardenMonths: GardenMonthRepository,
    myPlants: MyPlantsService,
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  private def error(message: String) = Json.obj("error" -> message)
  private def badId(text: String) = Future.successful(BadRequest(error("Invalid id: " + text)))

  // GET /api/plants?category=forage&foragable=true
  def listPlants(category: Option[String], foragable: Option[Boolean]): Action[AnyContent] = Action.async {
    category.map(key => Json.fromJson[PlantCategory](JsString(key))) match {
      case Some(JsError(_)) => Future.successful(BadRequest(error("Unknown category: " + category.getOrElse(""))))
      case parsed =>
        val wanted = parsed.collect { case JsSuccess(value, _) => value }
        plants.all().map { all =>
          val shown = all.filter(plant => wanted.forall(_ == plant.category) && foragable.forall(_ == plant.foraging.isDefined))
          Ok(Json.toJson(shown))
        }
    }
  }

  // GET /api/plants/:id
  def getPlant(id: String): Action[AnyContent] = Action.async {
    Id.parse(id).fold(badId(id))(plantId => plants.find(plantId).map(_.fold(NotFound(error("No such plant")))(plant => Ok(Json.toJson(plant)))))
  }

  // GET /api/climates/:id
  def getClimate(id: String): Action[AnyContent] = Action.async {
    Id.parse(id).fold(badId(id))(climateId => climates.find(climateId).map(_.fold(NotFound(error("No such climate")))(c => Ok(Json.toJson(c)))))
  }

  // GET /api/jobs?month=May
  def listJobs(month: Option[String]): Action[AnyContent] = Action.async {
    month.map(name => Json.fromJson[MonthName](JsString(name))) match {
      case Some(JsError(_)) => Future.successful(BadRequest(error("Unknown month: " + month.getOrElse(""))))
      case parsed =>
        val wanted = parsed.collect { case JsSuccess(value, _) => value }
        jobs.all().map(all => Ok(Json.toJson(all.filter(job => wanted.forall(job.months.contains)))))
    }
  }

  // GET /api/action-colours (until the colours move to the front end)
  def listActionColours: Action[AnyContent] = Action.async(actionColours.all().map(all => Ok(Json.toJson(all))))

  // GET /api/me
  def me: Action[AnyContent] = userAction(request => Ok(Json.toJson(request.user)))

  // GET /api/me/grow-settings
  def myGrowSettings: Action[AnyContent] = userAction.async { request =>
    growSettings.findByUser(request.user._id).map(_.fold(NotFound(error("No grow settings")))(s => Ok(Json.toJson(s))))
  }

  // GET /api/me/climate: the climate the user's grow settings point at
  def myClimate: Action[AnyContent] = userAction.async { request =>
    growSettings.findByUser(request.user._id).flatMap {
      case None           => Future.successful(NotFound(error("No grow settings")))
      case Some(settings) => climates.find(settings.climateId).map(_.fold(NotFound(error("No such climate")))(c => Ok(Json.toJson(c))))
    }
  }

  // GET /api/me/garden-months
  def myGardenMonths: Action[AnyContent] = userAction.async { request =>
    gardenMonths.findByUser(request.user._id).map(months => Ok(Json.toJson(months)))
  }

  // PUT / DELETE /api/me/saved-plants/:plantId and /api/me/selected-plants/:plantId
  def savePlant(plantId: String): Action[AnyContent] = myPlantsChange(plantId)(myPlants.save)
  def removePlant(plantId: String): Action[AnyContent] = myPlantsChange(plantId)(myPlants.remove)
  def selectPlant(plantId: String): Action[AnyContent] = myPlantsChange(plantId)(myPlants.select)
  def unselectPlant(plantId: String): Action[AnyContent] = myPlantsChange(plantId)(myPlants.unselect)

  // Every change answers with the user's lists as they are now.
  private def myPlantsChange(plantId: String)(change: (models.user.User, Id) => Future[MyPlantsService.Result]): Action[AnyContent] =
    userAction.async { request =>
      Id.parse(plantId).fold(badId(plantId)) { id =>
        change(request.user, id).map {
          case Updated(user) => Ok(Json.obj("savedPlantIds" -> user.savedPlantIds, "selectedPlantIds" -> user.selectedPlantIds))
          case NotSaved      => Conflict(error("Save the plant before selecting it"))
          case UnknownPlant  => NotFound(error("No such plant"))
          case UnknownUser   => Unauthorized(error("Unknown user"))
        }
      }
    }
}
