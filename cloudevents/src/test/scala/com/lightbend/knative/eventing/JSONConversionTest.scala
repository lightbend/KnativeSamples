package com.lightbend.knative.eventing

import java.net.URI
import java.time.ZonedDateTime
import spray.json._

import CloudEventJsonSupport._

object JSONConversionTest{
  def main(args: Array[String]): Unit = {
    val event = CloudEvent("ID", Some(URI.create("http://localhost:8080")), Some("type"), Some("json"), Some(URI.create("http://localhost")),
      Some("subject"), Some(ZonedDateTime.now()), Some("my data".getBytes()), Some(Map("one" -> "one", "two" -> 2, "three" -> false)))

    var json = event.toJson.prettyPrint

    println(json)

    var ev = json.parseJson.convertTo[CloudEvent]
    println(ev.toString)

    json = "{\"id\": \"ID\",\"type\": \"type\"}"
    println(json)

    ev = json.parseJson.convertTo[CloudEvent]
    println(ev.toString)

  }
}
