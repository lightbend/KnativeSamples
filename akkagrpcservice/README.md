# Akka GRPC Service

A microservice which demonstrates how to get set up and running with Knative GRPC Serving. 
It will respond to a GRPC request with a text specified as an ENV variable named MESSAGE, defaulting to "Hello".
See this [project](https://github.com/akka/akka-grpc-sample-kubernetes-scala) to build the same implementation
using Akka GRPC. Also look [here](https://doc.akka.io/docs/akka-grpc/current/client/walkthrough.html#writing-a-client-program) 
for implementation of a GRPC client for Akka GRPC.

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

We will use this to demonstrate split traffic between 2 service revision
 
* [Deploy version 1](deploy/serviceV1.yaml) - all traffic goes to v1
* [Deploy version 2 with no traffic block](deploy/serviceV2allTraffic.yaml) by default all traffic is going latest version of service
* [Deploy version 2 with traffic block](deploy/serviceV2.yaml) with traffic still going to first revision
* [Deploy version 2 split](deploy/servicesplit.yaml) with traffic split between revisions 50:50

## Testing
Get service URL using:
````
kubectl get ksvc httpservice
````
which returns url for a service. Use this URL to run [client](src/main/scala/com/lightbend/knative/serving/GRPCClient.scala)
to access service multiple times. Because different revisions return different messages, you can see
where traffic is routed

## Cleanup
````
kubectl delete ksvc grpcversioned
````
Copyright (C) 2020 Lightbend Inc. (https://www.lightbend.com).

