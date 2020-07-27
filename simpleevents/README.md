# Simple Events

A simple app that can receive and send Cloud Events that you can use for testing. It supports running in two modes:
* The default mode has the app reply to your input events with the output event, which is simplest for demonstrating things working in isolation, but is also the model for working for the Knative Eventing Broker concept.
* K_SINK mode has the app send events to the destination encoded in $K_SINK, which is useful to demonstrate how folks can synthesize events to send to a Service or Broker when not initiated by a Broker invocation (e.g. implementing an event source)

The application will use $K_SINK-mode whenever the environment variable is specified.

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
kubectl get ksvc grpcservice
````
which returns url for a service. 

Run curl command:
````
curl -X POST -H "content-type: application/json"  -d '{"name":"Dave"}' <URL>>
````

***Note:*** There is some weirdnes in Knative deployment. In this example our listener listens
on two URL - root and /secondary. But when deployed, the url seems to always point to root, meaning that both 
requests:
````
curl -X POST -H "content-type: application/json"  -d '{"name":"hw test"}' http://simpleevents.default.35.225.36.19.xip.io
````
and 
````
curl -X POST -H "content-type: application/json"  -d '{"name":"hw test"}' http://simpleevents.default.35.225.36.19.xip.io/secondary
````
return the same result:
````
"Hello from event listener  hw test!"
````

## Cleanup
````
kubectl delete ksvc simpleevents
````

Copyright (C) 2020 Lightbend Inc. (https://www.lightbend.com).

