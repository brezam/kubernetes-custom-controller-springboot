package org.zamariola.bruno.eventhubcontroller.azure.access;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zamariola.bruno.eventhubcontroller.azure.AzureRM;
import org.zamariola.bruno.eventhubcontroller.models.V1alpha1EventhubSpec;
import org.zamariola.bruno.eventhubcontroller.utils.LRUCache;

import java.util.List;
import java.util.Map;

@Component
public class EventHubAccess {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventHubAccess.class);
  private static final String RESOURCE_GROUP_NOT_FOUND = "ResourceGroupNotFound";
  private static final String RESOURCE_NOT_FOUND = "ResourceNotFound";

  private final Map<String, EventHubNamespace> cachedEventHubs = new LRUCache<>(32, 64);
  private final AzureResourceManager azureRm;

  @Autowired
  public EventHubAccess(final AzureRM azureRM) {
    this.azureRm = azureRM.getResourceManager();
  }

  public EventHubNamespace findEventhubNamespace(String resourceGroup, String namespace) throws ManagementException {
    String cacheKey = resourceGroup + "/" + namespace;
    if (!cachedEventHubs.containsKey(cacheKey)) {
      EventHubNamespace eventHubNamespace;
      try {
        eventHubNamespace = azureRm.eventHubNamespaces().getByResourceGroup(resourceGroup, namespace);
      } catch (ManagementException e) {
        if (RESOURCE_GROUP_NOT_FOUND.equals(e.getValue().getCode()) || RESOURCE_NOT_FOUND.equals(e.getValue().getCode())) {
          return null;
        }
        throw e;
      }
      cachedEventHubs.put(cacheKey, eventHubNamespace);
    }
    return cachedEventHubs.get(cacheKey);
  }

  public EventHubNamespaceAuthorizationRule createSharedAccessPolicy(EventHubNamespace eventHubNamespace,
                                                                     String authorizationName,
                                                                     List<V1alpha1EventhubSpec.AuthorizationClaimsEnum> claims) {
    EventHubNamespaceAuthorizationRule.DefinitionStages.WithAccessPolicy withAccessPolicy =
        azureRm.eventHubNamespaces().authorizationRules().define(authorizationName)
            .withExistingNamespace(eventHubNamespace.resourceGroupName(), eventHubNamespace.name());
    LOGGER.info("Creating authorization rule {} in eventhub namespace {} with claims {}", authorizationName, eventHubNamespace.name(), claims);
    if (claims.contains(V1alpha1EventhubSpec.AuthorizationClaimsEnum.MANAGE)) {
      return withAccessPolicy.withManageAccess().create();
    }
    if (claims.contains(V1alpha1EventhubSpec.AuthorizationClaimsEnum.SEND)) {
      if (claims.contains(V1alpha1EventhubSpec.AuthorizationClaimsEnum.LISTEN)) {
        return withAccessPolicy.withSendAndListenAccess().create();
      }
      return withAccessPolicy.withSendAccess().create();
    }
    return withAccessPolicy.withListenAccess().create();

  }

  public void deleteSharedAccessPolicy(EventHubNamespace eventHubNamespace,
                                       String authorizationName) {
    LOGGER.info("Deleting authorization rule {} in eventhub namespace {}", authorizationName, eventHubNamespace.name());
    azureRm.eventHubNamespaces().authorizationRules()
        .deleteByName(eventHubNamespace.resourceGroupName(), eventHubNamespace.name(), authorizationName);
  }
}
