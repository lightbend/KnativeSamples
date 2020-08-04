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

According to this [document](https://knative.dev/docs/serving/cluster-local-route/), in order 
to Label a service to be cluster-local, run the following command:
````
kubectl label kservice eventsreciever serving.knative.dev/visibility=cluster-local 
```` 
After this, running
````
kubectl get ksvc
````
returns the following:
````
NAME                   URL                                                       LATESTCREATED                       LATESTREADY                         READY   REASON
eventsreciever         http://eventsreciever.default.svc.cluster.local           eventsreciever-mn8n5                eventsreciever-mn8n5                True    
````

This is only required to disable remote access, local access works even if the service is not made local. 

***Note*** There seem no way to remove label `cluster-local` short of recreating a service

There are several [approaches](https://github.com/knative/docs/tree/master/docs/eventing/samples/writing-event-source-easy-way)
which I tried: 
* direct connection using [ContainerSource](deploy) 
* [sinkbinding](deploy/sinkbinding).
* using a [broker](deploy/broker). For more details look [here](https://knative.dev/docs/eventing/broker/)

For direct connection use [eventsreciever.yaml](deploy/eventsreciever.yaml) and [eventssource.yaml.yaml](deploy/eventssource.yaml) to do the following:
````
kubectl apply -f /Users/boris/Projects/KnativeSamples/cloudevents/deploy/eventsreciever.yaml
kubectl apply -f /Users/boris/Projects/KnativeSamples/cloudevents/deploy/eventssource.yaml
````
Once everything is running, you can see that the events are published and reciever shows events

For sink binding connection use [eventsreciever.yaml](deploy/sinkbinding/eventsreciever.yaml), 
[eventssourcedeployment.yaml](deploy/sinkbinding/eventssourcedeployment.yaml) and [sinkbinding.yaml](deploy/sinkbinding/sinkbinding.yaml) and to do the following:
````
kubectl apply -f /Users/boris/Projects/KnativeSamples/cloudevents/deploy/sinkbinding/eventsreciever.yaml
kubectl apply -f /Users/boris/Projects/KnativeSamples/cloudevents/deploy/sinkbinding/eventssourcedeployment.yaml
kubectl apply -f /Users/boris/Projects/KnativeSamples/cloudevents/deploy/sinkbinding/sinkbinding.yaml
````

Unlike, the previous case, where event source was directly binded to the reciever, here the binding is done using a separate
object - sinkbinding.yaml.

For broker based connection use [broker.yaml](deploy/broker/broker.yaml), [eventssource.yaml](deploy/broker/eventssource.yaml) 
[serviceconsumer.yaml](deploy/broker/serviceconsumer.yaml) and [trigger.yaml](deploy/broker/trigger.yaml) and to do the following:
````
kubectl apply -f /Users/boris/Projects/KnativeSamples/cloudevents/deploy/broker/broker.yaml
kubectl apply -f /Users/boris/Projects/KnativeSamples/cloudevents/deploy/broker/eventssource.yaml
kubectl apply -f /Users/boris/Projects/KnativeSamples/cloudevents/deploy/broker/serviceconsumer.yaml
kubectl apply -f /Users/boris/Projects/KnativeSamples/cloudevents/deploy/broker/trigger.yaml
````
Here event source is writing to broker, which in turn writes to the event consumer. Connection between 
event source and broker is set in the eventssource.yaml (sink), while connection between broker 
and event consumer is set through trigger object.

## Cleanup
````
kubectl delete ksvc eventsreciever
````

Copyright (C) 2020 Lightbend Inc. (https://www.lightbend.com).

