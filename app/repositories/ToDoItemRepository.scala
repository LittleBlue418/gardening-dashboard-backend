package repositories

import org.mongodb.scala._
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.ReplaceOptions
import com.google.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import models.{ToDoItem, Timescale, Priority} 

@Singleton
class ToDoItemRepository @Inject() (mongoConnection: MongoConnection)(implicit
    ec: ExecutionContext
) {
  private val collection: MongoCollection[Document] =
    mongoConnection.database.getCollection("toDoItems")

  private def toDoItemToDoc(toDoItem: ToDoItem): Document = Document(
    "_id" -> toDoItem._id,
    "userId" -> toDoItem.userId,
    "title" -> toDoItem.title,
    "done" -> toDoItem.done,
    "timescale" -> (toDoItem.timescale match {
        case Timescale.Short => "short"
        case Timescale.Medium => "medium"
        case Timescale.Long => "long"
    }),
    "priority" -> (toDoItem.priority match {
        case Priority.P1 => 1
        case Priority.P2 => 2
        case Priority.P3 => 3
    })
  )

  private def docToToDoItem(doc: Document): ToDoItem = ToDoItem(
    _id = doc.getObjectId("_id"),
    userId = doc.getString("userId"),
    title = doc.getString("title"),
    done = doc.getBoolean("done"),
    timescale = doc.getString("timescale") match {
        case "short" => Timescale.Short
        case "medium" => Timescale.Medium
        case "long" => Timescale.Long
    }, 
    priority = doc.getInteger("priority").toInt match {
        case 1 => Priority.P1
        case 2 => Priority.P2
        case 3 => Priority.P3
    }
  )

  def findByUser(userId: String): Future[Seq[ToDoItem]] = {
    collection
      .find(equal("userId", userId))
      .toFuture()
      .map(_.map(docToToDoItem))
  }

  def create(item: ToDoItem): Future[ToDoItem] = {
    collection
      .insertOne(toDoItemToDoc(item))
      .toFuture()
      .map(_ => item)
  }

  def update(item: ToDoItem): Future[Boolean] = {
    collection
      .replaceOne(
        equal("_id", item._id),
        toDoItemToDoc(item),
        ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(result => result.getModifiedCount > 0)
  }

  def delete(userId: String, itemId: String): Future[Boolean] = {
    collection
      .deleteOne(
        equal("_id", itemId)
      )
      .toFuture()
      .map(result => result.getDeletedCount > 0)
  }
}
