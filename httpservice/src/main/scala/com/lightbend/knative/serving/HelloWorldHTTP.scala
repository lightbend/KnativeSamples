package com.lightbend.knative.serving

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import ContentTypes._
import akka.http.scaladsl.server.Directives._
import akka.stream._
import akka.http.scaladsl.HttpConnectionContext
import akka.http.scaladsl.server.Route

import scala.concurrent._
import scala.util._

object HelloWorldHTTP {
  def main(args: Array[String]): Unit = {
    // Creates and initializes an Akka Actor System
    implicit val system: ActorSystem = ActorSystem("HelloWorldHTTP")
    // Creates and initializes a Materializer to be used for the Akka HTTP Server
    implicit val mat: Materializer = Materializer(system)
    // Specifies where any Futures in this code will execute
    implicit val ec: ExecutionContext = system.dispatcher
    // Obtains a logger to be used for the sample
    val log: LoggingAdapter = system.log
    // Obtains a reference to the configuration for this application
    val config = system.settings.config

    // These are read from the application.conf file under `resources`
    val message = config.getString("helloworld.message")
    val host = config.getString("helloworld.host")
    val port = config.getString("helloworld.port").toInt

    // Here we define the endpoints exposed by this application
    val serviceRoute =
      path("") {
        get {
          log.info("Received request to HelloWorldHTTP")
          complete(HttpEntity(`text/html(UTF-8)`, message))
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