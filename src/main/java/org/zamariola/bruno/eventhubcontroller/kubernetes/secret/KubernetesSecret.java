package org.zamariola.bruno.eventhubcontroller.kubernetes.secret;

import com.azure.resourcemanager.eventhubs.models.EventHubNamespaceAuthorizationRule;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1SecretBuilder;
import io.kubernetes.client.util.PatchUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zamariola.bruno.eventhubcontroller.azure.access.ServicePrincipalCredentials;
import org.zamariola.bruno.eventhubcontroller.constants.ApiConstants;
import org.zamariola.bruno.eventhubcontroller.exceptions.KubernetesApiException;
import org.zamariola.bruno.eventhubcontroller.models.V1alpha1Eventhub;

import java.util.Map;

@Component
public class KubernetesSecret {
  private final Logger LOGGER = LoggerFactory.getLogger(KubernetesSecret.class);

  private final CoreV1Api coreV1Api;

  @Autowired
  public KubernetesSecret(final CoreV1Api coreV1Api) {
    this.coreV1Api = coreV1Api;
  }

  public V1Secret findSecret(String namespace, String name) {
    try {
      return coreV1Api.readNamespacedSecret(name, namespace, "");
    } catch (ApiException e) {
      // If the error was something other than NOT FOUND (404), we rethrow. It might be a temporary network error.
      if (e.getCode() == 404) {
        LOGGER.info("Secret not found.");
        return null;
      }
      throw new KubernetesApiException(e);
    }
  }

  public void upsertSecretObject(Boolean objectAlreadyExists,
                                 String secretName,
                                 Long eventHubObjectGeneration,
                                 String namespace,
                                 String eventHubServiceBusEndpoint,
                                 ServicePrincipalCredentials credential,
                                 V1alpha1Eventhub eventhubObject,
                                 String instanceName,
                                 EventHubNamespaceAuthorizationRule authRule) {
    assert eventhubObject.getMetadata() != null;
    String hostWithoutHttpsAndPort = eventHubServiceBusEndpoint
        .replaceFirst("^https://", "")
        .replaceFirst(":\\d+/$", "");
    String instanceKeyName = instanceName.replace("-", "_").toUpperCase();
    Map<String, byte[]> secrets = Map.of(
        "EVENT_HUB_" + instanceKeyName + "_PRIMARY_HOST", hostWithoutHttpsAndPort.getBytes(),
        "EVENT_HUB_" + instanceKeyName + "_PRIMARY_CONNECTION_STRING", authRule.getKeys().primaryConnectionString().getBytes(),
        "EVENT_HUB_" + instanceKeyName + "_SECONDARY_CONNECTION_STRING", authRule.getKeys().secondaryConnectionString().getBytes(),
        "EVENT_HUB_" + instanceKeyName + "_SCHEMA_CLIENT_ID", credential.clientId().getBytes(),
        "EVENT_HUB_" + instanceKeyName + "_SCHEMA_CLIENT_SECRET", credential.clientSecret().getBytes(),
        "EVENT_HUB_" + instanceKeyName + "_SCHEMA_TENANT_ID", credential.tenantId().getBytes());
    try {
      if (objectAlreadyExists) {
        V1Secret secretUpdate = new V1SecretBuilder()
            .withApiVersion("v1")
            .withKind("Secret")
            .withNewMetadata()
            .withAnnotations(Map.of(ApiConstants.SECRET_GENERATION_KEY, Long.toString(eventHubObjectGeneration)))
            .endMetadata()
            .withData(secrets)
            .build();
        String secretJSON = coreV1Api.getApiClient().getJSON().serialize(secretUpdate);
        PatchUtils.patch(
            V1Secret.class,
            () -> coreV1Api.patchNamespacedSecretCall(secretName, namespace, new V1Patch(secretJSON), null, null, ApiConstants.CONTROLLER_NAME, "", true, null),
            V1Patch.PATCH_FORMAT_APPLY_YAML,
            coreV1Api.getApiClient());
        LOGGER.info("Updated kubernetes secret");
      } else {
        V1Secret newSecret = new V1SecretBuilder()
            .withNewMetadata()
            .withName(secretName)
            .withNamespace(namespace)
            .withAnnotations(Map.of(ApiConstants.SECRET_GENERATION_KEY, Long.toString(eventHubObjectGeneration)))
            .addNewOwnerReference()
            .withApiVersion(eventhubObject.getApiVersion())
            .withKind(eventhubObject.getKind())
            .withName(eventhubObject.getMetadata().getName())
            .withUid(eventhubObject.getMetadata().getUid())
            .endOwnerReference()
            .endMetadata()
            .withData(secrets)
            .build();
        coreV1Api.createNamespacedSecret(namespace, newSecret, null, null, ApiConstants.CONTROLLER_NAME, "");
        LOGGER.info("Created kubernetes secret");
      }
    } catch (ApiException e) {
      throw new KubernetesApiException(e);
    }
  }
}
