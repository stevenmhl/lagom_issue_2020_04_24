package com.example.app.process.impl

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import akka.Done
import com.datastax.driver.core.BoundStatement
import com.example.app.process.impl.event.Event
import com.lightbend.lagom.scaladsl.persistence.AggregateEventTag
import com.lightbend.lagom.scaladsl.persistence.EventStreamElement
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraReadSide
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.typesafe.scalalogging.LazyLogging

object ProcessReadSide {
  val table = "processes"
  val partition = 0
}

class ProcessReadSide(cassandraSession: CassandraSession, readSideProcessor: CassandraReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[Event] with LazyLogging {
  import ProcessReadSide.table

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[Event] =
    readSideProcessor.builder[Event]("cassandra_read_side_processes_offset")
      .setGlobalPrepare(() => createTable())
      .setPrepare(_ => prepare())
      .setEventHandler[Event](processEvent _)
      .build()

  override def aggregateTags: Set[AggregateEventTag[Event]] =  Set(Event.Tag)

  private def createTable(): Future[Done] = {
    println(s"\n******************\nCreate Table\n******************\n")
    cassandraSession.executeCreateTable(
      s"""
         |CREATE TABLE IF NOT EXISTS $table (
         |  partition int,
         |  id text,
         |  name text,
         |  description text,
         |  PRIMARY KEY ((partition), id)
         |)
         |""".stripMargin
    )
  }

  private def prepare(): Future[Done] = {
    Future.successful {
      println(s"\n******************\nPrepare\n******************\n")
      Done
    }
  }

  private def processEvent(eventStreamElement: EventStreamElement[Event]): Future[Seq[BoundStatement]] = {
    println(s"\n******************\nStream Element: $eventStreamElement\nEvent:${eventStreamElement.event}\n******************\n")
    Future.successful(Seq.empty)
  }

}
