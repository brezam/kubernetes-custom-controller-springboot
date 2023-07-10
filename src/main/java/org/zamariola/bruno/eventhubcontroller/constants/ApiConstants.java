package org.zamariola.bruno.eventhubcontroller.constants;

public class ApiConstants {
  public static final String CONTROLLER_NAME = "eventhubController";
  public static final String SECRET_GENERATION_KEY = "generationRef";
  public static final String MDC_KUBE_OBJ = "kubernetesObject"; // If you change this value make sure to change in logback.xml as well

  public static final String EVENTHUB_API_GROUP = "eventhubcontroller.bruno.zamariola.org";
  public static final String EVENTHUB_API_VERSION = "v1alpha1";
  public static final String EVENTHUB_RESOURCE_PLURAL = "eventhubs";
}
