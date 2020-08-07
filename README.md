# Knative samples

This is Scala implementation of the Knative example described [here](https://knative.dev/docs/samples/) and the code contained [here](https://github.com/knative/docs/tree/master/docs/serving/samples)
for serving and [here](https://github.com/knative/docs/tree/master/docs/eventing/samples) for eventing. 

## Installing Knative

Installation is based on [these instractions](https://knative.dev/docs/install/any-kubernetes-cluster/)

***Note*** Kubernetes version 1.16 is minimal required for install

### Install serving
Install CRDs
````
kubectl apply --filename https://github.com/knative/serving/releases/download/v0.16.0/serving-crds.yaml
kubectl apply --filename https://github.com/knative/serving/releases/download/v0.16.0/serving-core.yaml
````
[Installing Istio](https://istio.io/latest/docs/setup/getting-started/)

Version 1.6.0
````
bin/istioctl install --set profile=demo
````
***Note*** when installing Istio make sure that you add `cluster-local-gateway` as described [here](https://knative.dev/docs/install/installing-istio/)

I just added this:
````
     - name: cluster-local-gateway
        enabled: true
        label:
          istio: cluster-local-gateway
          app: cluster-local-gateway
        k8s:
          service:
            type: ClusterIP
            ports:
            - port: 15020
              name: status-port
            - port: 80
              name: http2
            - port: 443
              name: https
````
to demo profile

Install Istio for Knative
````
kubectl apply --filename https://github.com/knative/net-istio/releases/download/v0.16.0/release.yaml
kubectl --namespace istio-system get service istio-ingressgateway
````
Configure DNS
Using a simple Kubernetes Job called “default domain” that will configure Knative Serving to use xip.io as the default DNS suffix.
````
kubectl apply --filename https://github.com/knative/serving/releases/download/v0.16.0/serving-default-domain.yaml
````
### Install Eventing
Install CRDs
````
kubectl apply --filename https://github.com/knative/eventing/releases/download/v0.16.0/eventing-crds.yaml
````
Install Core components
````
kubectl apply --filename https://github.com/knative/eventing/releases/download/v0.16.0/eventing-core.yaml
````
Install Channels:
* default
````
kubectl apply --filename https://github.com/knative/eventing/releases/download/v0.16.0/in-memory-channel.yaml
````
* Kafka
[install broker](https://knative.dev/docs/eventing/samples/kafka/index.html)
````
kubectl create namespace kafka
curl -L "https://github.com/strimzi/strimzi-kafka-operator/releases/download/0.16.2/strimzi-cluster-operator-0.16.2.yaml" \
  | sed 's/namespace: .*/namespace: kafka/' \
  | kubectl -n kafka apply -f -
````
Create cluster
````
kubectl apply -n kafka -f - <<EOF
apiVersion: kafka.strimzi.io/v1beta1
kind: Kafka
metadata:
  name: my-cluster
spec:
  kafka:
    version: 2.4.0
    replicas: 1
    listeners:
      plain: {}
      tls: {}
    config:
      offsets.topic.replication.factor: 1
      transaction.state.log.replication.factor: 1
      transaction.state.log.min.isr: 1
      log.message.format.version: "2.4"
    storage:
      type: ephemeral
  zookeeper:
    replicas: 3
    storage:
      type: ephemeral
  entityOperator:
    topicOperator: {}
    userOperator: {}
EOF
````
Install the channel
````
curl -L "https://github.com/knative/eventing-contrib/releases/download/v0.16.0/kafka-channel.yaml" \
 | sed 's/REPLACE_WITH_CLUSTER_URL/my-cluster-kafka-bootstrap.kafka:9092/' \
 | kubectl apply --filename -
````
Install a Broker (eventing) layer
````
kubectl apply --filename https://github.com/knative/eventing/releases/download/v0.16.0/mt-channel-broker.yaml
````
Install Knative CLI

On Mac use brew to install [cli](https://knative.dev/docs/install/install-kn/)
````
brew install knative/client/kn
````
For the list of `kn` commands go [here](https://github.com/knative/client/blob/master/docs/cmd/kn.md)

Copyright (C) 2020 Lightbend Inc. (https://www.lightbend.com).

