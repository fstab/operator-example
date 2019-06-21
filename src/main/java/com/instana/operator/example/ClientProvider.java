package com.instana.operator.example;

import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.internal.KubernetesDeserializer;
import com.instana.operator.example.cr.ExampleResource;
import com.instana.operator.example.cr.ExampleResourceDoneable;
import com.instana.operator.example.cr.ExampleResourceList;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

public class ClientProvider {

  @Produces
  @Singleton
  KubernetesClient newClient() {
    return new DefaultKubernetesClient().inNamespace("default");
  }

  @Produces
  @Singleton
  NonNamespaceOperation<ExampleResource, ExampleResourceList, ExampleResourceDoneable, Resource<ExampleResource, ExampleResourceDoneable>> makeCustomResourceClient(
      KubernetesClient defaultClient) {

    KubernetesDeserializer.registerCustomKind("instana.com/v1alpha1", "Example", ExampleResource.class);

    CustomResourceDefinition crd = defaultClient
        .customResourceDefinitions()
        .list()
        .getItems()
        .stream()
        .filter(d -> "examples.instana.com".equals(d.getMetadata().getName()))
        .findAny()
        .orElseThrow(
            () -> new RuntimeException("Deployment error: Custom resource definition examples.instana.com not found."));

    return defaultClient
        .customResources(crd, ExampleResource.class, ExampleResourceList.class, ExampleResourceDoneable.class)
        .inNamespace("default");
  }
}