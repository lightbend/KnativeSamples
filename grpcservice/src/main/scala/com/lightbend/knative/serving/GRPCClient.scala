package com.lightbend.knative.serving

import com.typesafe.config.ConfigFactory
import io.grpc.ManagedChannelBuilder
import knative.grpc.examples.helloworld.helloworld._


object GRPCClient {

  val config = ConfigFactory.load()
  val port = config.getString("helloworld.port").toInt

  def main(args: Array[String]): Unit = {

    val config = ConfigFactory.load()
    val port = config.getString("helloworld.port").toInt
    val host = config.getString("helloworld.host")
    val builder = ManagedChannelBuilder.forAddress(host, port)
    builder.usePlaintext()
    val channel = builder.build
    val stub = GreeterGrpc.blockingStub(channel)
    val request = HelloRequest(name = "world")
    val reply: HelloReply = stub.sayHello(request)
    println(reply.message)
  }
}