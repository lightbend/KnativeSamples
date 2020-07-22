# GRPC Service

A microservice which demonstrates how to get set up and running with Knative GRPC Serving. 
It will respond to a GRPC request with a text specified as an ENV variable named MESSAGE, defaulting to "Hello".
See [ScalaPB documentation](https://scalapb.github.io/grpc.html) for building GRPC
Alternatively use this [project](https://github.com/akka/akka-grpc-sample-kubernetes-scala) to build the same implementation
using Akka GRPC. Also look [here](https://doc.akka.io/docs/akka-grpc/current/client/walkthrough.html#writing-a-client-program) 
for implementation of a GRPC client for Akka GRPC  

##Before you begin
A Kubernetes cluster installation with Knative Serving up and running See [here](README.md) for installation instractions.
Docker installed locally, and running, optionally a Docker Hub account configured or some other Docker Repository installed locally.
Java JDK8 or later installed locally.
Scala's standard build tool sbt installed locally.

##SBT build
To build run the command
````
sbt docker
````
##Configuring the Service descriptor

Service descriptor is [here](deploy/service.yaml)

## Testing
Get service URL using:
````
kubectl get ksvc httpservice
````
which returns url for a service. Use this URL to run [client](src/main/scala/com/lightbend/knative/GRPCClient.scala)
to access service

## Cleanup
````
kubectl delete ksvc httpservice
````
Copyright (C) 2020 Lightbend Inc. (https://www.lightbend.com).

