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

case class RegisterUser(id: Int)

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
    userActors(0)!RegisterUser(0)
    userActors(1)!RegisterUser(1)
    userActors(2)!RegisterUser(2)
    userActors(3)!RegisterUser(3)
    userActors(4)!RegisterUser(4)
    userActors(5)!RegisterUser(5)
    userActors(6)!RegisterUser(6)
    

  }

}

class Client(system: ActorSystem) extends Actor {

  def receive = {
    case RegisterUser(id: Int) => {
      import system.dispatcher
      val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
      val gender = Array("male","female")
      val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId="+id+"&name="+Random.alphanumeric.take(Random.nextInt(140)).mkString+"&gender="+Random.shuffle(gender.toList).head))
      //val response: Future[HttpResponse] = pipeline(Post("http://localhost:8080/registerUser?userId=0&name=nikhil&gender=male"))
    }
  }
}


