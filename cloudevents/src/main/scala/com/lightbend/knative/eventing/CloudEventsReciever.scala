package com.lightbend.knative.eventing

import java.net.URI

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.http.scaladsl.{Http, HttpConnectionContext}
import akka.stream._

import scala.concurrent._
import scala.util._
import CloudEventJsonSupport._

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

    // Here we define the endpoints exposed by this application

    val serviceRoute =
      post {
        log.info("Received new event")
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
            log.info(s"Cloud event $entity")
            complete(StatusCodes.OK)
          }
        }
      }

    // Here we create the Http server, and bind it to the host and the port,
    // so we can serve requests using the route(s) we defined previously.
    val binding = Http().bindAndHandleAsync(
      Route.asyncHandler(serviceRoute),
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