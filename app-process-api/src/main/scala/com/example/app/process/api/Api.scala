package com.example.app.process.api

import play.api.libs.json._

object Request {
  case class Process(name: Option[String], description: Option[String])
  implicit val processRequestFormat: Format[Process] = Json.format[Process]

}

trait Response
object Response {
  case class Success(id: String, name: String, description: Option[String]) extends Response
  case class Failure(message: String) extends Response


  implicit val successResponseFormat: Format[Success] = Json.format[Success]
  implicit val failureResponseFormat: Format[Failure] = Json.format[Failure]
  implicit val responseFormat: Format[Response] = Format[Response] (
    Reads { js =>
      val msgType = (JsPath \ "type").read[String].reads(js)
      msgType.fold(
        errors => JsError(errors),
        {
          case "success" => (successResponseFormat reads js)
          case "failure" => failureResponseFormat reads js
        }
      )
    },
    Writes {
      case o: Success => (successResponseFormat writes o).as[JsObject] + ("type", JsString("success"))
      case o: Failure => (failureResponseFormat writes o).as[JsObject] + ("type", JsString("failure"))
    }
  )

//  object Process extends JsonResponse[Process]
//  object Processes extends JsonResponse[Seq[Process]]
//  object String extends JsonResponse[String]
}
