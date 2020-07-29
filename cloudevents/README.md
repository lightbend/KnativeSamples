# Cloud Events

An end to end implementation of the cloudevents. An event definition is based on [JSON events format](https://github.com/cloudevents/spec/blob/master/json-format.md).
Java code for event implementation is available [here](https://github.com/cloudevents/sdk-java/blob/master/core/src/main/java/io/cloudevents/core/v1/CloudEventV1.java).
The requirements for creating an event producer are defined [here](https://github.com/knative/docs/tree/master/docs/eventing/samples/container-source#create-a-new-event-source-using-containersource)

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
There are 2 [approaches](https://github.com/knative/docs/tree/master/docs/eventing/samples/writing-event-source-easy-way)
 which I tried - direct connection using [ContainerSource](deploy) and [sinkbinding](deploy/sinkbinding).
 Both of them resolve sink to a value of ` http://eventsreciever.default.svc.cluster.local` which does not seem
 to accept post requests, while an actual endpoint `http://eventsreciever.default.35.225.36.19.xip.io` does.
 So there is still some confusion how it should work.

## Cleanup
````
kubectl delete ksvc eventsreciever
````

Copyright (C) 2020 Lightbend Inc. (https://www.lightbend.com).

