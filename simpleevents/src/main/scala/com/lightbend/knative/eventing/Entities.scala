package com.lightbend.knative.eventing

import spray.json._

case class HelloRequest(name : String)

case class HelloResponse(message : String)

object HelloRequestJsonSupport extends DefaultJsonProtocol {
  implicit val helloRequestFormat = jsonFormat1(HelloRequest.apply)
}

object HelloResponseJsonSupport extends DefaultJsonProtocol {
  implicit val helloResponseFormat = jsonFormat1(HelloResponse.apply)
}