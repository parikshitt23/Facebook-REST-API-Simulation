import scala.collection.mutable.ArrayBuffer
import scala.util.Random

import akka.actor._
import akka.actor.Actor
import akka.actor.ActorDSL._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.routing.RoundRobinRouter
import spray.http._
import spray.client.pipelining._
import spray.routing._
import spray.http.MediaTypes
import spray.routing.Directive.pimpApply
import spray.routing.SimpleRoutingApp
import spray.routing.directives.ParamDefMagnet.apply
import scala.concurrent.Future
import scala.util.{ Success, Failure }
import spray.client.pipelining.{ Get, sendReceive }
import spray.client.pipelining.{ Post, sendReceive }
import java.io._
import spray.http.{ MediaTypes, BodyPart, MultipartFormData }
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import java.util.concurrent.TimeUnit;
import akka.util
import java.nio.file.{Files, Paths}
import org.apache.commons.codec.binary
import org.apache.commons.codec.binary.Base64
//import scala.concurrent.ExecutionContext.Implicits.global

import spray.httpx._

import spray.httpx.SprayJsonSupport
import spray.json.AdditionalFormats

case class RegisterUser(userId: Int)
case class GetUser(userId: Int)
case class RegisterPage(pageId: Int)
case class GetPage(pageId: Int)
case class likePage(pageId: Int, userId: Int)
case class pagePost(pageId: Int)
case class userPost(userId: Int, fromUser: Int)
case class unlikePage(pageId: Int, userId: Int)
case class getPageFeed(pageId: Int)
case class getUserFeed(userId: Int)
case class deletePagePost(pageId: Int, postId: Int)
case class deleteUserPost(userId: Int, fromUser: Int, postId: Int)
case class getFriendList(userId: Int)
case class getFriendRequestList(userId: Int)
case class sendFriendRequest(userId: Int, friendId: Int)
case class postPicture(userId: Int, pictureId: Int)
case class getPicture(userId: Int, pictureId: Int)
case class approveDeclineRequest(userId: Int, friendId: Int, decision: Boolean)
case class initialNetwork()
case class simulateVisitPage()
case class simulatePostPage()
case class simulateLikePage()
case class simulateReadPageFeed()
case class simulateFriendRequest()
case class simulateStatusUpdate()
case class simulateReadUserFeed()
case class simulateVisitUserProfile()
case class simulateapproveDeclineRequest()
case class simulatePostPicture()

object FacebookClient extends App {

  override def main(args: Array[String]) {

    val system = ActorSystem("FacebookClientSystem")
    val master: ActorRef = system.actorOf(Props(new networkSimulator(system)), name = "Master")

    master ! initialNetwork()
    //    import system.dispatcher
    //    system.scheduler.schedule(FiniteDuration(2000, TimeUnit.MILLISECONDS), FiniteDuration(100, TimeUnit.MILLISECONDS), userActors(Random.nextInt(999)), GetPage(Random.nextInt(999)))

    //    for(i<- 0 until 100)
    //    userActors(i)!GetPage(i)
    // 
    //    userActors(1) ! likePage(0,1)
    //    
    //    userActors(2) ! pagePost(0)
    //    
    //    userActors(0) ! userPost(0,0)
    //    
    //    userActors(0)! imageUpload()
  }

}

class networkSimulator(system: ActorSystem) extends Actor {
  val userActors = new ArrayBuffer[ActorRef]()
  val pageActors = new ArrayBuffer[ActorRef]()
  var visitPageIntializer: Cancellable = null
  import context.dispatcher
  def receive = {

    case initialNetwork() => {
      for (i <- 0 until 1000) {
        userActors += system.actorOf(Props(new Client(system)), name = "User" + i)
        pageActors += system.actorOf(Props(new Client(system)), name = "Page" + i)
      }
      for (i <- 0 until 1000) {
        userActors(i) ! RegisterUser(i)
        pageActors(i) ! RegisterPage(i)
      }
       visitPageIntializer =context.system.scheduler.schedule(FiniteDuration(2000, TimeUnit.MILLISECONDS),FiniteDuration(100, TimeUnit.MILLISECONDS), self, simulateVisitPage())
       context.system.scheduler.schedule(FiniteDuration(3000, TimeUnit.MILLISECONDS),FiniteDuration(100, TimeUnit.MILLISECONDS), self, simulateVisitPage())
       context.system.scheduler.schedule(FiniteDuration(3000, TimeUnit.MILLISECONDS),FiniteDuration(500, TimeUnit.MILLISECONDS), self, simulateLikePage())
       context.system.scheduler.schedule(FiniteDuration(3000, TimeUnit.MILLISECONDS),FiniteDuration(10, TimeUnit.MILLISECONDS), self, simulatePostPage())
       context.system.scheduler.schedule(FiniteDuration(3000, TimeUnit.MILLISECONDS),FiniteDuration(100, TimeUnit.MILLISECONDS), self, simulateReadPageFeed())
       context.system.scheduler.schedule(FiniteDuration(3000, TimeUnit.MILLISECONDS),FiniteDuration(10, TimeUnit.MILLISECONDS), self, simulateStatusUpdate())
       context.system.scheduler.schedule(FiniteDuration(3000, TimeUnit.MILLISECONDS),FiniteDuration(100, TimeUnit.MILLISECONDS), self, simulateReadUserFeed())
       context.system.scheduler.schedule(FiniteDuration(3000, TimeUnit.MILLISECONDS),FiniteDuration(100, TimeUnit.MILLISECONDS), self, simulateFriendRequest())
       context.system.scheduler.schedule(FiniteDuration(3000, TimeUnit.MILLISECONDS),FiniteDuration(100, TimeUnit.MILLISECONDS), self, simulateapproveDeclineRequest())
       context.system.scheduler.schedule(FiniteDuration(3000, TimeUnit.MILLISECONDS),FiniteDuration(50000, TimeUnit.MILLISECONDS), self, simulatePostPicture())
    }
    
    case simulateVisitPage()=>{
      var actorIndex = Random.nextInt(999)
      var pageIndex = Random.nextInt(999)
      userActors(actorIndex) ! GetPage(pageIndex)
    }
    
    case simulateVisitUserProfile()=>{
      var actorIndex = Random.nextInt(999)
      var userIndex = Random.nextInt(999)
      userActors(actorIndex) ! GetUser(userIndex)
    }
    
    case simulateLikePage()=>{
      var actorIndex = Random.nextInt(999)
      var pageIndex = Random.nextInt(999)
      userActors(actorIndex) ! likePage(pageIndex,actorIndex)
    }
    
    
    case simulatePostPage()=>{
      var actorIndex = Random.nextInt(999)
      var pageIndex = Random.nextInt(999)
      pageActors(actorIndex) ! pagePost(pageIndex)
    }
    case simulateReadPageFeed()=>{
      var actorIndex = Random.nextInt(999)
      var pageIndex = Random.nextInt(999)
      userActors(actorIndex) ! getPageFeed(pageIndex)
    }
    
    case simulateFriendRequest()=>{
      var actorIndex = Random.nextInt(999)
      var friendIndex = Random.nextInt(999)
      userActors(actorIndex) ! sendFriendRequest(friendIndex,actorIndex)
    }
    case simulateapproveDeclineRequest()=>{
      var actorIndex = Random.nextInt(999)
      var friendIndex = Random.nextInt(999)
      userActors(actorIndex) ! approveDeclineRequest(actorIndex,friendIndex,true)
    }
    
    case simulateStatusUpdate()=>{
      var actorIndex = Random.nextInt(999)
      userActors(actorIndex) ! userPost(actorIndex,actorIndex)
    }
    
   case simulateReadUserFeed()=>{
      var actorIndex = Random.nextInt(999)
      var UserIndex = Random.nextInt(999)
      userActors(actorIndex) ! getUserFeed(UserIndex)
    }
   
   case simulatePostPicture() =>{
     userActors(0) ! postPicture(1,1)
     userActors(0) ! postPicture(1,2)
     
   }
    
    

  }
  

}

class Client(system: ActorSystem) extends Actor {
  import system.dispatcher
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  def receive = {
    case RegisterUser(userId: Int) => {

      val gender = Array("male", "female")
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=" + userId + "&name=" + Random.alphanumeric.take(Random.nextInt(50)).mkString + "&gender=" + Random.shuffle(gender.toList).head))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))
    }
    case RegisterPage(pageId: Int) => {
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerPage?pageId=" + pageId + "&pageName=" + Random.alphanumeric.take(Random.nextInt(50)).mkString))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))

    }
    case GetUser(userId: Int) => {
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/user/" + userId))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))
      response.foreach(
        response =>
          println(s"User Profile :\n${response.entity.asString}"))
    }

    case GetPage(pageId: Int) => {
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/page/" + pageId))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))
//      response.foreach(
//        response =>
//          println(s"Page Info :\n${response.entity.asString}"))
    }

    case likePage(pageId: Int, userId: Int) => {
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/likePage?pageId=" + pageId + "&userId=" + userId))
    }

    case unlikePage(pageId: Int, userId: Int) => {
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/unlikePage?pageId=" + pageId + "&userId=" + userId))
    }

    case pagePost(pageId: Int) => {
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/pagePost?pageId=" + pageId + "&post=" + Random.alphanumeric.take(Random.nextInt(140)).mkString))
    }

    case getPageFeed(pageId: Int) => {
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/page/" + pageId + "/feed"))
      
      response.foreach(
        response =>
          println(s"Page Feed :\n${response.entity.asString}"))
    }

    case deletePagePost(pageId: Int, postId: Int) => {
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/deletePost?pageId=" + pageId + "&postId=" + postId))
    }

    case userPost(userId: Int, fromUser: Int) => {
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/userPost?userId=" + userId + "&fromUser=" + fromUser + "&post=" + Random.alphanumeric.take(Random.nextInt(140)).mkString))
    }

    case getUserFeed(userId: Int) => {
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/user/" + userId + "/feed"))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))
      response.foreach(
        response =>
          println(s"User Feed :\n${response.entity.asString}"))
    }

    case deleteUserPost(userId: Int, fromUser: Int, postId: Int) => {
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/deletePost?userId=" + userId + "&fromUser=" + fromUser + "&postId=" + postId))
    }

    case getFriendList(userId: Int) => {
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/user/" + userId + "/friendsList"))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))
      response.foreach(
        response =>
          println(s"Friend List :\n${response.entity.asString}"))
    }

    case getFriendRequestList(userId: Int) => {
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/user/" + userId + "/friendRequestsList"))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))
      response.foreach(
        response =>
          println(s"Friend Request List :\n${response.entity.asString}"))
    }

    case sendFriendRequest(userId: Int, friendId: Int) => {
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/friendRequest?userId=" + userId + "&friendId=" + friendId))
    }

    case approveDeclineRequest(userId: Int, friendId: Int, decision: Boolean) => {
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/approveDeclineRequest?userId=" + userId + "&friendId=" + friendId + "&decision=" + decision))
    }

case postPicture(userId: Int, pictureId: Int) => {
      
      val byteArray = Files.readAllBytes(Paths.get("src/abc.jpg"))
      println(byteArray)
      val base64String = Base64.encodeBase64String(byteArray);
      val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
       val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/album",HttpEntity(MediaTypes.`application/json`,s"""{
        "userId": "$userId",
        "pictureId" : "$pictureId", 
        "Image": "$base64String"
    }""")))
    
    println("Reachaed Test Json")
    }

case getPicture(userId: Int, pictureId: Int) => {
  val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/user/" + userId + "/picture/"+pictureId))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))
//      response.foreach(
//        response =>
//          println(s"picture  :\n${response.entity.asString}"))
  
}

   

  }
}


