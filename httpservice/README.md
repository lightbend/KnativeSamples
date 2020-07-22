# HTTP Service

A microservice which demonstrates how to get set up and running with Knative HTTP Serving. 
It will respond to a HTTP request with a text specified as an ENV variable named MESSAGE, defaulting to "Hello World!".

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
curl -k -v --http2 http://httpservice.default.35.225.36.19.xip.io
````
Here keys `-k -v --http2` are optional

## Cleanup
````
kubectl delete ksvc grpcservice
````

Copyright (C) 2020 Lightbend Inc. (https://www.lightbend.com).

