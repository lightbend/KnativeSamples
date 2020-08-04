package com.lightbend.knative.eventing

// Based on https://github.com/cloudevents/sdk-java/blob/master/core/src/main/java/io/cloudevents/core/v1/CloudEventV1.java
// and https://github.com/cloudevents/sdk-java/blob/master/core/src/main/java/io/cloudevents/core/impl/BaseCloudEvent.java
// Json definition is here https://github.com/cloudevents/spec/blob/master/spec.json

import java.net.URI
import java.time.ZonedDateTime
import java.util.Arrays

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import spray.json._

import scala.util.Try

/*
 *    "id": "description": "Identifies the event.", "examples": ["A234-1234-1234"]
 *    "source": "description": "Identifies the context in which an event happened.","examples" : [
 *       "https://github.com/cloudevents",
 *       "mailto:cncf-wg-serverless@lists.cncf.io",
 *       "urn:uuid:6e8bc430-9c3a-11d9-9669-0800200c9a66",
 *       "cloudevents/spec/pull/123",
 *       "/sensors/tn-1234567/alerts",
 *       "1-555-123-4567"]
 *    "specversion": "description": "The version of the CloudEvents specification which the event uses.","examples": ["1.x-wip"]
 *    "type": "description": "Describes the type of event related to the originating occurrence.","examples" : [
 *        "com.github.pull_request.opened",
 *        "com.example.object.deleted.v2"]
 *    "datacontenttype": "description": "Content type of the data value. Must adhere to RFC 2046 format.", "examples": [
 *        "text/xml",
 *        "application/json",
 *        "image/png",
 *           },
 *    "dataschema": "description": "Identifies the schema that data adheres to.",
 *    "subject": "description": "Describes the subject of the event in the context of the event producer (identified by source).","examples": ["mynewfile.jpg"]
 *    "time": "description": "Timestamp of when the occurrence happened. Must adhere to RFC 3339.", "examples": ["2018-04-05T17:31:00Z"]
 *    "data": "description": "The event payload.", "examples": ["<much wow=\"xml\"/>"]}
 *    "data_base64": "description": "Base64 encoded event payload. Must adhere to RFC4648.","examples": ["Zm9vYg=="]
 *
 *    "required": ["id", "source", "specversion", "type"]
 *
 *    To allow some of the variables to come from the header we make all of the variables here optional
 */

case class CloudEvent(var id: Option[String], var source: Option[URI], var specversion : Option[String], var `type`: Option[String],
                      var datacontenttype: Option[String], var dataschema: Option[URI], var subject: Option[String],
                      var time: Option[ZonedDateTime], var data: Option[String], var data_base64: Option[Array[Byte]],
                      var extensions: Option[Map[String, Any]]){

  def isValid() : Boolean = id.isDefined && source.isDefined && specversion.isDefined && `type`.isDefined

  override def toString: String = {
    val builder = new StringBuilder()
    builder.append("CloudEvent{")
    id match {
      case Some(i) => builder.append(s"id=$i,")
      case _ =>
    }
    source match {
      case Some(s) => builder.append(s" source=${s.toString},")
      case _ =>
    }
    specversion match {
      case Some(s) => builder.append(s" specversion=$s,")
      case _ =>
    }
    `type` match {
      case Some(t) => builder.append(s" type=$t,")
      case _ =>
    }
    datacontenttype match {
      case Some(d) => builder.append(s" datacontenttype = $d,")
      case _ =>
    }
    dataschema match {
      case Some(d) => builder.append(s" dataschema = $d,")
      case _ =>
    }
    subject match {
      case Some(s) => builder.append(s" subject=${s.toString},")
      case _ =>
    }
    time match {
      case Some(t) => builder.append(s" time=$t,")
      case _ =>
    }
    data match {
      case Some(d) => builder.append(s" data=$data,")
      case _ =>
    }
    data_base64 match {
      case Some(d) =>
        datacontenttype match {
          case Some(ct) =>
            if(ct.contains("json") || ct.startsWith("text") || ct.contains("javascript"))
              builder.append(s" data=${new String(d)},")
            else
              builder.append(s" data=${Arrays.toString(d)},")
          case _ => builder.append(s" data=${Arrays.toString(d)},")
        }
      case _ =>
    }
    extensions match {
      case Some(e) => builder.append(s" extensions=$e")
      case _ =>
    }
    builder.append("}")
    builder.toString()
  }
}

trait URIJsonSupport extends DefaultJsonProtocol {
  implicit object URIFormat extends JsonFormat[URI] {
    def write(uri: URI) = JsString(uri.toString)

    def read(json: JsValue): URI = json match {
      case JsString(uri) ⇒ Try(URI.create(uri)).getOrElse(deserializationError(s"Expected valid URI but got '$uri'."))
      case other          ⇒ deserializationError(s"Expected URI as JsString, but got: $other")
    }
  }
}

trait ZonedDateTimeJsonSupport extends DefaultJsonProtocol {
  implicit object ZonedDateTimeFormat extends JsonFormat[ZonedDateTime] {
    def write(time: ZonedDateTime) = JsString(time.toString)

    def read(json: JsValue): ZonedDateTime = json match {
      case JsString(time) ⇒ Try(ZonedDateTime.parse(time)).getOrElse(deserializationError(s"Expected valid ZonedDateTime but got '$time'."))
      case other          ⇒ deserializationError(s"Expected ZonedDateTime as JsString, but got: $other")
    }
  }
}

trait AnyJsonSupport extends DefaultJsonProtocol {
  implicit object AnyJsonFormat extends JsonFormat[Any] {
    def write(x: Any) = x match {
      case n: Int => JsNumber(n)
      case s: String => JsString(s)
      case b: Boolean if b == true => JsTrue
      case b: Boolean if b == false => JsFalse
    }
    def read(value: JsValue) = value match {
      case JsNumber(n) => n.intValue()
      case JsString(s) => s
      case JsTrue => true
      case JsFalse => false
    }
  }
}

object CloudEventJsonSupport extends DefaultJsonProtocol with URIJsonSupport with ZonedDateTimeJsonSupport with AnyJsonSupport {
  implicit val helloRequestFormat = jsonFormat11(CloudEvent.apply)
}

trait CloudEventProcessing extends Directives with SprayJsonSupport {

  import CloudEventJsonSupport._

  def buildHttpRequest(event : CloudEvent, uri : String) : HttpRequest = {
/* Cloud events header (see https://github.com/cloudevents/spec/blob/master/primer.md#creating-cloudevents)
 *  ce-id: <event.id>
 *  ce-source: <event.source>
 *  ce-specversion: <event.specificversion>
 *  ce-type: <event.type>
 */
    val headers : scala.collection.immutable.Seq[HttpHeader] = scala.collection.immutable.Seq(
      RawHeader("ce-id", event.id.getOrElse("")),
      RawHeader("ce-source", event.source.getOrElse("").toString),
      RawHeader("ce-specversion", event.specversion.getOrElse("")),
      RawHeader("ce-type", event.`type`.getOrElse("")))
    HttpRequest(
      method = HttpMethods.POST,
      uri = uri,
      entity = HttpEntity(ContentTypes.`application/json`, event.toJson.compactPrint),
      headers = headers
    )
  }

  def route(eventpath : String = "") : Route =
    path(eventpath) {
      post {
        extractRequest { request =>
          request.headers.foreach(header => {
            println(s"Header ${header.name()} - ${header.value()}")
          })
          entity(as[CloudEvent]) { entity ⇒
            request.headers.foreach(header => {
              header.name() match {
                case name if name == "ce-id" => entity.id = Some(header.value())
                case name if name == "ce-source" => entity.source = Some(URI.create(header.value()))
                case name if name == "ce-specversion" => entity.specversion = Some(header.value())
                case name if name == "ce-type" => entity.`type` = Some(header.value())
                case _ =>
              }
            })
            processEvent(entity)
            complete(StatusCodes.OK)
          }
        }
      }
    }

  def processEvent(event : CloudEvent) : Unit
}