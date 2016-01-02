
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor._
import akka.actor.Actor
import akka.actor.ActorDSL._
import scala.concurrent.Future
import akka.actor.actorRef2Scala
import akka.routing.RoundRobinRouter
import scala.concurrent.Await
import spray.routing.HttpService
import spray.http.MediaTypes
import akka.pattern.ask
import spray.httpx.SprayJsonSupport._
import spray.routing.HttpServiceActor
import spray.http.StatusCodes._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.concurrent.duration._
import spray.json.DefaultJsonProtocol
import java.io._
import spray.http.{ MediaTypes, BodyPart, MultipartFormData }
import org.apache.commons.codec.binary
import org.apache.commons.codec.binary.Base64
import java.io.FileOutputStream
import akka.util.Timeout
//import spray.json.DefaultJsonProtocol._

case class User(userId: Int, name: String, gender: String)
case class Page(pageId: Int, pageName: String, likes: Int)
case class UserPost(postId: Int, admin_creator: Int, post: String)
case class Post(postId: Int, admin_creator: Int, post: String)
case class FriendList(userId: Int, friendList: List[User])
case class FriendRequestsList(userId: Int, requestsList: List[User])
case class userImageJson(userId: String, pictureId: String, Image: String)
case class pageImageJson(pageId: String, pictureId: String, Image: String)
case class statistics(pagePost_Count: Int, userPost_Count: Int, Total_Number_of_Posts: Int, picture_Post_count: Int, Number_of_friendRequests_sent: Int)
case class setPageList(userList: scala.collection.mutable.Map[Int, Page])
case class setUserList(userListFrom: scala.collection.mutable.Map[Int, User])
case class updatePageLikeList(pageId: Int, userId: Int, pageList: scala.collection.mutable.Map[Int, Page], pageLikeList: scala.collection.mutable.Map[Int, List[Int]])
//*********************************************************************************************************************
case class ObjectForLike(pageList: scala.collection.mutable.Map[Int, Page], pageLikeList: scala.collection.mutable.Map[Int, List[Int]])
case class pagePost(pageId: Int, post: String, pagePostList: scala.collection.mutable.Map[Int, List[Post]])
case class userPostMethod(userId: Int, fromUser: Int, post: String, friendList: scala.collection.mutable.Map[Int, List[User]], userPostList: scala.collection.mutable.Map[Int, List[UserPost]])
case class updateUnlike(pageId: Int, userId: Int, pageList: scala.collection.mutable.Map[Int, Page], pageLikeList: scala.collection.mutable.Map[Int, List[Int]])
case class deletePagePost(pageId: Int, postId: Int, pagePostList: scala.collection.mutable.Map[Int, List[Post]])
case class deleteUserPost(userId: Int, fromUser: Int, postId: Int, userPostList: scala.collection.mutable.Map[Int, List[UserPost]])
case class setUserPicture(imageJson: userImageJson, postUserPictureList: scala.collection.mutable.Map[Int, List[userImageJson]])
case class setPagePicture(imageJson: pageImageJson, postPagePictureList: scala.collection.mutable.Map[Int, List[pageImageJson]])
case class friendRequest(userId: Int, friendId: Int, friendRequestsList: scala.collection.mutable.Map[Int, List[User]], userList: scala.collection.mutable.Map[Int, User])
case class approveDeclineRequest(userId: Int, friendId: Int, decision: Boolean, friendList: scala.collection.mutable.Map[Int, List[User]], friendRequestsList: scala.collection.mutable.Map[Int, List[User]], userList: scala.collection.mutable.Map[Int, User])
case class ObjectForFriend(friendList: scala.collection.mutable.Map[Int, List[User]], friendRequestsList: scala.collection.mutable.Map[Int, List[User]], userList: scala.collection.mutable.Map[Int, User])

object userImageJson extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat3(userImageJson.apply)
}

object pageImageJson extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat3(pageImageJson.apply)
}

object User extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat3(User.apply)
}
object Page extends DefaultJsonProtocol {
  implicit var pageFormat = jsonFormat3(Page.apply)
}
object UserPost extends DefaultJsonProtocol {
  implicit var pagePostFormat = jsonFormat3(UserPost.apply)
}
object Post extends DefaultJsonProtocol {
  implicit var pageFormat = jsonFormat3(Post.apply)
}
object FriendList extends DefaultJsonProtocol {
  implicit var pageFormat = jsonFormat2(FriendList.apply)
}
object FriendRequestsList extends DefaultJsonProtocol {
  implicit var pageFormat = jsonFormat2(FriendRequestsList.apply)
}
object statistics extends DefaultJsonProtocol {
  implicit var pageFormat = jsonFormat5(statistics.apply)
}


class ServerActor extends HttpServiceActor {
  override def actorRefFactory = context
  implicit def executionContext = actorRefFactory.dispatcher

  var userList = scala.collection.mutable.Map[Int, User]()
  var pageList = scala.collection.mutable.Map[Int, Page]()
  var pageLikeList = scala.collection.mutable.Map[Int, List[Int]]()
  // var pagePostList1 = scala.collection.mutable.Map[Int, PagePost]()
  var pagePostList = scala.collection.mutable.Map[Int, List[Post]]()
  var userPostList = scala.collection.mutable.Map[Int, List[UserPost]]()
  var friendList = scala.collection.mutable.Map[Int, List[User]]()
  var friendRequestsList = scala.collection.mutable.Map[Int, List[User]]()
  var postUserPictureList = scala.collection.mutable.Map[Int, List[userImageJson]]()
  var postPagePictureList = scala.collection.mutable.Map[Int, List[pageImageJson]]()
  var pagePostCounter = 0
  var userPostCounter = 0
  var totalPostCounter = 0
  var picturePostCount = 0
  var friendRequestSent = 0
  var stat: statistics = null
  val worker = actorRefFactory.actorOf(
    Props[ServerWorker].withRouter(RoundRobinRouter(nrOfInstances = 1000)), name = "worker")

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
            //worker ! setUserList(userList)
            complete {
              "User Created - " + name
            }
          }
        }
      } ~ post {
        path("registerPage") {
          parameters("pageId".as[Int], "pageName".as[String]) { (pageId, pageName) =>
            pageList += pageId -> Page(pageId, pageName, 0)
            //worker ! setPageList(pageList)
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
              case None            => complete(NotFound -> s"No page with id $pageId was found!")
            }
          }
        }
      } ~ post {
        path("likePage") {
          parameters("pageId".as[Int], "userId".as[Int]) { (pageId, userId) =>
            //*********************************************************************************************************************
            implicit val timeout = Timeout(5 seconds)
            var future: Future[ObjectForLike] = ask(worker, updatePageLikeList(pageId, userId, pageList, pageLikeList)).mapTo[ObjectForLike]
            future onSuccess {
              case result => {
                pageList = result.pageList
                pageLikeList = result.pageLikeList
              }

            }
            complete {
              "OK"
            }
          }
        }
      } ~ post {
        path("unlikePage") {
          parameters("pageId".as[Int], "userId".as[Int]) { (pageId, userId) =>
            implicit val timeout = Timeout(5 seconds)
            var future: Future[ObjectForLike] = ask(worker, updateUnlike(pageId, userId, pageList, pageLikeList)).mapTo[ObjectForLike]
            future onSuccess {
              case result => {
                pageList = result.pageList
                pageLikeList = result.pageLikeList
              }
            }
            complete {
              "OK"
            }
          }
        }
      } ~ post {
        path("pagePost") {
          parameters("pageId".as[Int], "post".as[String]) { (pageId, post) =>

            implicit val timeout = Timeout(5 seconds)
            var future: Future[scala.collection.mutable.Map[Int, List[Post]]] = ask(worker, pagePost(pageId, post, pagePostList)).mapTo[scala.collection.mutable.Map[Int, List[Post]]]
            future onSuccess {
              case result => {
                pagePostCounter = pagePostCounter + 1
                pagePostList = result
              }

            }

            complete {
              "OK"
            }
          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("page" / IntNumber / "feed") { (pageId) =>
          get {
            pagePostList.get(pageId) match {
              case Some(userRoute) => complete(userRoute)
              case None            => complete(NotFound -> s"No posts for page id $pageId was found!")
            }
          }
        }
      } ~ post {
        path("deletePost") {
          parameters("pageId".as[Int], "postId".as[Int]) { (pageId, postId) =>
            implicit val timeout = Timeout(5 seconds)
            var future: Future[scala.collection.mutable.Map[Int, List[Post]]] = ask(worker, deletePagePost(pageId, postId, pagePostList)).mapTo[scala.collection.mutable.Map[Int, List[Post]]]
            future onSuccess {
              case result => {
                userPostCounter = userPostCounter + 1
                totalPostCounter = totalPostCounter + 1
                //println(userPostCounter)
                pagePostList = result
              }

            }

            complete {
              "OK"
            }
          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("user" / IntNumber / "friendsList") { (userId) =>
          get {
            friendList.get(userId) match {
              case Some(userRoute) => complete(userRoute)
              case None            => complete(NotFound -> s"No friends for user id $userId was founds!")
            }
          }

        } ~ respondWithMediaType(MediaTypes.`application/json`) {
          path("user" / IntNumber / "friendRequestsList") { (userId) =>
            get {
              friendRequestsList.get(userId) match {
                case Some(userRoute) => complete(userRoute)
                case None            => complete(NotFound -> s"No friends REQUESTS for user id $userId was founds!")
              }
            }

          }
        }
      } ~ post {
        path("friendRequest") {
          parameters("userId".as[Int], "friendId".as[Int]) { (userId, friendId) =>
            implicit val timeout = Timeout(5 seconds)
            var future: Future[scala.collection.mutable.Map[Int, List[User]]] = ask(worker, friendRequest(userId, friendId, friendRequestsList, userList)).mapTo[scala.collection.mutable.Map[Int, List[User]]]
            future onSuccess {
              case result => {
                friendRequestSent = friendRequestSent + 1
                friendRequestsList = result
              }

            }
            complete {
              "OK"
            }

          }
        }
      } ~ post {
        path("approveDeclineRequest") {
          parameters("userId".as[Int], "friendId".as[Int], "decision".as[Boolean]) { (userId, friendId, decision) =>

            implicit val timeout = Timeout(5 seconds)
            var future: Future[ObjectForFriend] = ask(worker, approveDeclineRequest(userId, friendId, decision, friendList, friendRequestsList, userList)).mapTo[ObjectForFriend]
            future onSuccess {
              case result => {
                friendRequestsList = result.friendRequestsList
                friendList = result.friendList
                userList = result.userList
              }
            }
            complete {
              "OK"
            }

          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("user" / IntNumber / "feed") { (userId) =>
          get {
            userPostList.get(userId) match {
              case Some(userRoute) => complete(userRoute)
              case None            => complete(NotFound -> s"No posts for user id $userId was found!")
            }
          }
        }
      } ~ post {
        path("userPost") {
          parameters("userId".as[Int], "fromUser".as[Int], "post".as[String]) { (userId, fromUser, post) =>
            implicit val timeout = Timeout(5 seconds)
            var future: Future[scala.collection.mutable.Map[Int, List[UserPost]]] = ask(worker, userPostMethod(userId, fromUser, post, friendList, userPostList)).mapTo[scala.collection.mutable.Map[Int, List[UserPost]]]
            future onSuccess {
              case result => {
                userPostCounter = userPostCounter + 1
                userPostList = result
              }

            }

            complete {
              "OK"
            }
          }
        }
      } ~ post {
        path("deletePost") {
          parameters("userId".as[Int], "fromUser".as[Int], "postId".as[Int]) { (userId, fromUser, postId) =>
            implicit val timeout = Timeout(5 seconds)
            var future: Future[scala.collection.mutable.Map[Int, List[UserPost]]] = ask(worker, deleteUserPost(userId, fromUser, postId, userPostList)).mapTo[scala.collection.mutable.Map[Int, List[UserPost]]]
            future onSuccess {
              case result => {
                userPostList = result
              }

            }

            complete {
              "OK"
            }
          }
        }
      } ~ post {
        path("userAlbum") {
          entity(as[userImageJson]) { (pictureJson) =>
            implicit val timeout = Timeout(5 seconds)
            var future: Future[scala.collection.mutable.Map[Int, List[userImageJson]]] = ask(worker, setUserPicture(pictureJson, postUserPictureList)).mapTo[scala.collection.mutable.Map[Int, List[userImageJson]]]
            future onSuccess {
              case result => {
                picturePostCount = picturePostCount + 1
                postUserPictureList = result

              }

            }

            complete {
              "OK"
            }
          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("user" / IntNumber / "album") { (userId) =>
          get {
            postUserPictureList.get(userId) match {
              case Some(userRoute) => complete(userRoute)
              case None            => complete(NotFound -> s"No pictures for page id $userId was found!")
            }
          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("user" / IntNumber / "picture" / IntNumber) { (userId, pictureId) =>
          get {

            getUserPictureIndex(userId, pictureId) match {
              case Some(userRoute) => complete(userRoute)
              case None            => complete(NotFound -> s"No pictures for page id $userId was found!")
            }
          }
        }
      } ~ post {
        path("pageAlbum") {
          entity(as[pageImageJson]) { (pictureJson) =>
            implicit val timeout = Timeout(5 seconds)
            var future: Future[scala.collection.mutable.Map[Int, List[pageImageJson]]] = ask(worker, setPagePicture(pictureJson, postPagePictureList)).mapTo[scala.collection.mutable.Map[Int, List[pageImageJson]]]
            future onSuccess {
              case result => {
                picturePostCount = picturePostCount + 1
                postPagePictureList = result
              }
            }
            complete {
              "OK"
            }
          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("page" / IntNumber / "album") { (pageId) =>
          get {
            postPagePictureList.get(pageId) match {
              case Some(userRoute) => complete(userRoute)
              case None            => complete(NotFound -> s"No pictures for page id $pageId was found!")
            }
          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("page" / IntNumber / "picture" / IntNumber) { (pageId, pictureId) =>
          get {

            getPagePictureIndex(pageId, pictureId) match {
              case Some(userRoute) => complete(userRoute)
              case None            => complete(NotFound -> s"No pictures for page id $pageId was found!")
            }
          }
        }
      } ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("Statistics") {
          get {
            complete(statistics(pagePostCounter, userPostCounter, pagePostCounter + userPostCounter, picturePostCount, friendRequestSent))
          }
        }
      }

  }

  def getUserPictureIndex(userId: Int, pictureId: Int): Option[userImageJson] = {
    if (postUserPictureList.contains(userId)) {
      var tempPostList: List[userImageJson] = postUserPictureList(userId)
      var i = 0
      for (i <- 0 to tempPostList.size - 1) {
        if (tempPostList(i).pictureId.toInt == pictureId) {
          return Some(tempPostList(i))
        }
      }

    }

    return None
  }
  def getPagePictureIndex(pageId: Int, pictureId: Int): Option[pageImageJson] = {
    if (postPagePictureList.contains(pageId)) {
      var tempPostList: List[pageImageJson] = postPagePictureList(pageId)
      var i = 0
      for (i <- 0 to tempPostList.size - 1) {
        if (tempPostList(i).pictureId.toInt == pictureId) {
          return Some(tempPostList(i))
        }
      }
    }

    return None
  }

  def receive = {

    runRoute(routes)

  }

}

class ServerWorker extends Actor {
  implicit def executionContext = context.dispatcher

  def receive = {

    //*********************************************************************************************************************
    case updatePageLikeList(pageId: Int, userId: Int, pageList: scala.collection.mutable.Map[Int, Page], pageLikeList: scala.collection.mutable.Map[Int, List[Int]]) => {
      //println("like"+self)
      if (!pageLikeList.contains(pageId)) {
        pageLikeList += pageId -> List(userId)
        // println(pageList.size)
        pageList(pageId) = Page(pageId, pageList(pageId).pageName, pageList(pageId).likes + 1)
        //pageLikeList foreach {case (key, value) => println (key + "---->" + value.toList)}
      } else {
        if (!pageLikeList(pageId).contains(userId)) {
          pageLikeList(pageId) ::= userId
          pageList(pageId) = Page(pageId, pageList(pageId).pageName, pageList(pageId).likes + 1)
        }

      }
      sender ! ObjectForLike(pageList, pageLikeList)
    }

    case updateUnlike(pageId: Int, userId: Int, pageList: scala.collection.mutable.Map[Int, Page], pageLikeList: scala.collection.mutable.Map[Int, List[Int]]) => {
      if (pageLikeList.contains(pageId) && pageLikeList(pageId).contains(userId)) {
        var index = pageLikeList(pageId).indexOf(userId)
        pageLikeList(pageId) = pageLikeList(pageId).take(index) ++ pageLikeList(pageId).drop(index + 1)
        pageList(pageId) = Page(pageId, pageList(pageId).pageName, pageList(pageId).likes - 1)
        //pageLikeList foreach {case (key, value) => println (key + "-->" + value.toList)}
      }
      sender ! ObjectForLike(pageList, pageLikeList)
    }

    case pagePost(pageId: Int, post: String, pagePostList: scala.collection.mutable.Map[Int, List[Post]]) => {

      if (!pagePostList.contains(pageId)) {
        pagePostList += pageId -> List(Post(100, pageId, post))
      } else {
        pagePostList(pageId) ::= Post(pagePostList(pageId).size + 100, pageId, post)

      }
      sender ! pagePostList
    }
    case deletePagePost(pageId: Int, postId: Int, pagePostList: scala.collection.mutable.Map[Int, List[Post]]) => {
      if (pagePostList.contains(pageId)) {
        var tempPostList: List[Post] = pagePostList(pageId)
        var i = 0
        for (i <- 0 to tempPostList.size - 1) {
          if (i < (tempPostList.size - 1) && tempPostList(i).postId == postId) {
            tempPostList = tempPostList.take(i) ++ tempPostList.drop(i + 1)
          }
        }
        pagePostList(pageId) = tempPostList
      }
      sender ! pagePostList

    }

    case userPostMethod(userId: Int, fromUser: Int, post: String, friendList: scala.collection.mutable.Map[Int, List[User]], userPostList: scala.collection.mutable.Map[Int, List[UserPost]]) => {

      var tempFriendList: List[User] = List()
      var isFriend = false
      if (!friendList.isEmpty && userId != fromUser) {
        tempFriendList = friendList(userId)
        //Friendship Check
        for (i <- 0 to tempFriendList.size - 1) {
          if (tempFriendList(i).userId == fromUser)
            isFriend = true
        }
      }

      if (userId == fromUser || isFriend) {
        if (!userPostList.contains(userId)) {
          userPostList += userId -> List(UserPost(100, fromUser, post))
        } else {
          userPostList(userId) ::= UserPost(userPostList(userId).size + 100, fromUser, post)

        }
      }
      sender ! userPostList

    }

    case deleteUserPost(userId: Int, fromUser: Int, postId: Int, userPostList: scala.collection.mutable.Map[Int, List[UserPost]]) => {
      if (userPostList.contains(userId)) {

        var tempPostList: List[UserPost] = userPostList(userId)
        for (i <- 0 to tempPostList.size - 1) {
          if (i < (tempPostList.size - 1) && tempPostList(i).postId == postId && (tempPostList(i).admin_creator == userId || tempPostList(i).admin_creator == fromUser)) {
            tempPostList = tempPostList.take(i) ++ tempPostList.drop(i + 1)
          }
        }
        userPostList(userId) = tempPostList
      }
      sender ! userPostList
    }
    case setUserPicture(imageJson: userImageJson, postUserPictureList: scala.collection.mutable.Map[Int, List[userImageJson]]) => {
      if (!postUserPictureList.contains(imageJson.userId.toInt)) {
        postUserPictureList += imageJson.userId.toInt -> List(imageJson)

      } else {
        postUserPictureList(imageJson.userId.toInt) ::= imageJson
      }
      sender ! postUserPictureList
    }

    case setPagePicture(imageJson: pageImageJson, postPagePictureList: scala.collection.mutable.Map[Int, List[pageImageJson]]) => {
      if (!postPagePictureList.contains(imageJson.pageId.toInt)) {
        postPagePictureList += imageJson.pageId.toInt -> List(imageJson)

      } else {
        postPagePictureList(imageJson.pageId.toInt) ::= imageJson
      }
      sender ! postPagePictureList
    }

    case friendRequest(userId: Int, friendId: Int, friendRequestsList: scala.collection.mutable.Map[Int, List[User]], userList: scala.collection.mutable.Map[Int, User]) => {
      if (!friendRequestsList.contains(userId)) {
        friendRequestsList += userId -> List(userList(friendId))
      } else {
        if (!friendRequestsList(userId).contains(userList(friendId))) {
          friendRequestsList(userId) ::= userList(friendId)
        }
      }

      sender ! friendRequestsList
    }

    case approveDeclineRequest(userId: Int, friendId: Int, decision: Boolean, friendList: scala.collection.mutable.Map[Int, List[User]], friendRequestsList: scala.collection.mutable.Map[Int, List[User]], userList: scala.collection.mutable.Map[Int, User]) => {
      if (!friendRequestsList.isEmpty && friendRequestsList.contains(userId)) {

        var tempRequestsList: List[User] = friendRequestsList(userId)
        for (i <- 0 to tempRequestsList.size - 1) {
          if (i < (tempRequestsList.size - 1) && tempRequestsList(i).userId == friendId) {
            if (decision) {
              if (!friendList.contains(userId)) {
                friendList += userId -> List(userList(friendId))

              } else {
                friendList(userId) ::= userList(friendId)

              }

              if (!friendList.contains(friendId)) {
                friendList += friendId -> List(userList(userId))
              } else {
                friendList(friendId) ::= userList(userId)
              }

            } else {

            }

            tempRequestsList = tempRequestsList.take(i) ++ tempRequestsList.drop(i + 1)
            friendRequestsList(userId) = tempRequestsList

          }
        }
      }

      sender ! ObjectForFriend(friendList, friendRequestsList, userList)
    }
  }

}

