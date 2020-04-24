package com.example.app.process.message

sealed trait Message {
  def id: String
}

case class Added(
                    id: String,
                    name: String,
                    description: Option[String]
                  ) extends Message

case class Updated(
                     id: String,
                     name: String,
                     description: Option[String]
                   ) extends Message


case class Dropped(
                    id: String
                  ) extends Message

object Json {
  import play.api.libs.json.{Json => PlayJson, _}

  implicit val messageAddedJsonFormat: Format[Added] = PlayJson.format
  implicit val messageUpdatedJsonFormat: Format[Updated] = PlayJson.format
  implicit val messageDroppedJsonFormat: Format[Dropped] = PlayJson.format

  implicit val messageJasonFormat: Format[Message] = Format[Message](
    Reads { js =>
      val msgType = (JsPath \ "type").read[String].reads(js)
      msgType.fold(
        errors => JsError(errors),
        {
          case "added" => (messageAddedJsonFormat reads js)
          case "updated" => messageUpdatedJsonFormat reads js
          case "dropped" => messageDroppedJsonFormat reads js
        }
      )
    },
    Writes {
      case o: Added => (messageAddedJsonFormat writes o).as[JsObject] + ("type", JsString("added"))
      case o: Updated => (messageUpdatedJsonFormat writes o).as[JsObject] + ("type", JsString("updated"))
      case o: Dropped => (messageDroppedJsonFormat writes o).as[JsObject] + ("type", JsString("dropped"))
    }
  )
}