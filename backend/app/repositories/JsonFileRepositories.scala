package repositories

import models.ActionColour
import models.climate.Climate
import models.common.Id
import models.job.GardenJob
import models.plant.Plant
import models.user.{GardenMonth, User, UserGrowSettings}
import play.api.Configuration

import java.nio.file.{Path, Paths}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

// Where the temp-db JSON files live. `tempdb.dir` in application.conf; tests point it at a copy.
@Singleton
class TempDb @Inject() (config: Configuration) {
  val dir: Path = Paths.get(config.get[String]("tempdb.dir"))
  def file(name: String): Path = dir.resolve(name)
}

@Singleton
class JsonFilePlantRepository @Inject() (db: TempDb)(implicit ec: ExecutionContext) extends PlantRepository {
  private val table = new JsonFileTable[Plant](db.file("plants.json"))
  def all(): Future[List[Plant]] = table.all()
  def find(id: Id): Future[Option[Plant]] = table.all().map(_.find(_._id == id))
}

@Singleton
class JsonFileClimateRepository @Inject() (db: TempDb)(implicit ec: ExecutionContext) extends ClimateRepository {
  private val table = new JsonFileTable[Climate](db.file("climate.json"))
  def find(id: Id): Future[Option[Climate]] = table.all().map(_.find(_._id == id))
}

@Singleton
class JsonFileGardenJobRepository @Inject() (db: TempDb)(implicit ec: ExecutionContext) extends GardenJobRepository {
  private val table = new JsonFileTable[GardenJob](db.file("garden-jobs.json"))
  def all(): Future[List[GardenJob]] = table.all()
}

@Singleton
class JsonFileActionColourRepository @Inject() (db: TempDb)(implicit ec: ExecutionContext) extends ActionColourRepository {
  private val table = new JsonFileTable[ActionColour](db.file("action-colours.json"))
  def all(): Future[List[ActionColour]] = table.all()
}

@Singleton
class JsonFileUserRepository @Inject() (db: TempDb)(implicit ec: ExecutionContext) extends UserRepository {
  private val table = new JsonFileTable[User](db.file("users.json"))
  def find(id: Id): Future[Option[User]] = table.all().map(_.find(_._id == id))

  def update(id: Id)(change: User => User): Future[Option[User]] =
    table.update(_.map(user => if (user._id == id) change(user) else user)).map(_.find(_._id == id))
}

@Singleton
class JsonFileUserGrowSettingsRepository @Inject() (db: TempDb)(implicit ec: ExecutionContext) extends UserGrowSettingsRepository {
  private val table = new JsonFileTable[UserGrowSettings](db.file("user-grow-settings.json"))
  def findByUser(userId: Id): Future[Option[UserGrowSettings]] = table.all().map(_.find(_.userId == userId))
}

@Singleton
class JsonFileGardenMonthRepository @Inject() (db: TempDb)(implicit ec: ExecutionContext) extends GardenMonthRepository {
  private val table = new JsonFileTable[GardenMonth](db.file("garden-months.json"))
  def findByUser(userId: Id): Future[List[GardenMonth]] = table.all().map(_.filter(_.userId == userId))
}
