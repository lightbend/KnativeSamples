package com.lightbend.knative.eventing

// Based on https://github.com/cloudevents/sdk-java/blob/master/core/src/main/java/io/cloudevents/core/v1/CloudEventV1.java
// and https://github.com/cloudevents/sdk-java/blob/master/core/src/main/java/io/cloudevents/core/impl/BaseCloudEvent.java
// Json definition is here https://github.com/cloudevents/spec/blob/master/spec.json

import java.net.URI
import java.time.ZonedDateTime
import java.util.Arrays

import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._

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

case class CloudEvent(var id: String, var source: URI, var specversion : String, var `type`: String,
                      var datacontenttype: Option[String], var dataschema: Option[URI], var subject: Option[String],
                      var time: Option[ZonedDateTime], var data: Option[String], var data_base64: Option[Array[Byte]],
                      var extensions: Option[Map[String, Any]]) {


  override def toString: String = {
    val builder = new StringBuilder()
    builder.append("CloudEvent{").append(s"id=$id,").append(s" source=${source.toString},").append(s" specversion=$specversion,").append(s" type=${`type`},")
    datacontenttype match {
      case Some(d) => builder.append(s" datacontenttype = $d,")
      case _ =>
    }
    dataschema match {
      case Some(d) => builder.append(s" dataschema = ${d.toString},")
      case _ =>
    }
    subject match {
      case Some(s) => builder.append(s" subject=$s,")
      case _ =>
    }
    time match {
      case Some(t) => builder.append(s" time=$t,")
      case _ =>
    }
    data match {
      case Some(d) => builder.append(s" data=$d,")
      case _ =>
    }
    data_base64 match {
      case Some(d) => builder.append(s" data=$d,")
      case _ =>
    }
    extensions match {
      case Some(e) => builder.append(s" extensions=$e")
      case _ =>
    }
    builder.append("}")
    builder.toString()
  }

  def toHttpRequest(uri: String): HttpRequest = {
    var headers: scala.collection.immutable.Seq[HttpHeader] = scala.collection.immutable.Seq(
      // Mandatory fields
      RawHeader("ce-id", id),
      RawHeader("ce-source", source.toString),
      RawHeader("ce-specversion", specversion),
      RawHeader("ce-type", `type`))
    // OPtional fields
    datacontenttype match {
      case Some(c) => headers = headers :+ RawHeader("ce-datacontenttype", c)
      case _ => headers = headers :+ RawHeader("ce-datacontenttype", "application/json")
    }

    dataschema match {
      case Some(d) => headers = headers :+ RawHeader("ce-dataschema", d.toString)
      case _ =>
    }
    subject match {
      case Some(s) => headers = headers :+ RawHeader("ce-subject", s)
      case _ =>
    }
    time match {
      case Some(t) => headers = headers :+ RawHeader("ce-time", t.toString)
      case _ =>
    }
    extensions match {
      case Some(e) =>
        for ((key, value) <- e)
          headers = headers :+ RawHeader(s"ce-${key}extension", value.toString)
      case _ =>
    }

    // Entity
    val entity = datacontenttype match {
      case Some(dtype) =>
        if (dtype.contains("json") || dtype.contains("javascript") || dtype.contains("text")) {
          HttpEntity(ContentTypes.`application/json`, data.getOrElse(""))
        } else {
          HttpEntity(ContentTypes.`application/octet-stream`, data_base64.getOrElse(Array[Byte]())
          )
        }
      case _ => HttpEntity(ContentTypes.`application/json`, "")
    }
    HttpRequest(
      method = HttpMethods.POST,
      uri = uri,
      entity = entity,
      headers = headers
    )

  }
}

trait CloudEventProcessing extends Directives {

  def route(eventpath : String = "") : Route =
    path(eventpath) {
      post {
        extractRequest { request =>
          request.headers.foreach(header => {
            println(s"Header ${header.name()} - ${header.value()}")
          })
          entity(as[Array[Byte]]) { entity ⇒
            val event = CloudEvent("", null, "", "", None, None, None, None, None, None, None)
            var extensions : Map[String, Any] = Map()
            request.headers.foreach { header => {
              header.name() match {
                // Attributes
                case name if name == "ce-id" => event.id = header.value()
                case name if name == "ce-source" => event.source = URI.create(header.value())
                case name if name == "ce-specversion" => event.specversion = header.value()
                case name if name == "ce-type" => event.`type` = header.value()
                case name if name == "ce-dataschema" => event.dataschema = Some(URI.create(header.value()))
                case name if name == "ce-subject" => event.subject = Some(header.value())
                case name if name == "ce-time" => event.time = Some(ZonedDateTime.parse(header.value()))
                // extensions
                case name if name.startsWith("ce-") && (name.contains("extension")) =>
                  val nend = name.indexOf("extension")
                  val exname = name.substring(3, nend)
                  extensions = extensions.+(exname -> header.value())
                // Data
                case name if name == "ce-datacontenttype" =>
                  if (header.value().contains("json") || header.value().contains("javascript") || header.value().contains("text"))
                    event.data = Some(new String(entity))
                  else
                    event.data_base64 = Some(entity)
                case _ =>
              }
            }
              if (extensions.size > 0)
                event.extensions = Some(extensions)
                if(event.datacontenttype == None)    // We did not get content type, default it to JSON
                  event.data = Some(new String(entity))
            }
            processEvent(event)
            complete(StatusCodes.OK)
          }
        }
      }
    }

  def processEvent(event : CloudEvent) : Unit
}