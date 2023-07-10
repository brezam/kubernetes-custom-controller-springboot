package org.zamariola.bruno.eventhubcontroller.kubernetes.recorder;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.extended.event.EventType;
import io.kubernetes.client.extended.event.legacy.EventBroadcaster;
import io.kubernetes.client.extended.event.legacy.EventRecorder;
import io.kubernetes.client.openapi.models.V1EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zamariola.bruno.eventhubcontroller.constants.ApiConstants;

public class EventHubRecorder {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventHubRecorder.class);

  private final EventRecorder eventRecorder;

  public EventHubRecorder(EventBroadcaster eventBroadcaster) {
    this.eventRecorder = eventBroadcaster.newRecorder(new V1EventSource().host("localhost").component(ApiConstants.CONTROLLER_NAME));
  }

  public void info(KubernetesObject obj, String reason, String message, String... args) {
    LOGGER.info(reason + ": " + String.format(message, (Object[]) args));
    if (obj != null) eventRecorder.event(obj, EventType.Normal, reason, message, args);
  }

  public void warn(KubernetesObject obj, String reason, String message, String... args) {
    LOGGER.warn(reason + ": " + String.format(message, (Object[]) args));
    if (obj != null) eventRecorder.event(obj, EventType.Warning, reason, message, args);
  }

  public void error(KubernetesObject obj, Exception exception) {
    LOGGER.error("Error", exception);
    if (obj != null) eventRecorder.event(obj, EventType.Warning, "Error", exception.getMessage());
  }

}
