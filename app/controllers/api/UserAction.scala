package controllers.api

import models.common.Id
import models.user.User
import play.api.libs.json.Json
import play.api.mvc._
import repositories.UserRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

// A request from a known user. The user id comes in the X-User-Id header (never in the URL), so it doesn't
// end up in logs, browser history or links. Placeholder until there's real authentication.
class UserRequest[A](val user: User, request: Request[A]) extends WrappedRequest[A](request)

class UserAction @Inject() (val parser: BodyParsers.Default, users: UserRepository)(implicit val executionContext: ExecutionContext)
    extends ActionBuilder[UserRequest, AnyContent] with ActionRefiner[Request, UserRequest] {

  protected def refine[A](request: Request[A]): Future[Either[Result, UserRequest[A]]] =
    request.headers.get(UserAction.Header).flatMap(Id.parse) match {
      case None => Future.successful(Left(Results.Unauthorized(Json.obj("error" -> s"Send your user id in the ${UserAction.Header} header"))))
      case Some(id) =>
        users.find(id).map {
          case Some(user) => Right(new UserRequest(user, request))
          case None       => Left(Results.Unauthorized(Json.obj("error" -> "Unknown user")))
        }
    }
}

object UserAction {
  val Header = "X-User-Id"
}
