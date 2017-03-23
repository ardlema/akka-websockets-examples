package chat

import akka.NotUsed
import akka.actor.{ ActorSystem, Props }
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{ Flow, Sink, Source }

class ChatRoom(roomId: Int, actorSystem: ActorSystem) {

  private[this] val chatRoomActor = actorSystem.actorOf(Props(classOf[ChatRoomActor], roomId))

  def chatInSink(sender: String) = Sink.actorRef[ChatEvent](chatRoomActor, Left(sender))

  def websocketFlow(user: String): Flow[Message, Message, _] = {
    val in: Sink[Message, NotUsed] =
      Flow[Message]
        .map { case TextMessage.Strict(txt) ⇒ IncomingMessage(user, txt) }
        .to(chatInSink(user))

    // The counter-part which is a source that will create a target ActorRef per
    // materialization where the chatActor will send its messages to.
    // This source will only buffer one element and will fail if the client doesn't read
    // messages fast enough.
    val out: Source[Message, Unit] =
      Source.actorRef[Message](1, OverflowStrategy.fail)
        .mapMaterializedValue(chatRoomActor ! UserJoined(user, _))

    Flow.fromSinkAndSource(in, out)
  }

  def sendMessage(message: ChatMessage): Unit = chatRoomActor ! TextMessage(message.text)
}

object ChatRoom {
  def apply(roomId: Int)(implicit actorSystem: ActorSystem) = new ChatRoom(roomId, actorSystem)
}