package org.zamariola.bruno.eventhubcontroller.constants;

public class EventConstants {
  public static final String WRONG_INSTANCE_TYPE_REASON = "ErrWrongInstanceType";
  public static final String WRONG_INSTANCE_TYPE_MSG = "Event hub with instance type %s not found";

  public static final String EVENTHUB_NOT_FOUND_REASON = "ErrEventHubNotFound";
  public static final String EVENTHUB_NOT_FOUND_MSG = "Event hub namespace %s in resource group %s not found";

  public static final String SECRET_REASON = "Secret";
  public static final String SECRET_MSG = "Secret %s created";

  public static final String SYNCED_REASON = "Synced";
  public static final String SYNCED_MSG = "Event hub namespace object synced successfully";

}
