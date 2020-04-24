package com.example.app.process.impl.state

import akka.persistence.typed.scaladsl.Effect
import com.example.app.process.impl.command.Command
import com.example.app.process.impl._
import com.example.app.process.impl.command.Command
import com.example.app.process.impl.event.Event

case class DroppedProcess(id: String, timestamp: Long) extends State {

  override def applyCommand(cmd: Command): ReplyEffect =
    Effect
      .reply(cmd.replyTo)(confirmation.Failure("Invalid command on dropped entity."))

  override def applyEvent(event: Event): State =
    throw new Exception(s"Event $event not supported for dropped entity $this.")
}
