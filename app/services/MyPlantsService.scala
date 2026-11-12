package services

import models.common.Id
import models.user.User
import repositories.{PlantRepository, UserRepository}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

// A user's "My plants": saved plants, and the selected ones they're growing this year.
// The rule lives here, once: selected is always a subset of saved.
@Singleton
class MyPlantsService @Inject() (users: UserRepository, plants: PlantRepository)(implicit ec: ExecutionContext) {
  import MyPlantsService._

  def save(user: User, plantId: Id): Future[Result] =
    ifPlantExists(plantId)(change(user)(u => u.copy(savedPlantIds = (u.savedPlantIds :+ plantId).distinct)))

  // Removing a plant also unselects it.
  def remove(user: User, plantId: Id): Future[Result] =
    change(user)(u => u.copy(savedPlantIds = u.savedPlantIds.filterNot(_ == plantId), selectedPlantIds = u.selectedPlantIds.filterNot(_ == plantId)))

  def select(user: User, plantId: Id): Future[Result] =
    if (!user.savedPlantIds.contains(plantId)) Future.successful(NotSaved)
    else change(user)(u => u.copy(selectedPlantIds = (u.selectedPlantIds :+ plantId).distinct))

  def unselect(user: User, plantId: Id): Future[Result] =
    change(user)(u => u.copy(selectedPlantIds = u.selectedPlantIds.filterNot(_ == plantId)))

  private def ifPlantExists(plantId: Id)(action: => Future[Result]): Future[Result] =
    plants.find(plantId).flatMap {
      case Some(_) => action
      case None    => Future.successful(UnknownPlant)
    }

  private def change(user: User)(update: User => User): Future[Result] =
    users.update(user._id)(update).map(_.fold[Result](UnknownUser)(Updated(_)))
}

object MyPlantsService {
  sealed trait Result
  case class Updated(user: User) extends Result
  case object NotSaved extends Result // can't select a plant that isn't saved
  case object UnknownPlant extends Result
  case object UnknownUser extends Result
}
