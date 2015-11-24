
import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http


object Main extends App {
    implicit val system = ActorSystem("FacebookSystem")
    val service = system.actorOf(Props[ServerActor],"facebook-server")   
     

    IO(Http) ! Http.Bind(service, interface = "127.0.0.1", port = 8080)
       
}




