package com.example.app.process.impl.state

import akka.persistence.typed.scaladsl.Effect
import com.example.app.process.impl.command.{Add, Command, Drop, Get, Update}
import com.example.app.process.impl._
import com.example.app.process.impl.command.Command
import com.example.app.process.impl.event.Event

case class Process(
                    id: String,
                    name: String,
                    description: Option[String],
                    timestamp: Long
                  ) extends State {

  override def applyCommand(cmd: Command): ReplyEffect =
    cmd match {

      case cmd: Add =>
        Effect
          .reply(cmd.replyTo)(confirmation.Failure("Entity ID collision.  Please retry add operation."))

      case cmd: Get =>
        assert(id == cmd.id)
        Effect
          .reply(cmd.replyTo)(confirmation.Success(id, name, description))

      case cmd: Update =>
        assert(id == cmd.id)
        Effect
          .persist(event.Updated(id, cmd.name, cmd.description))
          .thenReply(cmd.replyTo) {
            case state: state.Process => confirmation.Success(state.id, state.name, state.description)
          }

      case cmd: Drop =>
        assert(id == cmd.id)
        Effect
          .persist(event.Dropped(cmd.id))
          .thenReply(cmd.replyTo) {
            case state: state.Process => confirmation.Success(state.id, state.name, state.description)
          }

    }

  override def applyEvent(e: Event): State =
    e match {
      case ev: event.Updated =>
        Process(ev.id, ev.name, ev.description, ev.timestamp)

      case ev: event.Dropped =>
        DroppedProcess(ev.id, ev.timestamp)

    }
}
