Operator Example
----------------

_Note: This example can be used to build an operator in native mode, but it requires a local Quarkus version 999-SNAPSHOT which was built from this PR: https://github.com/quarkusio/quarkus/pull/2910_

Build:

```
./mvnw clean package -Pnative -Dnative-image.docker-build=true
docker build -f src/main/docker/Dockerfile.native -t quarkus/getting-started-native .
```

Prepare:

```
kubectl apply -f deploy/operator-example.clusterrole.yaml
kubectl apply -f deploy/operator-example.serviceaccount.yaml
kubectl apply -f deploy/operator-example.clusterrolebinding.yaml
kubectl apply -f deploy/operator-example.crd.yaml
```

Deploy:

```
kubectl apply -f deploy/operator-example.deployment.yaml
```

Trigger the operator by creating a custom resource (the operator should create a daemon set with `kuard` pods):

```
kubectl apply -f deploy/operator-example.cr.yaml
```
