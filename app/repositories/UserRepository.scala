package repositories

import com.google.inject.{Inject, Singleton}
import models.user.User
import org.mongodb.scala._
import org.mongodb.scala.bson.BsonArray
import scala.jdk.CollectionConverters._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.ReplaceOptions
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject() (mongoConnection: MongoConnection)(implicit
    ec: ExecutionContext
) {
  private val collection: MongoCollection[Document] =
    mongoConnection.database.getCollection("users")

  private def userToDoc(user: User): Document = Document(
    "googleId" -> user.googleId,
    "email" -> user.email,
    "name" -> user.name,
    "pictureUrl" -> user.pictureUrl,
    "savedPlantIds" -> user.savedPlantIds,
    "selectedPlantIds" -> user.selectedPlantIds
  )

  private def docToUser(doc: Document): User = User(
    googleId = doc.getString("googleId"),
    email = doc.getString("email"),
    name = doc.getString("name"),
    pictureUrl = Option(doc.getString("pictureUrl")),
    savedPlantIds = objectIds(doc, "savedPlantIds"),
    selectedPlantIds = objectIds(doc, "selectedPlantIds")
  )

  // A list of ObjectIds stored as a BSON array; missing = empty.
  private def objectIds(doc: Document, field: String): List[org.bson.types.ObjectId] =
    doc.get[BsonArray](field).map(_.getValues.asScala.toList.map(_.asObjectId().getValue)).getOrElse(Nil)

  def findByGoogleId(googleId: String): Future[Option[User]] = {
    collection
      .find(equal("googleId", googleId))    // FindObservable[Document]
      .headOption()                         // SingleObservable[Option[Document]]
      .map(_.map(docToUser))                // Future[Option[User]]
  }

  def findByEmail(email: String): Future[Option[User]] = {
    collection
      .find(equal("email", email)) 
      .headOption() 
      .map(_.map(docToUser)) 
  }

  def create(user: User): Future[User] = {
    collection
      .insertOne(userToDoc(user))
      .toFuture()
      .map(_ => user)
  }

  def update(user: User): Future[User] = {
    collection
        .replaceOne(
            equal("googleId", user.googleId),
            userToDoc(user),
            ReplaceOptions().upsert(true)
        )
        .toFuture()
        .map(_ => user)
  }
}
