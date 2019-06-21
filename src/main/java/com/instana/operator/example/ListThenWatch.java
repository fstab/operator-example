package com.instana.operator.example;

import io.fabric8.kubernetes.api.model.apps.DaemonSet;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.quarkus.runtime.StartupEvent;
import com.instana.operator.example.cr.ExampleResource;
import com.instana.operator.example.cr.ExampleResourceDoneable;
import com.instana.operator.example.cr.ExampleResourceList;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@ApplicationScoped
public class ListThenWatch {

  private final Map<String, ExampleResource> cache = new ConcurrentHashMap<>();

  @Inject
  private KubernetesClient defaultClient;

  @Inject
  private NonNamespaceOperation<ExampleResource, ExampleResourceList, ExampleResourceDoneable, Resource<ExampleResource, ExampleResourceDoneable>> crClient;

  void onStartup(@Observes StartupEvent _ev) {
    new Thread(this::runWatch).start();
  }

  private void runWatch() {
    Executor executor = Executors.newSingleThreadExecutor();
    listThenWatch((action, uid) -> executor.execute(() -> handleEvent(action, uid)));
  }

  private void handleEvent(Watcher.Action action, String uid) {
    try {
      ExampleResource resource = cache.get(uid);
      if (resource == null) {
        return;
      }

      Predicate<DaemonSet> ownerRefMatches = daemonSet -> daemonSet.getMetadata().getOwnerReferences().stream()
          .anyMatch(ownerReference -> ownerReference.getUid().equals(uid));

      if (defaultClient
          .apps()
          .daemonSets()
          .list()
          .getItems()
          .stream()
          .noneMatch(ownerRefMatches)) {

        defaultClient
            .apps()
            .daemonSets()
            .create(newDaemonSet(resource));
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  private DaemonSet newDaemonSet(ExampleResource resource) {
    DaemonSet daemonSet = defaultClient.apps().daemonSets()
        .load(getClass().getResourceAsStream("/daemonset.yaml")).get();
    daemonSet.getMetadata().getOwnerReferences().get(0).setUid(resource.getMetadata().getUid());
    daemonSet.getMetadata().getOwnerReferences().get(0).setName(resource.getMetadata().getName());
    return daemonSet;
  }

  private void listThenWatch(BiConsumer<Watcher.Action, String> callback) {

    try {

      // list

      crClient
          .list()
          .getItems()
          .forEach(resource -> {
                cache.put(resource.getMetadata().getUid(), resource);
                callback.accept(Watcher.Action.ADDED, resource.getMetadata().getUid());
              }
          );

      // watch

      crClient.watch(new Watcher<ExampleResource>() {
        @Override
        public void eventReceived(Action action, ExampleResource resource) {
          try {
            String uid = resource.getMetadata().getUid();
            if (cache.containsKey(uid)) {
              int knownResourceVersion = Integer.parseInt(cache.get(uid).getMetadata().getResourceVersion());
              int receivedResourceVersion = Integer.parseInt(resource.getMetadata().getResourceVersion());
              if (knownResourceVersion > receivedResourceVersion) {
                return;
              }
            }
            System.out.println("received " + action + " for resource " + resource);
            if (action == Action.ADDED || action == Action.MODIFIED) {
              cache.put(uid, resource);
            } else {
              cache.remove(uid);
            }
            callback.accept(action, uid);
          } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
          }
        }

        @Override
        public void onClose(KubernetesClientException cause) {
          cause.printStackTrace();
          System.exit(-1);
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
}
