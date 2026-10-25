package gardeningdashboard.domian

import cats.effect.IO

trait BedRepo {
  def findById(googleId: String): IO[Option[User]]

}