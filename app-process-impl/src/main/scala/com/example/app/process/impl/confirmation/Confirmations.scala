package com.example.app.process.impl.confirmation

import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import com.example.app.process.api.Response

trait Confirmation {
  def toResponse: Response
}

case class Success(id: String, name: String, description: Option[String]) extends Confirmation{
  def toResponse: Response = Response.Success(id, name, description)
}
case class Failure(message: String) extends Confirmation {
  override def toResponse: Response = Response.Failure(message)
}

object Json {

  import play.api.libs.json.{Json => PlayJson, _}

  implicit val successJsonFormat: Format[Success] = PlayJson.format
  implicit val failureJsonFormat: Format[Failure] = PlayJson.format
  implicit val confirmationJsonFormat: Format[Confirmation] = Format[Confirmation](
    Reads { js =>
      val msgType = (JsPath \ "type").read[String].reads(js)
      msgType.fold(
        errors => JsError(errors),
        {
          case "success" => (successJsonFormat reads js)
          case "failure" => failureJsonFormat reads js
        }
      )
    },
    Writes {
      case o: Success => (successJsonFormat writes o).as[JsObject] + ("type", JsString("success"))
      case o: Failure => (failureJsonFormat writes o).as[JsObject] + ("type", JsString("failure"))
    }
  )

  def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[Confirmation],
    JsonSerializer[Success],
    JsonSerializer[Failure]
  )

}
