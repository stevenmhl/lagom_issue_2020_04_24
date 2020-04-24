package com.example.app.process.impl

import akka.actor.typed.ActorSystem
import com.example.app.process._
import akka.{Done, NotUsed}
import akka.stream.scaladsl._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.EntityRef
import akka.persistence.query.Offset
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry, ReadSide}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import akka.util.Timeout
import com.example.app.process.ProcessService
import com.example.app.process.api.{Request, Response}
import com.example.app.process.impl.command.{Add, Drop, Get, Update}
import com.example.app.process.message.{Added, Dropped, Message, Updated}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker._
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import com.typesafe.scalalogging.LazyLogging

/**
  * Implementation of the Mfg Process Service.
  */
class ProcessServiceImpl(
  clusterSharding: ClusterSharding,
  persistentEntityRegistry: PersistentEntityRegistry,
  processService: ProcessService,
  cassandraSession: CassandraSession,
  readSideProcessor: CassandraReadSide,
  readSide: ReadSide
)(implicit ec: ExecutionContext, actorSystem: ActorSystem[_])
  extends ProcessService with LazyLogging {

  import command.Command
  import confirmation.Confirmation
  import api._
  import message._

  readSide.register(new ProcessReadSide(cassandraSession, readSideProcessor))

  /**
    * Looks up the entity for the given ID.
    */
  private def entityRef(id: String): EntityRef[Command] =
    clusterSharding.entityRefFor(ProcessAggregate.typeKey, id)

  implicit val timeout = Timeout(5.seconds)

  override def addProcess: ServiceCall[Request.Process, Response] = ServiceCall { process =>

      val id: String = java.util.UUID.randomUUID.toString

        entityRef(id)
          .ask[Confirmation] { replyTo =>
            Add(id, process.name.get, process.description, replyTo)
          } map { _.toResponse }
  }

  override def getProcess(id: String): ServiceCall[NotUsed, Response] = { _ =>
       entityRef(id)
        .ask[Confirmation] { replyTo =>
          Get(id, replyTo)
        } map { _.toResponse }
  }

  override def updateProcess(id: String): ServiceCall[Request.Process, Response] = { process =>

        entityRef(id)
          .ask[Confirmation] { replyTo =>
            Update(id, process.name.get, process.description, replyTo)
          } map { _.toResponse }

  }

  override def deleteProcess(id: String): ServiceCall[NotUsed, String] = { _ =>
      entityRef(id)
        .ask[Confirmation] { replyTo =>
          Drop(id, replyTo)
        }
        .map {
          case _: confirmation.Success => "Process deleted."

          case confirmation.Failure(message) => message
        }
  }


  override def processTopic: Topic[Message] =
    TopicProducer
      .singleStreamWithOffset { offset =>
        persistentEntityRegistry
          .eventStream(event.Event.Tag, offset)
          .mapConcat(handleElement)
      }

  private def handleElement(element: EventStreamElement[event.Event]): Seq[(Message, Offset)] = element.event match {
    case e: event.Added => collection.immutable.Seq[(Message, Offset)]((Added(e.id, e.name, e.description), element.offset))
    case e: event.Updated => collection.immutable.Seq[(Message, Offset)]((Updated(e.id, e.name, e.description), element.offset))
    case e: event.Dropped => collection.immutable.Seq[(Message, Offset)]((Dropped(e.id), element.offset))
    case _ => collection.immutable.Seq.empty[(Message, Offset)]
  }

  processService
    .processTopic
    .subscribe
    .atLeastOnce {
      Flow.fromFunction { message =>
        println(s"\n****************\nTopic Subscriber: $message\n****************\n ")
        Done
      }
    }

}
