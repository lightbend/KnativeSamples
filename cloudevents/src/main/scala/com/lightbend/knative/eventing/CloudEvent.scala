package com.lightbend.knative.eventing

// Based on https://github.com/cloudevents/sdk-java/blob/master/core/src/main/java/io/cloudevents/core/v1/CloudEventV1.java
// and https://github.com/cloudevents/sdk-java/blob/master/core/src/main/java/io/cloudevents/core/impl/BaseCloudEvent.java

import java.net.URI
import java.time.ZonedDateTime
import java.util.Arrays

import spray.json._

import scala.util.Try

case class CloudEvent(var id: String, var source: Option[URI], var `type`: Option[String], var datacontenttype: Option[String],
                      var dataschema: Option[URI], var subject: Option[String], var time: Option[ZonedDateTime],
                      var data: Option[Array[Byte]], var extensions: Option[Map[String, Any]]){
  override def toString: String = {
    val builder = new StringBuilder()
    builder.append("CloudEvent{").append(s"id=$id,")
    source match {
      case Some(s) => builder.append(s" source=${s.toString},")
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
  implicit val helloRequestFormat = jsonFormat9(CloudEvent.apply)
}
