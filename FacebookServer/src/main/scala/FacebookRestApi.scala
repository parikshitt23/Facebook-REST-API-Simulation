
import akka.actor.Actor
import spray.routing.HttpService
import spray.http.MediaTypes
import spray.httpx.SprayJsonSupport._
import spray.routing.HttpServiceActor
import spray.http.StatusCodes._

import spray.json.DefaultJsonProtocol

case class User(userId: Int, name: String, gender: String)
case class Page(pageId: Int, pageName: String, likes: Int)

object User extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat3(User.apply)
}
object Page extends DefaultJsonProtocol {
  implicit val pageFormat = jsonFormat3(Page.apply)
}

class ServerActor extends HttpServiceActor {
  override def actorRefFactory = context

  val userRoute = new UserRoute {
    override implicit def actorRefFactory = context
  }

  def receive = runRoute(userRoute.routes)
}

trait UserRoute extends HttpService {
  implicit def executionContext = actorRefFactory.dispatcher

  var userList = scala.collection.mutable.Map[Int, User]()
  var pageList = scala.collection.mutable.Map[Int, Page]()

  val routes = {
    respondWithMediaType(MediaTypes.`application/json`) {
      path("user" / IntNumber) { (userId) =>
        get {
          userList.get(userId) match {
            case Some(userRoute) => complete(userRoute)
            case None            => complete(NotFound -> s"No user with id $userId was found!")
          }
        }
      }
    } ~
      post {
        path("registerUser") {
          parameters("userId".as[Int], "name".as[String], "gender".as[String]) { (userId, name, gender) =>
            userList += userId -> User(userId, name, gender)
            complete {
              "User Created - " + name
            }
          }
        }
      }  ~ post {
        path("registerPage") {
          parameters("pageId".as[Int], "pageName".as[String]) { (pageId, pageName) =>
            pageList += pageId -> Page(pageId, pageName, 1)
            complete {
              "Page Created - " + pageName
            }
          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("page" / IntNumber) { (pageId) =>
          get {
            pageList.get(pageId) match {
              case Some(userRoute) => complete(userRoute)
              case None            => complete(NotFound -> s"No user with id $pageId was found!")
            }
          }
        }
      }

  }

}

