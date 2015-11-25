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
//import scala.concurrent.ExecutionContext.Implicits.global

import spray.httpx._

case class RegisterUser(userId: Int)
case class GetUser(userId: Int)
case class RegisterPage(pageId: Int)
case class GetPage(pageId: Int)
case class likePage(pageId: Int,userId: Int)
case class pagePost(pageId: Int)

object FacebookClient extends App {

  override def main(args: Array[String]) {
    val userActors = new ArrayBuffer[ActorRef]()
    val pageActors = new ArrayBuffer[ActorRef]()
    val system = ActorSystem("FacebookClientSystem")
//    val client = system.actorOf(Props(new Client(system)), name = "Client")
//    client ! RegisterUser(1)

    for (i <- 0 until 10) {
      userActors += system.actorOf(Props(new Client(system)), name = "User" + i)
      pageActors += system.actorOf(Props(new Client(system)), name = "Page" + i)
    }
    
    for(i<- 0 until 10){
      userActors(i)!RegisterUser(i)
      pageActors(i)!RegisterPage(i)
    }
    for(i<- 0 until 10)
    userActors(1)!GetPage(i)
 
    userActors(1) ! likePage(0,1)
    
    userActors(2) ! pagePost(0)
  }

}

class Client(system: ActorSystem) extends Actor {
  import system.dispatcher
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
  def receive = {
    case RegisterUser(userId: Int) => {
      
      val gender = Array("male","female")
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId="+userId+"&name="+Random.alphanumeric.take(Random.nextInt(50)).mkString+"&gender="+Random.shuffle(gender.toList).head))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))
    }
    case RegisterPage(pageId: Int) => {
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerPage?pageId="+pageId+"&pageName="+Random.alphanumeric.take(Random.nextInt(50)).mkString))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))
      
    }
    case GetUser(userId: Int) => {
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/user/"+userId))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))
      response.foreach(
        response=>
         println(s"User Profile :\n${response.entity.asString}")                                              
      )
    }
    
    case GetPage(pageId: Int) => {
      val response: Future[HttpResponse] = pipeline(Get("http://localhost:8080/page/"+pageId))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))
      response.foreach(
        response=>
         println(s"Page Info :\n${response.entity.asString}")                                              
      )
    }
    
    case likePage(pageId: Int,userId: Int) =>{
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/likePage?pageId="+pageId+"&userId="+userId))
    }
    
    case pagePost(pageId: Int) =>{
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/pagePost?pageId="+pageId+"&post="+Random.alphanumeric.take(Random.nextInt(140)).mkString))
    }
    
  }
}


