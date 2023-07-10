package org.zamariola.bruno.eventhubcontroller.kubernetes.controllers;

import org.zamariola.bruno.eventhubcontroller.models.V1alpha1Eventhub;

class WorkItem {
  private final V1alpha1Eventhub object;
  private final String namespace;
  private final String name;
  private final WorkItem.Type workItemType;

  private WorkItem(V1alpha1Eventhub object, WorkItem.Type workItemType) {
    assert object.getMetadata() != null;
    this.object = object;
    this.namespace = object.getMetadata().getNamespace();
    this.name = object.getMetadata().getName();
    this.workItemType = workItemType;
  }

  static WorkItem newUpdateEvent(V1alpha1Eventhub obj) {
    return new WorkItem(obj, WorkItem.Type.UPDATE);
  }

  static WorkItem newDeleteEvent(V1alpha1Eventhub obj) {
    return new WorkItem(obj, WorkItem.Type.DELETE);
  }

  public String makeKey() {
    return namespace + "/" + name;
  }

  public V1alpha1Eventhub getObject() {
    return object;
  }

  public String getNamespace() {
    return namespace;
  }

  public String getName() {
    return name;
  }

  public WorkItem.Type workItemType() {
    return workItemType;
  }

  public enum Type {
    UPDATE, DELETE
  }

}