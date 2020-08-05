package com.lightbend.knative.serving

import scala.concurrent.Future
import akka.NotUsed
import akka.event.LoggingAdapter
import akka.stream.Materializer
import akka.stream.scaladsl.BroadcastHub
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.MergeHub
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import knative.grpc.examples.helloworld._


class GreeterServiceImpl(log: LoggingAdapter, message: String)(implicit mat: Materializer) extends Greeter {

  val (inboundHub: Sink[HelloRequest, NotUsed], outboundHub: Source[HelloReply, NotUsed]) =
    MergeHub.source[HelloRequest]
      .map(request => HelloReply(s"Hello, ${request.name}"))
      .toMat(BroadcastHub.sink[HelloReply])(Keep.both)
      .run()

  override def sayHello(request: HelloRequest): Future[HelloReply] = {
    log.info("sayHello with name {}", request.name)
    Future.successful(HelloReply(s"$message ${request.name}!"))
  }
}