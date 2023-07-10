package org.zamariola.bruno.eventhubcontroller.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("azure.schema-registry-access")
public record AzureSchemaRegistryAccessProperties(String clientId) {
}
