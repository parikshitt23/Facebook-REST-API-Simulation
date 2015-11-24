import java.security.MessageDigest
    
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

import akka.actor._
import akka.actor.Actor
import akka.actor.ActorDSL._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.routing.RoundRobinRouter

 case class xyz()
 
object Demo extends App{
  
      val context = ActorSystem("workerSystem")
  val worker = context.actorOf(
      Props[Worker].withRouter(RoundRobinRouter(nrOfInstances = 3*Runtime.getRuntime().availableProcessors())), name = "worker")
      
      worker ! xyz()
}

class Worker extends Actor{

def receive = {

case xyz() => {
println("Pjfsnifns")
}
}

}