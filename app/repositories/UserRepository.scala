package repositories

import com.google.inject.{Inject, Singleton}
import models.User
import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.ReplaceOptions
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject() (mongoConnection: MongoCollection)(implicit
    ec: ExecutionContext
) {
  private val collection: MongoCollection[Document] =
    mongoConnection.database.getCollection("users")

  private def userToDoc(user: User): Document = Document(
    "googleId" -> user.googleId,
    "email" -> user.email,
    "name" -> user.name,
    "pictureUrl" -> user.pictureUrl
  )

  private def docToUser(doc: Document): User = User(
    googleId = doc.getString("googleId"),
    email = doc.getString("email"),
    name = doc.getString("name"),
    pictureUrl = doc.getString("pictureUrl")
  )

  def findByGoogleId(googleId: String): Future[Option[User]] = {
    collection
      .find(equal("googleId", googleId))    // FindObservable[Document]
      .headOption()                         // SingleObservable[Option[Document]]
      .toFuture()                           // Future[Option[Document]]
      .map(_.map(docToUser))                // Future[Option[User]]
  }

  def findByEmail(email: String): Future[Option[User]] = {
    collection
      .find(equal("email", email)) 
      .headOption() 
      .toFuture() 
      .map(_.map(docToUser)) 
  }

  def create(user: User): Future[User] = {
    collection
      .insertOne(userToDoc(user))
      .toFuture()
      .map(_ => user)
  }

  def update(user: User): Future[Boolean] = {
    collection
        .replaceOne(
            equal("googleId", user.googleId),
            userToDoc(user),
            ReplaceOptions().upsert(true)
        )
        .toFuture()
        .map(result => result.getModifiedCount > 0)
  }
}
