package com.lightbend.knative.eventing

import java.net.URI
import java.time.ZonedDateTime

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.stream._

import scala.util.{Failure, Success}
import spray.json._
import CloudEventJsonSupport._

import java.util.UUID

object CloudEventsSender extends Directives with SprayJsonSupport{

  implicit val eventDataFormat = jsonFormat2(EventData.apply)

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = Materializer(system)
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher
    // Obtains a reference to the configuration for this application
    val config = system.settings.config

    // These are read from the application.conf file under `resources`
    val message = config.getString("cloudevents.message")
    val sink = config.getString("events.sink")

    println(s"Starting event sender - message: $message, sink: $sink")

    val event = CloudEvent(
      id = "",
      source = Some(URI.create("https://com.lightbend.knative.eventing/CloudEventsSender")),
      `type` = Some("dev.knative.eventing.samples.heartbeat"),
      datacontenttype = Some("application/json"),
      dataschema = Some(URI.create("https://knative.dev/cloudevents/V1")),
      subject = Some("heartbeat"),
      time = None,
      data = None,
      extensions = Some(Map("beat" -> true, "heart" -> "yes")))

    var count = 0
    while (true) {
      event.id = UUID.randomUUID().toString
      event.time = Some(ZonedDateTime.now())
      event.data = Some(EventData(message, count).toJson.compactPrint.getBytes())
       val response = Http().singleRequest(
        HttpRequest(
          method = HttpMethods.POST,
          uri = sink,
          entity = HttpEntity(ContentTypes.`application/json`, event.toJson.compactPrint)
        )
      )
      response
        .onComplete {
          case Success(msg) =>
            val status = msg.status
            if(status.isFailure())
              println(s"Request failed ${status.reason()}")
            else
              println("Published event")
          case Failure(err) => println(s"Exception publishing event $err")
        }
      Thread.sleep(5000)
      count = count + 1
    }
  }
}

case class EventData(message: String, count: Int)