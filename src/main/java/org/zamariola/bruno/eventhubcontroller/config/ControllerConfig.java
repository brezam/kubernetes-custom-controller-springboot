package org.zamariola.bruno.eventhubcontroller.config;

import io.kubernetes.client.extended.event.legacy.EventBroadcaster;
import io.kubernetes.client.extended.event.legacy.LegacyEventBroadcaster;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zamariola.bruno.eventhubcontroller.constants.ApiConstants;
import org.zamariola.bruno.eventhubcontroller.models.V1alpha1Eventhub;
import org.zamariola.bruno.eventhubcontroller.models.V1alpha1EventhubList;
import org.zamariola.bruno.eventhubcontroller.properties.EventHubProperties;

import java.io.IOException;

@Configuration
public class ControllerConfig {
  private final EventHubProperties eventhubProperties;


  @Autowired
  public ControllerConfig(EventHubProperties eventhubProperties) {
    this.eventhubProperties = eventhubProperties;
  }

  @Bean
  public GenericKubernetesApi<V1alpha1Eventhub, V1alpha1EventhubList> eventhubApi(ApiClient apiClient) {
    return new GenericKubernetesApi<>(
        V1alpha1Eventhub.class,
        V1alpha1EventhubList.class,
        ApiConstants.EVENTHUB_API_GROUP,
        ApiConstants.EVENTHUB_API_VERSION,
        ApiConstants.EVENTHUB_RESOURCE_PLURAL,
        apiClient
    );
  }

  @Bean
  public SharedIndexInformer<V1alpha1Eventhub> eventHubInformer(SharedInformerFactory sharedInformerFactory,
                                                                GenericKubernetesApi<V1alpha1Eventhub, V1alpha1EventhubList> eventhubApi) {
    return sharedInformerFactory.sharedIndexInformerFor(
        eventhubApi, V1alpha1Eventhub.class, eventhubProperties.resyncPeriodSeconds().toMillis());
  }

  @Bean
  public CoreV1Api coreV1Api() throws IOException {
    ApiClient client = Config.defaultClient();
    io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
    return new CoreV1Api(client);
  }

  @Bean
  public EventBroadcaster eventBroadcaster(CoreV1Api coreV1Api) {
    LegacyEventBroadcaster eventBroadcaster = new LegacyEventBroadcaster(coreV1Api);
    eventBroadcaster.startRecording();
    return eventBroadcaster;
  }
}
