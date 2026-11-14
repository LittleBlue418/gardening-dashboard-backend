package repositories

import models.ActionColour
import models.climate.Climate
import models.common.Id
import models.job.GardenJob
import models.plant.Plant
import models.user.{GardenMonth, User, UserGrowSettings}

import scala.concurrent.Future

// What the app needs from storage. Controllers and services only see these traits; today they're backed
// by the temp-db JSON files (JsonFileRepositories), and a database can replace them later in Module.scala.

trait PlantRepository {
  def all(): Future[List[Plant]]
  def find(id: Id): Future[Option[Plant]]
}

trait ClimateRepository {
  def find(id: Id): Future[Option[Climate]]
}

trait GardenJobRepository {
  def all(): Future[List[GardenJob]]
}

trait ActionColourRepository {
  def all(): Future[List[ActionColour]]
}

trait UserRepository {
  def find(id: Id): Future[Option[User]]
  def update(id: Id)(change: User => User): Future[Option[User]] // None if there's no such user
}

trait UserGrowSettingsRepository {
  def findByUser(userId: Id): Future[Option[UserGrowSettings]]
}

trait GardenMonthRepository {
  def findByUser(userId: Id): Future[List[GardenMonth]]
}
