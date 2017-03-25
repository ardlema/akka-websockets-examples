package org.ardlema.chat

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Sink, Source }
import org.ardlema.chat.io.scalac.akka.http.websockets.WSClient
import org.ardlema.publisher.{ RouterActor, VMActor, VMStatsPublisher }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.StdIn

object WSServer extends App {

  implicit val system = ActorSystem("myexample")
  implicit val materializer = ActorMaterializer()

  val interface = "localhost"
  val port = 8080
  val router: ActorRef = system.actorOf(Props[RouterActor], "router")
  val vmactor: ActorRef = system.actorOf(Props(classOf[VMActor], router, 2 seconds, 5 seconds))

  val route: Route = get {
    pathEndOrSingleSlash {
      complete("Welcome to websocket server")
    }
  } ~
    path("ws-echo") {
      get {
        handleWebSocketMessages(echoService)
      }
    } ~
    pathPrefix("ws-org.ardlema.chat" / IntNumber) { chatId ⇒
      parameter('name) { userName ⇒
        handleWebSocketMessages(ChatRooms.findOrCreate(chatId).websocketFlow(userName))
      }
    } ~
    pathPrefix("stats") {
      handleWebSocketMessages(graphFlowWithStats(router))
    }

  val echoService: Flow[Message, Message, _] = Flow[Message].map {
    case TextMessage.Strict(text) ⇒ TextMessage("Echo: " + text)
    case _                        ⇒ TextMessage("Message type unsupported")

  }

  val binding = Http().bindAndHandle(route, interface, port)
  println(s"Server is now online at http://$interface:$port\nPress RETURN to stop...")

  alternativelyRunTheClient()

  StdIn.readLine()

  binding.flatMap(_.unbind()).onComplete(_ ⇒ system.terminate())
  println("Server is down...")

  private def alternativelyRunTheClient(): Unit = {

    if (args.headOption.map(_.equalsIgnoreCase("with-client")).getOrElse(false)) {
      val c: WSClient = WSClient("http://localhost:8080/ws-org.ardlema.chat/123?name=HAL1000", "HAL1000")

      if (c.connectBlocking())
        c.spam("hello message")
    }
  }

  // setup the actors for the stats
  // router: will keep a list of connected actorpublisher, to inform them about new stats.
  // vmactor: will start sending messages to the router, which will pass them on to any
  // connected routee

  /**
   * Creates a flow which uses the provided source as additional input. This complete scenario
   * works like this:
   *  1. When the actor is created it registers itself with a router.
   *  2. the VMActor sends messages at an interval to the router.
   *  3. The router next sends the message to this source which injects it into the flow
   */
  private def graphFlowWithStats(router: ActorRef): Flow[Any, Message, _] = {

    val source = Source.actorPublisher[String](Props(classOf[VMStatsPublisher], router))
      .map[Message](x ⇒ TextMessage.Strict(x))

    Flow.fromSinkAndSource(Sink.ignore, source)
  }
}
