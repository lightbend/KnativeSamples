package com.lightbend.knative

import com.typesafe.config.ConfigFactory
import io.grpc.ManagedChannelBuilder
import knative.grpc.examples.helloworld.helloworld._


object GRPCClient {

  val config = ConfigFactory.load()
  val port = config.getString("helloworld.port").toInt

  def main(args: Array[String]): Unit = {

    val builder = ManagedChannelBuilder.forAddress("grpcservice.default.35.225.36.19.xip.io", 80)
    builder.usePlaintext()
    val channel = builder.build
    val stub = GreeterGrpc.blockingStub(channel)
    val request = HelloRequest(name = "world")
    val reply: HelloReply = stub.sayHello(request)
    println(reply.message)
  }
}