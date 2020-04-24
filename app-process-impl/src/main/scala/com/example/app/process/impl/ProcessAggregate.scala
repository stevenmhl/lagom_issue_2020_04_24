package com.example.app.process.impl

import akka.cluster.sharding.typed.scaladsl._
import akka.persistence.typed._
import akka.persistence.typed.scaladsl._
import com.lightbend.lagom.scaladsl.persistence._
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}

import scala.collection.immutable.Seq

object ProcessAggregate {

  import command.Command
  import state.State
  import event.Event

  val typeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("Mfg_Process")

  def create(persistenceId: PersistenceId): EventSourcedBehavior[Command, Event, State] =
    EventSourcedBehavior
      .withEnforcedReplies[Command, Event, State](
        persistenceId = persistenceId,
        emptyState = state.NewProcess,
        commandHandler = (process, command) => process.applyCommand(command),
        eventHandler = (process, event) => process.applyEvent(event)
      )

  def create(entityContext: EntityContext[Command]): EventSourcedBehavior[Command, Event, State] =
    create(PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))
      .withTagger(AkkaTaggerAdapter.fromLagom[Command, Event](entityContext, Event.Tag))
      .withRetention(RetentionCriteria.snapshotEvery(numberOfEvents = 25, keepNSnapshots = 2))

  object Registry extends JsonSerializerRegistry {

    override def serializers: Seq[JsonSerializer[_]] =
      event.Json.serializers ++
      confirmation.Json.serializers ++
      state.Json.serializers
  }
}


