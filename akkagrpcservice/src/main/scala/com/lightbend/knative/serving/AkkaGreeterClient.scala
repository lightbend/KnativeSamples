package com.lightbend.knative.serving

import akka.actor.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.stream.Materializer

import knative.grpc.examples.helloworld._

import scala.concurrent.ExecutionContext
import scala.util._

object AkkaGreeterClient {

  def main(args: Array[String]): Unit = {
    // Boot akka
    implicit val sys: ActorSystem = ActorSystem("GreeterClient")
    implicit val mat: Materializer = Materializer(sys)
    implicit val ec: ExecutionContext = sys.dispatcher

    val config = sys.settings.config

    // These are read from the application.conf file under `resources`
//    val host = config.getString("helloworld.host")
//    val port = config.getString("helloworld.port").toInt
    val host = "grpcversioned.default.35.225.36.19.xip.io"
    val port = 80
    println(s"Connecting to server $host:$port")
    // Configure the client by code:
    val clientSettings = GrpcClientSettings.connectToServiceAt(host, port).withTls(false)
    // Create a client-side stub for the service
    val client: Greeter = GreeterClient(clientSettings)
    sys.log.info("Performing request")
    val request = HelloRequest(name = "world")
    1 to 20 foreach { _ =>
      val reply = client.sayHello(request)
      reply.onComplete {
        case Success(msg) =>
          println(s"got single reply: ${msg.message}")
        //        System.exit(0)
        case Failure(e) =>
          println(s"Error sayHello: $e")
        //        System.exit(0)
      }
    }
  }
}