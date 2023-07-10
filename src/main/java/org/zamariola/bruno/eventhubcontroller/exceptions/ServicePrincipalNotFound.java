package org.zamariola.bruno.eventhubcontroller.exceptions;

public class ServicePrincipalNotFound extends RuntimeException {

  public ServicePrincipalNotFound(final String applicationId) {
    super("Could not find service principal with client-id (application-id): " + applicationId);
  }
}
