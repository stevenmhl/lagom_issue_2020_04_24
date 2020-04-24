package com.example.app.process.impl.event

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, AggregateEventTagger}
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer

sealed trait Event extends AggregateEvent[Event] {
  override def aggregateTag: AggregateEventTagger[Event] = Event.Tag
}

object Event {
  val Tag: AggregateEventTag[Event] = AggregateEventTag[Event]("process_event")
}

case class Added(id: String, name: String, description: Option[String], timestamp: Long = System.currentTimeMillis()) extends Event

case class Updated(id: String, name: String, description: Option[String], timestamp: Long = System.currentTimeMillis()) extends Event

case class Dropped(id: String, timestamp: Long = System.currentTimeMillis()) extends Event

object Json {

  import play.api.libs.json.{Json => PlayJson, _}

  implicit val eventAddedJsonFormat: Format[Added] = PlayJson.format
  implicit val eventUpdatedJsonFormat: Format[Updated] = PlayJson.format
  implicit val eventDroppedJsonFormat: Format[Dropped] = PlayJson.format

  implicit val eventJsonFormat: Format[Event] = Format[Event](
    Reads { js =>
      (JsPath \ "type").read[String].reads(js) fold (
        errors => JsError(errors),
        {
          case "added" => eventAddedJsonFormat reads js
          case "updated" => eventUpdatedJsonFormat reads js
          case "dropped" => eventDroppedJsonFormat reads js
        }
      )
    },
    Writes {
      case o: Added => (eventAddedJsonFormat writes o).as[JsObject] + ("type", JsString("added"))
      case o: Updated => (eventUpdatedJsonFormat writes o).as[JsObject] + ("type", JsString("updated"))
      case o: Dropped => (eventDroppedJsonFormat writes o).as[JsObject] + ("type", JsString("dropped"))
    }
  )

  def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[Added],
    JsonSerializer[Updated],
    JsonSerializer[Dropped],
    JsonSerializer[Event]
  )
}
