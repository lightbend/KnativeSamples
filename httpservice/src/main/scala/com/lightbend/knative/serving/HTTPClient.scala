package com.lightbend.knative.serving

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream._
import scala.concurrent.duration._

import scala.concurrent.Future

object HTTPClient {

//  val url = "http://localhost:8080"
  val url = "http://httpservice.default.35.225.36.19.xip.io"

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = Materializer(system)
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val response: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url))
    response.flatMap(_.entity.toStrict(2 seconds)).map(_.data.utf8String).foreach(println)
  }
}