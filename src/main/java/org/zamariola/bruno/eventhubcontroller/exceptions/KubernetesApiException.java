package org.zamariola.bruno.eventhubcontroller.exceptions;

import io.kubernetes.client.openapi.ApiException;

public class KubernetesApiException extends RuntimeException {

  public KubernetesApiException(ApiException exception) {
    super(exception.getResponseBody());
  }
}
