package com.example.app.process.impl

import com.example.app.process._
import akka.cluster.sharding.typed.scaladsl.Entity
import com.example.app.process.ProcessService
import com.lightbend.lagom.scaladsl.akka.discovery.AkkaDiscoveryComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.softwaremill.macwire._

class ProcessLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ProcessApplication(context) with AkkaDiscoveryComponents

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ProcessApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[ProcessService])
}

abstract class ProcessApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  import akka.actor.typed.scaladsl.adapter._
  implicit lazy val typedActorSystem = actorSystem.toTyped

  // Bind the service that this server provides
  override lazy val lagomServer: LagomServer = serverFor[ProcessService](wire[ProcessServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry: JsonSerializerRegistry = ProcessAggregate.Registry

  // Initialize the sharding of the Aggregate. The following starts the aggregate Behavior under
  // a given sharding entity typeKey.
  clusterSharding.init(
    Entity(ProcessAggregate.typeKey)(
      entityContext => ProcessAggregate create entityContext
    )
  )

  lazy val processService: ProcessService = serviceClient.implement[ProcessService]
}
