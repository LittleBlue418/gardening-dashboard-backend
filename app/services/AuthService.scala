package services

import com.google.inject.{Inject, Singleton}
import repositories.UserRepository
import scala.concurrent.{ExecutionContext, Future}
import models.User

@Singleton 
class AuthService @Inject() (userRepository: UserRepository)(implicit ec: ExecutionContext){
    def findOrCreateUser(
        googleId: String,
        email: String,
        name: String,
        pictureUrl: Option[String]
    ): Future[User] = {
        userRepository.findByGoogleId(googleId).flatMap{
            case Some(existingUser) => 
                val updatedUser = existingUser.copy(
                    email = email,
                    googleId = googleId,
                    name = name ,
                    pictureUrl = pictureUrl,
                )
                userRepository.update(updatedUser)

            case None => 
                val newUser = User(
                    googleId = googleId,
                    email = email,
                    name = name,
                    pictureUrl = pictureUrl,
                )
                userRepository.create(newUser)
        }
    }
}