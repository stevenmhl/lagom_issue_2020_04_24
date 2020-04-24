package com.example.app.process.impl.command

sealed trait CommandSerializable
trait Command extends CommandSerializable {
  def replyTo: ReplyTo
}

case class Get(id: String, replyTo: ReplyTo) extends Command

case class Add(id: String, name: String, description: Option[String], replyTo: ReplyTo) extends Command

case class Update(id: String, name: String, description: Option[String], replyTo: ReplyTo) extends Command

case class Drop(id: String, replyTo: ReplyTo) extends Command
