package com.lightbend.knative.serving

import akka.actor.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl._
import akka.stream._

import scala.concurrent._

import knative.grpc.examples.helloworld._


object GreeterServer {

  def main(args: Array[String]): Unit = {
    val system: ActorSystem = ActorSystem("GreeterServer")

    new GreeterServer(system).run()
  }
}

class GreeterServer(system: ActorSystem) {

  def run(): Future[Http.ServerBinding] = {
    implicit val sys: ActorSystem = system
    implicit val mat: Materializer = Materializer(sys)
    implicit val ec: ExecutionContext = sys.dispatcher

    val config = system.settings.config
    val message = config.getString("helloworld.message")
    val port = config.getString("helloworld.port").toInt

    val service: HttpRequest => Future[HttpResponse] =
      GreeterHandler(new GreeterServiceImpl(system.log, message))

    val bound = Http().bindAndHandleAsync(
      service,
      interface = "0.0.0.0",
      port = port,
      connectionContext = HttpConnectionContext())

    bound.foreach { binding =>
      sys.log.info("gRPC server bound to: {}", binding.localAddress)
    }

    bound
  }
}