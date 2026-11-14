package repositories

import play.api.libs.json._

import java.nio.file.{Files, Path, StandardCopyOption}
import scala.concurrent.{ExecutionContext, Future, blocking}

// One temp-db JSON file holding an array of records. Reads the whole file and writes it back whole
// (the files are small). Writes go to a temp file first, then replace the real one, so a crash
// mid-write can't leave half a file. `synchronized` keeps two writes from interleaving.
class JsonFileTable[T: Format](file: Path)(implicit ec: ExecutionContext) {

  def all(): Future[List[T]] = Future(blocking(read()))

  // Read, change, write back as one step, so concurrent updates don't lose each other's changes.
  def update(change: List[T] => List[T]): Future[List[T]] = Future(blocking(synchronized {
    val updated = change(read())
    val temp = file.resolveSibling(file.getFileName.toString + ".tmp")
    Files.writeString(temp, Json.prettyPrint(Json.toJson(updated)) + "\n")
    Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    updated
  }))

  private def read(): List[T] = synchronized {
    Json.parse(Files.readString(file)).validate[List[T]] match {
      case JsSuccess(rows, _) => rows
      case JsError(errors)    => throw new IllegalStateException(s"$file doesn't match its model: $errors")
    }
  }
}
