Operator Example
----------------

Build:

```
./mvnw clean package
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/getting-started-jvm .
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
