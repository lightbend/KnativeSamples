package com.lightbend.knative.eventing

import java.net.URI
import java.time.ZonedDateTime
import spray.json._

import CloudEventJsonSupport._

object JSONConversionTest{
  def main(args: Array[String]): Unit = {
    val event = CloudEvent("ID", URI.create("http://localhost:8080"), "1.0", "type", Some("json"), Some(URI.create("http://localhost")),
      Some("subject"), Some(ZonedDateTime.now()), None, Some("my data".getBytes()), Some(Map("one" -> "one", "two" -> 2, "three" -> false)))

    var json = event.toJson.prettyPrint

    println(json)

    var ev = json.parseJson.convertTo[CloudEvent]
    println(ev.toString)

    json = "{\"id\": \"ID\",\"source\": \"source\",\"specversion\": \"1.0\",\"type\": \"type\"}"
    println(json)

    ev = json.parseJson.convertTo[CloudEvent]
    println(ev.toString)

  }
}
