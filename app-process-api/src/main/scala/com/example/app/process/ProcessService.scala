package com.example.app.process

import akka.NotUsed
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.transport.Method
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}

object ProcessService {
  val TOPIC_NAME: String = "mfg_process"
}

trait ProcessService extends Service {

  import api._
  import message.Json._

  def addProcess: ServiceCall[Request.Process, Response]
  def getProcess(id: String): ServiceCall[NotUsed, Response]
  def updateProcess(id: String): ServiceCall[Request.Process, Response]
  def deleteProcess(id: String): ServiceCall[NotUsed, String]


  override final def descriptor: Descriptor = {
    import Service._

    named("mgf_process")
      .withCalls(
        restCall(Method.GET, "/api/process/:id", getProcess _),
        restCall(Method.POST, "/api/process/:id", updateProcess _),
        restCall(Method.DELETE, "/api/process/:id", deleteProcess _),
        restCall(Method.PUT, "/api/process", addProcess)
      )
      .withTopics(
        topic(ProcessService.TOPIC_NAME, processTopic)
      )
      .withAutoAcl(true)
  }

  def processTopic: Topic[message.Message]
}
