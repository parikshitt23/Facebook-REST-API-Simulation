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

 
 
object Demo extends App{;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(387); 
  
      val context = ActorSystem("workerSystem");System.out.println("""context  : akka.actor.ActorSystem = """ + $show(context ));$skip(161); 
  val worker = context.actorOf(
      Props[Worker].withRouter(RoundRobinRouter(nrOfInstances = 3*Runtime.getRuntime().availableProcessors())), name = "worker");System.out.println("""worker  : akka.actor.ActorRef = """ + $show(worker ));$skip(28); val res$0 = 
      
      worker ! xyz();System.out.println("""res0: <error> = """ + $show(res$0))}
}

class Worker extends Actor{

def receive = {

case xyz() => {
println("Pjfsnifns")
}
}

}
