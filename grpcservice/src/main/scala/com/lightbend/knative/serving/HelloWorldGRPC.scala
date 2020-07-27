package com.lightbend.knative.serving

import com.typesafe.config.ConfigFactory
import knative.grpc.examples.helloworld.helloworld._
import org.slf4j._
import scala.concurrent._
import io.grpc._

object HelloWorldGRPC{

  val log = LoggerFactory.getLogger(this.getClass.getName)

  val config = ConfigFactory.load()
  val message = config.getString("helloworld.message")
  val port = config.getString("helloworld.port").toInt

  def main(args: Array[String]): Unit = {
    val server = new HelloWorldServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }
}

class HelloWorldServer(executionContext: ExecutionContext) { self =>
  private[this] var server: Server = null

  def start(): Unit = {
    val builder = ServerBuilder.forPort(HelloWorldGRPC.port)
    builder.addService(GreeterGrpc.bindService(new GreeterImpl, executionContext))
    server = builder.build.start
    println("Server started, listening on " + HelloWorldGRPC.port)
    sys.addShutdownHook {
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("*** server shut down")
    }
  }

  private def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  private class GreeterImpl extends GreeterGrpc.Greeter {
    override def sayHello(req: HelloRequest) = {
      println(s"New request with name ${req.name}")
      val reply = HelloReply(message = s"${HelloWorldGRPC.message} ${req.name}!")
      Future.successful(reply)
    }
  }
}