
import akka.actor.Actor
import spray.routing.HttpService
import spray.http.MediaTypes
import spray.httpx.SprayJsonSupport._
import spray.routing.HttpServiceActor
import spray.http.StatusCodes._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import spray.json.DefaultJsonProtocol
import java.io._
import spray.http.{ MediaTypes, BodyPart, MultipartFormData }
import org.apache.commons.codec.binary
import org.apache.commons.codec.binary.Base64
import java.io.FileOutputStream
//import spray.json.DefaultJsonProtocol._

case class User(userId: Int, name: String, gender: String)
case class Page(pageId: Int, pageName: String, likes: Int)
case class UserPost(postId: Int, admin_creator: Int, post: String)
case class Post(postId: Int, admin_creator: Int, post: String)
case class FriendList(userId: Int, friendList: List[User])
case class FriendRequestsList(userId: Int, requestsList: List[User])
case class userImageJson(userId: String, pictureId: String, Image: String)
case class pageImageJson(pageId: String, pictureId: String, Image: String)

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
  var pageLikeList = scala.collection.mutable.Map[Int, List[Int]]()
  // var pagePostList1 = scala.collection.mutable.Map[Int, PagePost]()
  var pagePostList = scala.collection.mutable.Map[Int, List[Post]]()
  var userPostList = scala.collection.mutable.Map[Int, List[UserPost]]()
  var friendList = scala.collection.mutable.Map[Int, List[User]]()
  var friendRequestsList = scala.collection.mutable.Map[Int, List[User]]()
  var postUserPictureList = scala.collection.mutable.Map[Int, List[userImageJson]]()
  var postPagePictureList = scala.collection.mutable.Map[Int, List[pageImageJson]]()
  var pagePostCounter = 1
  var userPostCounter = 1

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
      } ~ post {
        path("registerPage") {
          parameters("pageId".as[Int], "pageName".as[String]) { (pageId, pageName) =>
            pageList += pageId -> Page(pageId, pageName, 0)
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
            updatePageLikeList(pageId, userId)
            complete {
              "OK"
            }
          }
        }
      } ~ post {
        path("unlikePage") {
          parameters("pageId".as[Int], "userId".as[Int]) { (pageId, userId) =>
            updateUnlike(pageId, userId)
            complete {
              "OK"
            }
          }
        }
      } ~ post {
        path("pagePost") {
          parameters("pageId".as[Int], "post".as[String]) { (pageId, post) =>
            pagePost(pageId, post)
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
            deletePagePost(pageId, postId)
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
            friendRequest(userId, friendId)
            complete {
              "OK"
            }

          }
        }
      } ~ post {
        path("approveDeclineRequest") {
          parameters("userId".as[Int], "friendId".as[Int], "decision".as[Boolean]) { (userId, friendId, decision) =>
            approveDeclineRequest(userId, friendId, decision)
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
            userPost(userId, fromUser, post)
            complete {
              "OK"
            }
          }
        }
      } ~ post {
        path("deletePost") {
          parameters("userId".as[Int], "fromUser".as[Int], "postId".as[Int]) { (userId, fromUser, postId) =>
            deleteUserPost(userId, fromUser, postId)
            complete {
              "OK"
            }
          }
        }
      }  ~ post {
        path("userAlbum") {
          entity(as[userImageJson]) { (pictureJson) =>
           setUserPicture(pictureJson)
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
      }  ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("user" / IntNumber / "picture"/ IntNumber) { (userId, pictureId) =>
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
           setPagePicture(pictureJson)
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
      }  ~ respondWithMediaType(MediaTypes.`application/json`) {
        path("page" / IntNumber / "picture"/ IntNumber) { (pageId, pictureId) =>
          get {
            
             getPagePictureIndex(pageId, pictureId) match {
              case Some(userRoute) => complete(userRoute)
              case None            => complete(NotFound -> s"No pictures for page id $pageId was found!")
            }
          }
        }
      }    

  }

  //  def incrementLikeCount(pageId: Int, userId: Int) = {
  //    pageList(pageId) = Page(pageId, pageList(pageId).pageName, pageList(pageId).likes + 1)
  //  }

  def updatePageLikeList(pageId: Int, userId: Int) = {
    if (!pageLikeList.contains(pageId)) {
      pageLikeList += pageId -> List(userId)
      pageList(pageId) = Page(pageId, pageList(pageId).pageName, pageList(pageId).likes + 1)
      //pageLikeList foreach {case (key, value) => println (key + "---->" + value.toList)}
    } else {
      if (!pageLikeList(pageId).contains(userId)) {
        pageLikeList(pageId) ::= userId
        pageList(pageId) = Page(pageId, pageList(pageId).pageName, pageList(pageId).likes + 1)
      }
      //pageLikeList foreach {case (key, value) => println (key + "-->" + value.toList)}
    }

  }
  def updateUnlike(pageId: Int, userId: Int) = {
    if (pageLikeList.contains(pageId) && pageLikeList(pageId).contains(userId)) {
      var index = pageLikeList(pageId).indexOf(userId)
      pageLikeList(pageId) = pageLikeList(pageId).take(index) ++ pageLikeList(pageId).drop(index + 1)
      pageList(pageId) = Page(pageId, pageList(pageId).pageName, pageList(pageId).likes - 1)
      //pageLikeList foreach {case (key, value) => println (key + "-->" + value.toList)}
    }

  }

  //  def pagePost1(pageId:Int, post:String) = {
  //    if (!pagePostList.contains(pageId)) {
  //      pagePostList += pageId -> PagePost(pageId, List(post))
  //    }else{
  //     var tempPostList:List[String] = pagePostList(pageId).posts
  //     tempPostList ::= post
  //     pagePostList(pageId) = PagePost(pageId, tempPostList)
  //      //pagePostList(pageId) ::= post
  //    }
  //    
  //  }

  def pagePost(pageId: Int, post: String) = {

    if (!pagePostList.contains(pageId)) {
      pagePostList += pageId -> List(Post(100, pageId, post))
    } else {
      // println(pagePostList(pageId).toList)
      userPostCounter = userPostCounter + 1
      pagePostList(pageId) ::= Post(pagePostList(pageId).size+100, pageId, post)
      // println(pagePostList(pageId).toList)

    }

  }
  
 

  def userPost(userId: Int, fromUser: Int, post: String) = {

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
        // println(pagePostList(pageId).toList)
        println(userPostCounter)
        userPostCounter = userPostCounter + 1
        userPostList(userId) ::= UserPost(userPostList(userId).size+100, fromUser, post)
        // println(pagePostList(pageId).toList)

      }
    }

  }

  def deletePagePost(pageId: Int, postId: Int) = {
    if (pagePostList.contains(pageId)) {
      var tempPostList: List[Post] = pagePostList(pageId)
      var i = 0
      for (i <- 0 to tempPostList.size - 1) {
        if (tempPostList(i).postId == postId) {
          tempPostList = tempPostList.take(i) ++ tempPostList.drop(i + 1)
        }
      }
      pagePostList(pageId) = tempPostList
    }

  }

  def deleteUserPost(userId: Int, fromUser: Int, postId: Int) = {
    if (userPostList.contains(userId)) {
      var tempPostList: List[UserPost] = userPostList(userId)
      var i = 0
      for (i <- 0 to tempPostList.size-1) {
        if (tempPostList(i).postId == postId && (tempPostList(i).admin_creator == userId || tempPostList(i).admin_creator == fromUser)) {
          println(tempPostList.size +" "+i)
          tempPostList = tempPostList.take(i) ++ tempPostList.drop(i + 1)
        }
      }
      userPostList(userId) = tempPostList
    }

  }

  def friendRequest(userId: Int, friendId: Int) = {
    if (!friendRequestsList.contains(userId)) {
      friendRequestsList += userId -> List(userList(friendId))
    } else {
      if (!friendRequestsList(userId).contains(userList(friendId))) {
        friendRequestsList(userId) ::= userList(friendId)
      }
    }
  }

  def approveDeclineRequest(userId: Int, friendId: Int, decision: Boolean) = {
    if (!friendRequestsList.isEmpty && friendRequestsList.contains(userId)) {

      var tempRequestsList: List[User] = friendRequestsList(userId)
      for (i <- 0 to tempRequestsList.size - 1) {
        if (tempRequestsList(i).userId == friendId) {
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
          //println(friendRequestsList(userId))
          tempRequestsList = tempRequestsList.take(i) ++ tempRequestsList.drop(i + 1)
          friendRequestsList(userId) = tempRequestsList
          //println(friendRequestsList(userId))
        }
      }
    }
  }
  
  def setUserPicture(imageJson: userImageJson ) = {
    if (!postUserPictureList.contains(imageJson.userId.toInt)) {
              postUserPictureList += imageJson.userId.toInt -> List(imageJson)

            } else {
              postUserPictureList(imageJson.userId.toInt) ::= imageJson
            }
  }
  
  def getUserPictureIndex(userId: Int,pictureId: Int) : Option[userImageJson]= {
    var tempPostList: List[userImageJson] = postUserPictureList(userId)
      var i = 0
      for (i <- 0 to tempPostList.size - 1) {
        if (tempPostList(i).pictureId.toInt == pictureId) {
          return Some(tempPostList(i))
        }
      }
    return None
  }
  
   def setPagePicture(imageJson: pageImageJson ) = {
    if (!postPagePictureList.contains(imageJson.pageId.toInt)) {
              postPagePictureList += imageJson.pageId.toInt -> List(imageJson)

            } else {
              postPagePictureList(imageJson.pageId.toInt) ::= imageJson
            }
  }
  
  def getPagePictureIndex(pageId: Int,pictureId: Int) : Option[pageImageJson]= {
    var tempPostList: List[pageImageJson] = postPagePictureList(pageId)
      var i = 0
      for (i <- 0 to tempPostList.size - 1) {
        if (tempPostList(i).pictureId.toInt == pictureId) {
          return Some(tempPostList(i))
        }
      }
    return None
  }
}

