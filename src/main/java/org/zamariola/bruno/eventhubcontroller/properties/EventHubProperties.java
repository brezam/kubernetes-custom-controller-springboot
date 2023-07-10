package org.zamariola.bruno.eventhubcontroller.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;

@ConfigurationProperties("controllers.eventhub")
public record EventHubProperties(
    Duration resyncPeriodSeconds,
    String defaultResourceGroup,
    Map<String, String> hostAliasMapping
) {
}
