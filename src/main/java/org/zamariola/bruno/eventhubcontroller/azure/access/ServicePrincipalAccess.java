package org.zamariola.bruno.eventhubcontroller.azure.access;

import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zamariola.bruno.eventhubcontroller.azure.AzureRM;
import org.zamariola.bruno.eventhubcontroller.exceptions.ServicePrincipalNotFound;

@Component
public class ServicePrincipalAccess {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServicePrincipalAccess.class);
  private final ActiveDirectoryApplication servicePrincipalApplication;

  @Autowired
  public ServicePrincipalAccess(final AzureRM azureRM) {
    String schemaRegistrySpnClientId = azureRM.getSchemaRegistrySpnClientId();
    servicePrincipalApplication = azureRM.getResourceManager().accessManagement()
        .activeDirectoryApplications()
        .list()
        .stream()
        .filter(spn -> schemaRegistrySpnClientId.equals(spn.applicationId()))
        .findAny()
        .orElseThrow(() -> new ServicePrincipalNotFound(schemaRegistrySpnClientId));
  }

  public ServicePrincipalCredentials createClientSecretForSchemaRegistrySpn(String credentialDescription) {
    if (servicePrincipalApplication.passwordCredentials().containsKey(credentialDescription)) {
      // If we already have a client secret with the same description, we will remove the older one in order to avoid trash from piling up
      servicePrincipalApplication.update().withoutCredential(credentialDescription).apply();
    }

    final String[] passwordData = new String[2];
    servicePrincipalApplication.update().definePasswordCredential(credentialDescription)
        .withPasswordConsumer(p -> {
          passwordData[0] = p.id(); // This is the Secret ID, a value that is not sensitive
          passwordData[1] = p.value(); // This is the client secret
        }).attach().apply();
    LOGGER.info("Created client secret with description {} and id {}", credentialDescription, passwordData[0]);
    return new ServicePrincipalCredentials(servicePrincipalApplication.applicationId(), passwordData[1], servicePrincipalApplication.manager().tenantId());

  }

  public void removeClientSecretForSchemaRegistrySpn(String credentialDescription) {
    LOGGER.info("Deleting client secret with description {}", credentialDescription);
    servicePrincipalApplication.update().withoutCredential(credentialDescription).apply();
  }

}
