package com.lightbend.knative.eventing

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.server._
import akka.stream._
import com.lightbend.knative.eventing.HelloResponseJsonSupport._

import scala.concurrent._
import scala.util._

object SimpleEventsReciever extends Directives with SprayJsonSupport{
  def main(args: Array[String]): Unit = {
    // Creates and initializes an Akka Actor System
    implicit val system: ActorSystem = ActorSystem("simple-events")
    // Creates and initializes a Materializer to be used for the Akka HTTP Server
    implicit val mat: Materializer = Materializer(system)
    // Specifies where any Futures in this code will execute
    implicit val ec: ExecutionContext = system.dispatcher
    // Obtains a logger to be used for the sample
    val log: LoggingAdapter = system.log
    // Obtains a reference to the configuration for this application
    val config = system.settings.config

    // These are read from the application.conf file under `resources`
    val host = config.getString("helloworld.host")
    val port = config.getString("helloworld.port").toInt

    // Here we define the endpoints exposed by this application

    val serviceRoute =
      post {
        log.info("Received new event")
        entity(as[HelloResponse]) { request ⇒
          println(s"Event is  $request")
          complete(StatusCodes.OK)
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

  def postdata(sink: String, data: String)(implicit system: ActorSystem, executionContext: ExecutionContext): Unit = {

    val response = Http().singleRequest(
      HttpRequest(
        method = HttpMethods.POST,
        uri = sink,
        entity = HttpEntity(ContentTypes.`application/json`, data)
      )
    )
    response
      .onComplete {
        case Success(msg) =>
          val status = msg.status
          if(status.isFailure())
            println(s"Request failed ${status.reason()}")
        case Failure(_) => println("something wrong")
      }
  }
}