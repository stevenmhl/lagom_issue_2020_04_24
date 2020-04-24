package com.example.app.process.impl.state

import akka.persistence.typed.scaladsl.Effect
import com.example.app.process.impl.command.{Add, Command}
import com.example.app.process.impl._
import com.example.app.process.impl.command.Command
import com.example.app.process.impl.event.Event

case object NewProcess extends State {

  override def applyCommand(cmd: Command): ReplyEffect =
    cmd match {
      case cmd: Add =>
        import cmd._
        Effect
          .persist(event.Added(id, name, description))
          .thenReply(replyTo) {
            case state: state.Process => confirmation.Success(state.id, state.name, state.description)
          }

      case cmd: Command =>
        Effect
          .reply(cmd.replyTo)(confirmation.Failure(s"Invalid command $cmd on non-existent Process entity."))
    }

  override def applyEvent(e: Event): State = e match {
    case ev: event.Added =>
      import ev._
      Process(id, name, description, ev.timestamp)

    case ev: Event =>
      throw new Exception(s"Event $ev not supported for entity $this.")
  }

}
