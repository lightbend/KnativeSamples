package com.lightbend.knative.eventing

import java.net.URI

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server._
import akka.http.scaladsl.{Http, HttpConnectionContext}
import akka.stream._

import scala.concurrent._
import scala.util._

class EventProcessor extends CloudEventProcessing{
  override def processEvent(event: CloudEvent): Unit = println(s"New cloud event $event")
}

object CloudEventsReciever extends Directives with SprayJsonSupport{

  def main(args: Array[String]): Unit = {
    // Creates and initializes an Akka Actor System
    implicit val system: ActorSystem = ActorSystem("cloud-events")
    // Creates and initializes a Materializer to be used for the Akka HTTP Server
    implicit val mat: Materializer = Materializer(system)
    // Specifies where any Futures in this code will execute
    implicit val ec: ExecutionContext = system.dispatcher
    // Obtains a logger to be used for the sample
    val log: LoggingAdapter = system.log
    // Obtains a reference to the configuration for this application
    val config = system.settings.config

    // These are read from the application.conf file under `resources`
    val host = config.getString("cloudevents.host")
    val port = config.getString("cloudevents.port").toInt

    println(s"Starting event reciever - host: $host, port: $port")

    val processor = new EventProcessor()

    // Here we create the Http server, and bind it to the host and the port,
    // so we can serve requests using the route(s) we defined previously.
    val binding = Http().bindAndHandleAsync(
      Route.asyncHandler(processor.route()),
      host,
      port,
      connectionContext = HttpConnectionContext()) andThen {
      case Success(sb) =>
        log.info("Bound: {}", sb)
      case Failure(t) =>
        log.error(t, "Failed to bind to {}:{}—shutting down", host, port)
        system.terminate()
    }
  }
}