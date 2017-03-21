package chat

import akka.actor.{Actor, ActorRef}
import akka.http.scaladsl.model.ws.TextMessage

class ChatRoomActor(roomId: Int) extends Actor {

  var participants: Map[String, ActorRef] = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case UserJoined(name, actorRef) =>
      participants += name -> actorRef
      broadcast(TextMessage(s"User $name joined channel..."))
      println(s"User $name joined channel[$roomId]")

    case UserLeft(name) =>
      println(s"User $name left channel[$roomId]")
      broadcast(TextMessage(s"User $name left channel[$roomId]"))
      participants -= name

    case msg: IncomingMessage =>
      broadcast(TextMessage(msg.message))
  }

  def broadcast(message: TextMessage): Unit = participants.values.foreach(_ ! message)

}
