package org.zamariola.bruno.eventhubcontroller.azure;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zamariola.bruno.eventhubcontroller.properties.AzureCredentialsProperties;
import org.zamariola.bruno.eventhubcontroller.properties.AzureSchemaRegistryAccessProperties;

@Component
public class AzureRM {

  private final String schemaRegistrySpnClientId;
  private final AzureResourceManager resourceManager;

  @Autowired
  public AzureRM(final AzureCredentialsProperties azureCredentialsProperties,
                 final AzureSchemaRegistryAccessProperties azureSchemaRegistryAccessProperties) {
    this.schemaRegistrySpnClientId = azureSchemaRegistryAccessProperties.clientId();

    AzureProfile profile = new AzureProfile(
        azureCredentialsProperties.tenantId(),
        azureCredentialsProperties.subscriptionId(),
        AzureEnvironment.AZURE);

    TokenCredential credential = new ClientSecretCredentialBuilder()
        .clientId(azureCredentialsProperties.clientId())
        .clientSecret(azureCredentialsProperties.clientSecret())
        .tenantId(azureCredentialsProperties.tenantId())
        .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
        .build();

    this.resourceManager = AzureResourceManager
        .authenticate(credential, profile)
        .withDefaultSubscription();
  }

  public String getSchemaRegistrySpnClientId() {
    return schemaRegistrySpnClientId;
  }

  public AzureResourceManager getResourceManager() {
    return resourceManager;
  }
}
