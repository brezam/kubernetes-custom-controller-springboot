package org.zamariola.bruno.eventhubcontroller.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("azure.credentials")
public record AzureCredentialsProperties(
    String subscriptionId,
    String clientId,
    String clientSecret,
    String tenantId
) {
}
