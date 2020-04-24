package com.example.app.process.impl.state

import com.lightbend.lagom.scaladsl.playjson.JsonSerializer

object Json {

  import play.api.libs.json.{Json => PlayJson, _}
  
  implicit val stateProcessJsonFormat: Format[Process] = PlayJson.format
  implicit val stateDroppedProcessJsonFormat: Format[DroppedProcess] = PlayJson.format

  implicit val stateJsonFormat: Format[State] = new Format[State] {
    override def writes(o: State): JsValue = o match {
      case NewProcess => JsString("New Process")
      case o: Process => stateProcessJsonFormat writes o
      case o: DroppedProcess => stateDroppedProcessJsonFormat writes o
    }
    override def reads(json: JsValue): JsResult[State] = json match {
      case JsString("New Process") => JsSuccess(NewProcess)
      case obj: JsObject =>
        if (obj.keys contains "name")
          stateProcessJsonFormat reads json
        else
          stateDroppedProcessJsonFormat reads json
      case _ => stateProcessJsonFormat reads json
    }
  }

  def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[Process],
    JsonSerializer[DroppedProcess],
    JsonSerializer[State]
  )

}
