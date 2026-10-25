package gardeningdashboard.domian

import cats.effect.IO

trait UserRepo {
  def findByGoogleId(googleId: String): IO[Option[User]]
  def findByEmail(email: String): IO[Option[User]]
}
