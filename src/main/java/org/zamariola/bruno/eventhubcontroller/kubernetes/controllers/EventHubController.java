package org.zamariola.bruno.eventhubcontroller.kubernetes.controllers;

import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRule;
import io.kubernetes.client.extended.event.legacy.EventBroadcaster;
import io.kubernetes.client.extended.workqueue.DefaultRateLimitingQueue;
import io.kubernetes.client.informer.ResourceEventHandler;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.zamariola.bruno.eventhubcontroller.azure.access.EventHubAccess;
import org.zamariola.bruno.eventhubcontroller.azure.access.ServicePrincipalAccess;
import org.zamariola.bruno.eventhubcontroller.azure.access.ServicePrincipalCredentials;
import org.zamariola.bruno.eventhubcontroller.constants.ApiConstants;
import org.zamariola.bruno.eventhubcontroller.constants.EventConstants;
import org.zamariola.bruno.eventhubcontroller.kubernetes.recorder.EventHubRecorder;
import org.zamariola.bruno.eventhubcontroller.kubernetes.secret.KubernetesSecret;
import org.zamariola.bruno.eventhubcontroller.models.V1alpha1Eventhub;
import org.zamariola.bruno.eventhubcontroller.models.V1alpha1EventhubList;
import org.zamariola.bruno.eventhubcontroller.models.V1alpha1EventhubSpec;
import org.zamariola.bruno.eventhubcontroller.models.V1alpha1EventhubStatus;
import org.zamariola.bruno.eventhubcontroller.properties.EventHubProperties;

import java.time.Duration;
import java.util.Objects;

@Component
public class EventHubController {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventHubController.class);

  private final EventHubProperties eventHubProperties;
  private final EventHubRecorder recorder;
  private final SharedInformerFactory sharedInformerFactory;
  private final GenericKubernetesApi<V1alpha1Eventhub, V1alpha1EventhubList> eventhubApi;
  private final SharedInformer<V1alpha1Eventhub> eventhubInformer;
  private final Lister<V1alpha1Eventhub> eventhubLister;
  private final KubernetesSecret kubernetesSecretService;
  private final ServicePrincipalAccess servicePrincipalAccess;
  private final EventHubAccess eventHubAccess;
  private final DefaultRateLimitingQueue<WorkItem> workQueue = new DefaultRateLimitingQueue<>();


  @Autowired
  public EventHubController(EventHubProperties eventHubProperties,
                            EventBroadcaster eventBroadcaster,
                            SharedInformerFactory sharedInformerFactory,
                            GenericKubernetesApi<V1alpha1Eventhub, V1alpha1EventhubList> eventhubApi,
                            SharedIndexInformer<V1alpha1Eventhub> eventhubInformer,
                            KubernetesSecret kubernetesSecretService,
                            ServicePrincipalAccess servicePrincipalAccess,
                            EventHubAccess eventHubAccess) {
    this.eventHubProperties = eventHubProperties;
    this.recorder = new EventHubRecorder(eventBroadcaster);
    this.sharedInformerFactory = sharedInformerFactory;
    this.eventhubApi = eventhubApi;
    this.eventhubInformer = eventhubInformer;
    this.eventhubLister = new Lister<>(eventhubInformer.getIndexer());
    this.kubernetesSecretService = kubernetesSecretService;
    this.servicePrincipalAccess = servicePrincipalAccess;
    this.eventHubAccess = eventHubAccess;
    initialize();
  }


  public void initialize() {
    LOGGER.info("Setting up event handlers");
    initializeEventHandler(eventHubProperties.resyncPeriodSeconds());
    LOGGER.info("Starting informers");
    sharedInformerFactory.startAllRegisteredInformers();

    LOGGER.info("Waiting for informer caches to sync");
    while (!eventhubInformer.hasSynced()) {
    }
  }


  @Scheduled(fixedDelay = 5000L)
  public void consumeQueue() {
    WorkItem item;
    try {
      item = workQueue.get();
    } catch (InterruptedException e) {
      return;
    }
    MDC.put(ApiConstants.MDC_KUBE_OBJ, item.makeKey());
    V1alpha1Eventhub obj = null;
    try {
      if (item.workItemType() == WorkItem.Type.DELETE) {
        LOGGER.info("Deleting old access");
        deleteAccesses(item.getObject());
      } else {
        obj = eventhubLister.get(item.makeKey());
        if (obj == null) {
          LOGGER.warn("Object does not exist anymore");
          workQueue.forget(item);
          return;
        }
        LOGGER.info("Sync object");
        syncObject(obj);
      }
    } catch (Exception e) {
      recorder.error(obj == null ? item.getObject() : obj, e);
      workQueue.addRateLimited(item);
      return;
    }
    workQueue.done(item);
    MDC.clear();
  }


  @PreDestroy
  public void shutdown() {
    LOGGER.info("Stopping all registered informers");
    sharedInformerFactory.stopAllRegisteredInformers();
    LOGGER.info("Shutting down queue");
    workQueue.shutDown();
  }


  private void initializeEventHandler(Duration resyncPeriod) {
    eventhubInformer.addEventHandlerWithResyncPeriod(new ResourceEventHandler<>() {

      @Override
      public void onAdd(V1alpha1Eventhub obj) {
        assert obj.getMetadata() != null;
        MDC.put(ApiConstants.MDC_KUBE_OBJ, obj.getMetadata().getNamespace() + "/" + obj.getMetadata().getName());
        LOGGER.info("[onAdd] Add new event to work queue");
        workQueue.add(WorkItem.newUpdateEvent(obj));
      }

      @Override
      public void onUpdate(V1alpha1Eventhub oldObj, V1alpha1Eventhub newObj) {
        assert oldObj.getMetadata() != null;
        assert newObj.getMetadata() != null;
        MDC.put(ApiConstants.MDC_KUBE_OBJ, newObj.getMetadata().getNamespace() + "/" + newObj.getMetadata().getName());
        if (Objects.equals(oldObj.getMetadata().getResourceVersion(), newObj.getMetadata().getResourceVersion())) {
          LOGGER.info("[onUpdate] Add resync event to work queue");
          // This is a simple resync event. We may still want to check whether the secret wasn't deleted
          workQueue.add(WorkItem.newUpdateEvent(newObj));
          return;
        }
        if (Objects.equals(oldObj.getMetadata().getGeneration(), newObj.getMetadata().getGeneration())) {
          // Different resource version but equal generation implies only change in status
          LOGGER.info("[onUpdate] Skipping status update event");
          return;
        }
        // A delete event also appears in onUpdate with deletion a timestamp. Since we have defined onDelete we ignore this
        if (newObj.getMetadata().getDeletionTimestamp() == null) {
          LOGGER.info("[onUpdate] Add update event to work queue");
          // We delete the old accesses and create new ones
          workQueue.add(WorkItem.newDeleteEvent(oldObj));
          workQueue.addAfter(WorkItem.newUpdateEvent(newObj), Duration.ofSeconds(5));
        }
      }

      @Override
      public void onDelete(V1alpha1Eventhub obj, boolean deletedFinalStateUnknown) {
        if (deletedFinalStateUnknown) {
          LOGGER.warn("[onDelete] Received event with an object in an unknown final state. Ignoring");
        } else {
          assert obj.getMetadata() != null;
          MDC.put(ApiConstants.MDC_KUBE_OBJ, obj.getMetadata().getNamespace() + "/" + obj.getMetadata().getName());
          LOGGER.info("[onDelete] Add delete event to work queue");
          workQueue.add(WorkItem.newDeleteEvent(obj));
        }
      }
    }, resyncPeriod.toMillis());
  }


  private void syncObject(V1alpha1Eventhub object) {
    V1ObjectMeta metadata = object.getMetadata();
    V1alpha1EventhubSpec spec = object.getSpec();
    assert metadata != null;
    assert spec != null;

    // Checking if we already have a secret
    V1Secret oldSecret = kubernetesSecretService.findSecret(metadata.getNamespace(), metadata.getName());

    // If we have a secret and the generation of the secret matches the generation of the eventhub object, we assume everything is fine
    if (oldSecret != null
        && oldSecret.getMetadata() != null
        && oldSecret.getMetadata().getAnnotations() != null
        && Objects.equals(
        metadata.getGeneration(),
        Long.valueOf(oldSecret.getMetadata().getAnnotations().get(ApiConstants.SECRET_GENERATION_KEY)))) {
      return;
    }

    // Trying to find if host mappings contain that instance name
    String eventHubNamespaceName = eventHubProperties.hostAliasMapping().get(spec.getEventHubInstanceName());
    if (eventHubNamespaceName == null) {
      recorder.warn(object, EventConstants.WRONG_INSTANCE_TYPE_REASON, EventConstants.WRONG_INSTANCE_TYPE_MSG, spec.getEventHubInstanceName());
      updateStatus(object, "error");
      return;
    }
    // Trying to find if eventhub namespace exists
    String resourceGroup = spec.getEventHubResourceGroup() == null
        ? eventHubProperties.defaultResourceGroup()
        : spec.getEventHubResourceGroup();
    EventHubNamespace eventHubNamespace = eventHubAccess.findEventhubNamespace(
        resourceGroup, eventHubNamespaceName);
    if (eventHubNamespace == null) {
      recorder.warn(object, EventConstants.EVENTHUB_NOT_FOUND_REASON, EventConstants.EVENTHUB_NOT_FOUND_MSG, eventHubNamespaceName, resourceGroup);
      updateStatus(object, "error");
      return;
    }

    // Creating shared access policies
    String authorizationName = spec.getAuthorizationName();
    EventHubNamespaceAuthorizationRule authRule = eventHubAccess.createSharedAccessPolicy(
        eventHubNamespace,
        authorizationName,
        spec.getAuthorizationClaims());

    // Creating schema registry access through service principal
    ServicePrincipalCredentials credential = servicePrincipalAccess.createClientSecretForSchemaRegistrySpn(authorizationName);

    // Creating/Updating the k8s secret
    kubernetesSecretService.upsertSecretObject(
        oldSecret != null,
        metadata.getName(),
        metadata.getGeneration(),
        metadata.getNamespace(),
        eventHubNamespace.serviceBusEndpoint(),
        credential,
        object,
        spec.getEventHubInstanceName(),
        authRule);
    recorder.info(object, EventConstants.SECRET_REASON, EventConstants.SECRET_MSG, metadata.getName());

    // Updating object status (synced)
    updateStatus(object, "synced");
    recorder.info(object, EventConstants.SYNCED_REASON, EventConstants.SYNCED_MSG);
  }


  private void deleteAccesses(V1alpha1Eventhub object) {
    V1alpha1EventhubSpec spec = object.getSpec();
    assert spec != null;
    String eventHubNamespaceName = eventHubProperties.hostAliasMapping().get(spec.getEventHubInstanceName());
    if (eventHubNamespaceName != null) {
      String resourceGroup = spec.getEventHubResourceGroup() == null
          ? eventHubProperties.defaultResourceGroup()
          : spec.getEventHubResourceGroup();
      EventHubNamespace eventHubNamespace = eventHubAccess.findEventhubNamespace(
          resourceGroup, eventHubNamespaceName);
      eventHubAccess.deleteSharedAccessPolicy(eventHubNamespace, spec.getAuthorizationName());
    }
    servicePrincipalAccess.removeClientSecretForSchemaRegistrySpn(spec.getAuthorizationName());
  }

  private void updateStatus(V1alpha1Eventhub object, String status) {
    V1alpha1EventhubStatus statusObj = new V1alpha1EventhubStatus();
    statusObj.setCurrent(status);
    eventhubApi.updateStatus(object, o -> statusObj);
  }
}
