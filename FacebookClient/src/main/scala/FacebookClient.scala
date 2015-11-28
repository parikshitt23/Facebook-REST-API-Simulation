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
import java.nio.file.{ Files, Paths }
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
case class postUserPicture(userId: Int, pictureId: Int)
case class getUserPicture(userId: Int, pictureId: Int)
case class postPagePicture(pageId: Int, pictureId: Int)
case class getPagePicture(pageId: Int, pictureId: Int)
case class approveDeclineRequest(userId: Int, friendId: Int, decision: Boolean)
case class initialNetwork(numUsers: Int)
case class simulateVisitPage(actorType: String)
case class simulatePostPage(actorType: String)
case class simulateLikePage(actorType: String)
case class simulateUnlikePage(actorType: String)
case class simulateReadPageFeed(actorType: String)
case class simulateDeletePagePost(actorType: String)
case class simulatedeleteUserPost(actorType: String)
case class simulateFriendRequest(actorType: String)
case class simulateStatusUpdate(actorType: String)
case class simulateReadUserFeed(actorType: String)
case class simulateVisitUserProfile(actorType: String)
case class simulateapproveDeclineRequest(actorType: String)
case class simulategetFriendList(actorType: String)
case class simulategetFriendRequestList(actorType: String)
case class simulatePostPicture(actorType: String)
case class simulatepostUserPicture(actorType: String)
case class simulatepostPagePicture(actorType: String)
case class simulategettUserPicture(actorType: String)
case class simulategetPagePicture(actorType: String)

object FacebookClient extends App {

  override def main(args: Array[String]) {
    var numUser: Int = 0
    if (args.length == 0 || args.length != 1) {
      println("Wrong Arguments");
    } else {
      numUser = args(0).toInt
    }
    val system = ActorSystem("FacebookClientSystem")
    val master: ActorRef = system.actorOf(Props(new networkSimulator(system)), name = "Master")

    master ! initialNetwork(numUser)
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
  val multiMediaSavyUsers = new ArrayBuffer[ActorRef]()
  val lowEngagedUsers = new ArrayBuffer[ActorRef]()
  val textSavyPages = new ArrayBuffer[ActorRef]()
  val highEngagedUsers = new ArrayBuffer[ActorRef]()
  val multiMediaSpecialistPages = new ArrayBuffer[ActorRef]()
  var visitPageIntializer: Cancellable = null
  var numActors = 0
  var numPages = 0
  var numUsers = 0
  var numMultiMediaSavyUsers = 0
  var numLowEngagedUsers = 0
  var numTextSavyPages = 0
  var numHighEngagedUsers = 0
  var numMultiMediaSpecialistPages = 0

  import context.dispatcher
  def receive = {

    case initialNetwork(numberOfActors: Int) => {
      numActors = numberOfActors
      numPages = (numActors * 0.205).toInt
      numUsers = (numActors * 0.795).toInt
      numMultiMediaSavyUsers = (numActors * 0.17).toInt
      numLowEngagedUsers = (numActors * 0.225).toInt
      numTextSavyPages = (numActors * 0.18).toInt
      numHighEngagedUsers = (numActors * 0.4).toInt
      numMultiMediaSpecialistPages = (numActors * 0.025).toInt

      //props all actors

      for (i <- 0 until numMultiMediaSavyUsers) {
        multiMediaSavyUsers += system.actorOf(Props(new Client(system)), name = "multiMediaSavyUsers" + i)
      }
      for (i <- 0 until numLowEngagedUsers) {
        lowEngagedUsers += system.actorOf(Props(new Client(system)), name = "lowEngagedUsers" + i)
      }
      for (i <- 0 until numTextSavyPages) {
        textSavyPages += system.actorOf(Props(new Client(system)), name = "textSavyPages" + i)
      }
      for (i <- 0 until numHighEngagedUsers) {
        highEngagedUsers += system.actorOf(Props(new Client(system)), name = "highEngagedUsers" + i)
      }
      for (i <- 0 until numMultiMediaSpecialistPages) {
        multiMediaSpecialistPages += system.actorOf(Props(new Client(system)), name = "multiMediaSpecialistPages" + i)
      }

      for (i <- 0 until numMultiMediaSavyUsers) {
        multiMediaSavyUsers(i) ! RegisterUser(i)
      }
      for (i <- 0 until numLowEngagedUsers) {
        lowEngagedUsers(i) ! RegisterUser(i + numMultiMediaSavyUsers)
      }
      for (i <- 0 until numTextSavyPages) {
        textSavyPages(i) ! RegisterPage(i)
      }
      for (i <- 0 until numHighEngagedUsers) {
        highEngagedUsers(i) ! RegisterUser(i + numLowEngagedUsers + numMultiMediaSavyUsers)
      }
      for (i <- 0 until (numMultiMediaSpecialistPages)) {
        multiMediaSpecialistPages(i) ! RegisterPage(i + numTextSavyPages)
      }

      // broadcast posts
            for (i <- 0 until numMultiMediaSavyUsers) {
              context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(100, TimeUnit.MILLISECONDS), multiMediaSavyUsers(i), userPost(i, i))  
            }
            for (i <- 0 until numLowEngagedUsers) {
              context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(400, TimeUnit.MILLISECONDS), lowEngagedUsers(i), userPost(i, i))
            }
            for (i <- 0 until numTextSavyPages) {
              context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(200, TimeUnit.MILLISECONDS), textSavyPages(i), pagePost(i))
            }
            for (i <- 0 until numHighEngagedUsers) {
              context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(110, TimeUnit.MILLISECONDS), highEngagedUsers(i), userPost(i, i))
            }
            for (i <- 0 until (numMultiMediaSpecialistPages)) {
              context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(8800, TimeUnit.MILLISECONDS), multiMediaSpecialistPages(i), pagePost(i))
            }

      // broadcast posts
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(100, TimeUnit.MILLISECONDS), self, simulateStatusUpdate("C1"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(400, TimeUnit.MILLISECONDS), self, simulateStatusUpdate("C2"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(200, TimeUnit.MILLISECONDS), self, simulatePostPage("C3"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(110, TimeUnit.MILLISECONDS), self, simulateStatusUpdate("C4"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(8800, TimeUnit.MILLISECONDS), self, simulatePostPage("C5"))

      // Read Page feeds
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(800, TimeUnit.MILLISECONDS), self, simulateReadPageFeed("C1"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(500, TimeUnit.MILLISECONDS), self, simulateReadPageFeed("C2"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(100, TimeUnit.MILLISECONDS), self, simulateReadPageFeed("C4"))

      //Read User feeds
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(800, TimeUnit.MILLISECONDS), self, simulateReadUserFeed("C1"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(500, TimeUnit.MILLISECONDS), self, simulateReadUserFeed("C2"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(100, TimeUnit.MILLISECONDS), self, simulateReadUserFeed("C4"))

      //Like Pages
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(500, TimeUnit.MILLISECONDS), self, simulateLikePage("C1"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(2000, TimeUnit.MILLISECONDS), self, simulateLikePage("C2"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(500, TimeUnit.MILLISECONDS), self, simulateLikePage("C4"))

      //UnLike Pages
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(5000, TimeUnit.MILLISECONDS), self, simulateUnlikePage("C1"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(5000, TimeUnit.MILLISECONDS), self, simulateUnlikePage("C4"))

      //Delete Posts
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(15000, TimeUnit.MILLISECONDS), self, simulatedeleteUserPost("C1"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(10000, TimeUnit.MILLISECONDS), self, simulateDeletePagePost("C3"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(15000, TimeUnit.MILLISECONDS), self, simulatedeleteUserPost("C4"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(10000, TimeUnit.MILLISECONDS), self, simulateDeletePagePost("C5"))

      // Send and ApprovefriendRequests
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(1000, TimeUnit.MILLISECONDS), self, simulateFriendRequest("C1"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(5000, TimeUnit.MILLISECONDS), self, simulateFriendRequest("C2"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(1000, TimeUnit.MILLISECONDS), self, simulateFriendRequest("C4"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(1000, TimeUnit.MILLISECONDS), self, simulateapproveDeclineRequest("C1"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(5000, TimeUnit.MILLISECONDS), self, simulateapproveDeclineRequest("C2"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(1000, TimeUnit.MILLISECONDS), self, simulateapproveDeclineRequest("C4"))

      //Post Pictures
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(100, TimeUnit.MILLISECONDS), self, simulatepostUserPicture("C1"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(800, TimeUnit.MILLISECONDS), self, simulatepostUserPicture("C2"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(880, TimeUnit.MILLISECONDS), self, simulatepostUserPicture("C4"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(800, TimeUnit.MILLISECONDS), self, simulatepostPagePicture("C3"))
      context.system.scheduler.schedule(FiniteDuration(5000, TimeUnit.MILLISECONDS), FiniteDuration(220, TimeUnit.MILLISECONDS), self, simulatepostPagePicture("C5"))

      //visitPageIntializer = context.system.scheduler.schedule(FiniteDuration(3000, TimeUnit.MILLISECONDS), FiniteDuration(100, TimeUnit.MILLISECONDS), self, simulateVisitPage("C2"))

      //      context.system.scheduler.schedule(FiniteDuration(3000, TimeUnit.MILLISECONDS), FiniteDuration(50000, TimeUnit.MILLISECONDS), self, simulatePostPicture())
    }

    case simulateVisitPage(actorType: String) => {

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(Random.nextInt(numMultiMediaSavyUsers)) ! GetPage(Random.nextInt(numPages))
        case "C2" => lowEngagedUsers(Random.nextInt(numLowEngagedUsers)) ! GetPage(Random.nextInt(numPages))
        case "C3" => textSavyPages(Random.nextInt(numTextSavyPages)) ! GetPage(Random.nextInt(numPages))
        case "C4" => highEngagedUsers(Random.nextInt(numHighEngagedUsers)) ! GetPage(Random.nextInt(numPages))
        case "C5" => multiMediaSpecialistPages(Random.nextInt(numMultiMediaSpecialistPages)) ! GetPage(Random.nextInt(numPages))
      }

    }

    case simulateVisitUserProfile(actorType: String) => {
      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(Random.nextInt(numMultiMediaSavyUsers)) ! GetUser(Random.nextInt(numUsers))
        case "C2" => lowEngagedUsers(Random.nextInt(numLowEngagedUsers)) ! GetUser(Random.nextInt(numUsers))
        case "C4" => highEngagedUsers(Random.nextInt(numHighEngagedUsers)) ! GetUser(Random.nextInt(numUsers))
      }

    }

    case simulateLikePage(actorType: String) => {
      var numMultiMediaSavyUserIndex = Random.nextInt(numMultiMediaSavyUsers)
      var numLowEngagedUserIndex = Random.nextInt(numLowEngagedUsers)
      var numHighEngagedUserIndex = Random.nextInt(numHighEngagedUsers)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(numMultiMediaSavyUserIndex) ! likePage(Random.nextInt(numPages), numMultiMediaSavyUserIndex)
        case "C2" => lowEngagedUsers(numLowEngagedUserIndex) ! likePage(Random.nextInt(numPages), numLowEngagedUserIndex)
        case "C4" => highEngagedUsers(numHighEngagedUserIndex) ! likePage(Random.nextInt(numPages), numHighEngagedUserIndex)
      }

    }

    case simulateUnlikePage(actorType: String) => {
      var numMultiMediaSavyUserIndex = Random.nextInt(numMultiMediaSavyUsers)
      var numLowEngagedUserIndex = Random.nextInt(numLowEngagedUsers)
      var numHighEngagedUserIndex = Random.nextInt(numHighEngagedUsers)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(numMultiMediaSavyUserIndex) ! unlikePage(Random.nextInt(numPages), numMultiMediaSavyUserIndex)
        case "C2" => lowEngagedUsers(numLowEngagedUserIndex) ! unlikePage(Random.nextInt(numPages), numLowEngagedUserIndex)
        case "C4" => highEngagedUsers(numHighEngagedUserIndex) ! unlikePage(Random.nextInt(numPages), numHighEngagedUserIndex)
      }

    }

    case simulatePostPage(actorType: String) => {
      var textSavyPageIndex = Random.nextInt(numTextSavyPages)
      var multiMediaSpecialistPageIndex = Random.nextInt(numMultiMediaSpecialistPages)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C3" => textSavyPages(textSavyPageIndex) ! pagePost(textSavyPageIndex)
        case "C5" => multiMediaSpecialistPages(multiMediaSpecialistPageIndex) ! pagePost(multiMediaSpecialistPageIndex)
      }

    }
    case simulateReadPageFeed(actorType: String) => {
      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(Random.nextInt(numMultiMediaSavyUsers)) ! getPageFeed(Random.nextInt(numPages))
        case "C2" => lowEngagedUsers(Random.nextInt(numLowEngagedUsers)) ! getPageFeed(Random.nextInt(numPages))
        case "C3" => textSavyPages(Random.nextInt(numTextSavyPages)) ! getPageFeed(Random.nextInt(numPages))
        case "C4" => highEngagedUsers(Random.nextInt(numHighEngagedUsers)) ! getPageFeed(Random.nextInt(numPages))
        case "C5" => multiMediaSpecialistPages(Random.nextInt(numMultiMediaSpecialistPages)) ! getPageFeed(Random.nextInt(numPages))
      }
    }
    case simulateDeletePagePost(actorType: String) => {
      var textSavyPageIndex = Random.nextInt(numTextSavyPages)
      var multiMediaSpecialistPageIndex = Random.nextInt(numMultiMediaSpecialistPages)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C3" => textSavyPages(textSavyPageIndex) ! deletePagePost(textSavyPageIndex, Random.nextInt(10) + 100)
        case "C5" => multiMediaSpecialistPages(multiMediaSpecialistPageIndex) ! deletePagePost(multiMediaSpecialistPageIndex, Random.nextInt(10) + 100)
      }

    }

    case simulateFriendRequest(actorType: String) => {
      var numMultiMediaSavyUserIndex = Random.nextInt(numMultiMediaSavyUsers)
      var numLowEngagedUserIndex = Random.nextInt(numLowEngagedUsers)
      var numHighEngagedUserIndex = Random.nextInt(numHighEngagedUsers)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(numMultiMediaSavyUserIndex) ! sendFriendRequest(Random.nextInt(numUsers), numMultiMediaSavyUserIndex)
        case "C2" => lowEngagedUsers(numLowEngagedUserIndex) ! sendFriendRequest(Random.nextInt(numUsers), numLowEngagedUserIndex)
        case "C4" => highEngagedUsers(numHighEngagedUserIndex) ! sendFriendRequest(Random.nextInt(numUsers), numHighEngagedUserIndex)
      }
      //userActors(actorIndex) ! sendFriendRequest(friendIndex, actorIndex)
    }

    case simulateapproveDeclineRequest(actorType: String) => {
      var numMultiMediaSavyUserIndex = Random.nextInt(numMultiMediaSavyUsers)
      var numLowEngagedUserIndex = Random.nextInt(numLowEngagedUsers)
      var numHighEngagedUserIndex = Random.nextInt(numHighEngagedUsers)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(numMultiMediaSavyUserIndex) ! approveDeclineRequest(numMultiMediaSavyUserIndex, Random.nextInt(numUsers), true)
        case "C2" => lowEngagedUsers(numLowEngagedUserIndex) ! approveDeclineRequest(numLowEngagedUserIndex, Random.nextInt(numUsers), true)
        case "C4" => highEngagedUsers(numHighEngagedUserIndex) ! approveDeclineRequest(numHighEngagedUserIndex, Random.nextInt(numUsers), true)
      }
      
    }

    case simulateStatusUpdate(actorType: String) => {
      var numMultiMediaSavyUserIndex = Random.nextInt(numMultiMediaSavyUsers)
      var numLowEngagedUserIndex = Random.nextInt(numLowEngagedUsers)
      var numHighEngagedUserIndex = Random.nextInt(numHighEngagedUsers)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(numMultiMediaSavyUserIndex) ! userPost(numMultiMediaSavyUserIndex, numMultiMediaSavyUserIndex)
        case "C2" => lowEngagedUsers(numLowEngagedUserIndex) ! userPost(numLowEngagedUserIndex, numLowEngagedUserIndex)
        case "C4" => highEngagedUsers(numHighEngagedUserIndex) ! userPost(numHighEngagedUserIndex, numHighEngagedUserIndex)
      }
      
    }

    case simulateReadUserFeed(actorType: String) => {
      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(Random.nextInt(numMultiMediaSavyUsers)) ! getUserFeed(Random.nextInt(numUsers))
        case "C2" => lowEngagedUsers(Random.nextInt(numLowEngagedUsers)) ! getUserFeed(Random.nextInt(numUsers))
        case "C3" => textSavyPages(Random.nextInt(numTextSavyPages)) ! getUserFeed(Random.nextInt(numUsers))
        case "C4" => highEngagedUsers(Random.nextInt(numHighEngagedUsers)) ! getUserFeed(Random.nextInt(numUsers))
        case "C5" => multiMediaSpecialistPages(Random.nextInt(numMultiMediaSpecialistPages)) ! getUserFeed(Random.nextInt(numUsers))
      }
      
    }
    case simulatedeleteUserPost(actorType: String) => {
      var numMultiMediaSavyUserIndex = Random.nextInt(numMultiMediaSavyUsers)
      var numLowEngagedUserIndex = Random.nextInt(numLowEngagedUsers)
      var numHighEngagedUserIndex = Random.nextInt(numHighEngagedUsers)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(numMultiMediaSavyUserIndex) ! deleteUserPost(numMultiMediaSavyUserIndex, numMultiMediaSavyUserIndex, Random.nextInt(10) + 100)
        case "C2" => lowEngagedUsers(numLowEngagedUserIndex) ! deleteUserPost(numLowEngagedUserIndex, numLowEngagedUserIndex, Random.nextInt(10) + 100)
        case "C4" => highEngagedUsers(numHighEngagedUserIndex) ! deleteUserPost(numHighEngagedUserIndex, numHighEngagedUserIndex, Random.nextInt(10) + 100)
      }
      
    }

    case simulategetFriendList(actorType: String) => {
      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(Random.nextInt(numMultiMediaSavyUsers)) ! getFriendList(Random.nextInt(numUsers))
        case "C2" => lowEngagedUsers(Random.nextInt(numLowEngagedUsers)) ! getFriendList(Random.nextInt(numUsers))
        case "C3" => textSavyPages(Random.nextInt(numTextSavyPages)) ! getFriendList(Random.nextInt(numUsers))
        case "C4" => highEngagedUsers(Random.nextInt(numHighEngagedUsers)) ! getFriendList(Random.nextInt(numUsers))
        case "C5" => multiMediaSpecialistPages(Random.nextInt(numMultiMediaSpecialistPages)) ! getFriendList(Random.nextInt(numUsers))
      }
      
    }

    case simulategetFriendRequestList(actorType: String) => {
      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(Random.nextInt(numMultiMediaSavyUsers)) ! getFriendRequestList(Random.nextInt(numUsers))
        case "C2" => lowEngagedUsers(Random.nextInt(numLowEngagedUsers)) ! getFriendRequestList(Random.nextInt(numUsers))
        case "C3" => textSavyPages(Random.nextInt(numTextSavyPages)) ! getFriendRequestList(Random.nextInt(numUsers))
        case "C4" => highEngagedUsers(Random.nextInt(numHighEngagedUsers)) ! getFriendRequestList(Random.nextInt(numUsers))
        case "C5" => multiMediaSpecialistPages(Random.nextInt(numMultiMediaSpecialistPages)) ! getFriendRequestList(Random.nextInt(numUsers))
      }
      
    }

    case simulatepostUserPicture(actorType: String) => {
      var numMultiMediaSavyUserIndex = Random.nextInt(numMultiMediaSavyUsers)
      var numLowEngagedUserIndex = Random.nextInt(numLowEngagedUsers)
      var numHighEngagedUserIndex = Random.nextInt(numHighEngagedUsers)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(numMultiMediaSavyUserIndex) ! postUserPicture(numMultiMediaSavyUserIndex, Random.nextInt(numUsers))
        case "C2" => lowEngagedUsers(numLowEngagedUserIndex) ! postUserPicture(numLowEngagedUserIndex, Random.nextInt(numUsers))
        case "C4" => highEngagedUsers(numHighEngagedUserIndex) ! postUserPicture(numHighEngagedUserIndex, Random.nextInt(numUsers))
      }

    }

    case simulatepostPagePicture(actorType: String) => {
      var textSavyPageIndex = Random.nextInt(numTextSavyPages)
      var multiMediaSpecialistPageIndex = Random.nextInt(numMultiMediaSpecialistPages)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C3" => textSavyPages(textSavyPageIndex) ! postPagePicture(textSavyPageIndex, Random.nextInt(numPages))
        case "C5" => multiMediaSpecialistPages(multiMediaSpecialistPageIndex) ! postPagePicture(multiMediaSpecialistPageIndex, Random.nextInt(numPages))

      }

    }

    case simulategettUserPicture(actorType: String) => {
      var numMultiMediaSavyUserIndex = Random.nextInt(numMultiMediaSavyUsers)
      var numLowEngagedUserIndex = Random.nextInt(numLowEngagedUsers)
      var numHighEngagedUserIndex = Random.nextInt(numHighEngagedUsers)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C1" => multiMediaSavyUsers(numMultiMediaSavyUserIndex) ! postUserPicture(numMultiMediaSavyUserIndex, Random.nextInt(numUsers))
        case "C2" => lowEngagedUsers(numLowEngagedUserIndex) ! postUserPicture(numLowEngagedUserIndex, Random.nextInt(numUsers))
        case "C4" => highEngagedUsers(numHighEngagedUserIndex) ! postUserPicture(numHighEngagedUserIndex, Random.nextInt(numUsers))
      }

    }

    case simulategetPagePicture(actorType: String) => {
      var textSavyPageIndex = Random.nextInt(numTextSavyPages)
      var multiMediaSpecialistPageIndex = Random.nextInt(numMultiMediaSpecialistPages)

      chooseActorType(actorType)

      def chooseActorType(choice: String) = choice match {
        case "C3" => textSavyPages(textSavyPageIndex) ! postPagePicture(textSavyPageIndex, Random.nextInt(numPages))
        case "C5" => multiMediaSpecialistPages(multiMediaSpecialistPageIndex) ! postPagePicture(multiMediaSpecialistPageIndex, Random.nextInt(numPages))

      }

    }

  }

}

class Client(system: ActorSystem) extends Actor {
  import system.dispatcher
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  val pipeline1: HttpRequest => Future[HttpResponse] = sendReceive
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
//      response.foreach(
//        response =>
//          println(s"User Profile :\n${response.entity.asString}"))
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
      val response: Future[HttpResponse] = pipeline1(Post("http://localhost:8080/pagePost?pageId=" + pageId + "&post=" + Random.alphanumeric.take(Random.nextInt(140)).mkString))
    }

    case getPageFeed(pageId: Int) => {
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/page/" + pageId + "/feed"))

//      response.foreach(
//        response =>
//          println(s"Page Feed :\n${response.entity.asString}"))
    }

    case deletePagePost(pageId: Int, postId: Int) => {
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/deletePost?pageId=" + pageId + "&postId=" + postId))
    }

    case userPost(userId: Int, fromUser: Int) => {
      val response: Future[HttpResponse] = pipeline1(Post("http://localhost:8080/userPost?userId=" + userId + "&fromUser=" + fromUser + "&post=" + Random.alphanumeric.take(Random.nextInt(140)).mkString))
    }

    case getUserFeed(userId: Int) => {
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/user/" + userId + "/feed"))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))
      //      response.foreach(
      //        response =>
      //          println(s"User Feed :\n${response.entity.asString}"))
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

    case postUserPicture(userId: Int, pictureId: Int) => {

      val byteArray = Files.readAllBytes(Paths.get("src/abc.jpg"))
      //println(byteArray)
      val base64String = Base64.encodeBase64String(byteArray);
      val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/userAlbum", HttpEntity(MediaTypes.`application/json`, s"""{
        "userId": "$userId",
        "pictureId" : "$pictureId", 
        "Image": "$base64String"
    }""")))

    }

    case getUserPicture(userId: Int, pictureId: Int) => {
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/user/" + userId + "/picture/" + pictureId))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))
      //      response.foreach(
      //        response =>
      //          println(s"picture  :\n${response.entity.asString}"))

    }

    case postPagePicture(pageId: Int, pictureId: Int) => {

      val byteArray = Files.readAllBytes(Paths.get("src/abc.jpg"))
      //println(byteArray)
      val base64String = Base64.encodeBase64String(byteArray);
      val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/pageAlbum", HttpEntity(MediaTypes.`application/json`, s"""{
        "pageId": "$pageId",
        "pictureId" : "$pictureId", 
        "Image": "$base64String"
    }""")))

    }

    case getPagePicture(pageId: Int, pictureId: Int) => {
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/page/" + pageId + "/picture/" + pictureId))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))
      //      response.foreach(
      //        response =>
      //          println(s"picture  :\n${response.entity.asString}"))

    }

  }
}


