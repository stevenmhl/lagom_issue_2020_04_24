package com.example.app.process.impl.state

import com.example.app.process.impl.command.Command
import com.example.app.process.impl.command.Command
import com.example.app.process.impl.event.Event

trait State {
  def applyCommand(cmd: Command): ReplyEffect
  def applyEvent(event: Event): State
}
