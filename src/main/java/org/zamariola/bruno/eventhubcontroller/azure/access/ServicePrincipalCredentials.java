package org.zamariola.bruno.eventhubcontroller.azure.access;

public record ServicePrincipalCredentials(String clientId, String clientSecret, String tenantId) {
}
