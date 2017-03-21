import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

object WSServer extends App {

  implicit val system = ActorSystem("myexample")
  implicit val materializer = ActorMaterializer()

  val interface = "localhost"
  val port = 8080


  val route: Route = get {
    pathEndOrSingleSlash {
      complete("Welcome to websocket server")
    }
  }

  val binding = Http().bindAndHandle(route, interface, port)
  println(s"Server is now online at http://$interface:$port\nPress RETURN to stop...")
  StdIn.readLine()


  binding.flatMap(_.unbind()).onComplete(_ => system.terminate())
  println("Server is down...")

}
