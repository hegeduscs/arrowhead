package eu.arrowhead.common.model.messages;


import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

public class OrchestrationStoreQuery {

  private ArrowheadService requestedService;
  private ArrowheadSystem requesterSystem;
  private Boolean onlyActive = false;

  public OrchestrationStoreQuery() {
  }

  public OrchestrationStoreQuery(ArrowheadService requestedService, ArrowheadSystem requesterSystem, Boolean onlyActive) {
    this.requestedService = requestedService;
    this.requesterSystem = requesterSystem;
    this.onlyActive = onlyActive;
  }

  public ArrowheadService getRequestedService() {
    return requestedService;
  }

  public void setRequestedService(ArrowheadService requestedService) {
    this.requestedService = requestedService;
  }

  public ArrowheadSystem getRequesterSystem() {
    return requesterSystem;
  }

  public void setRequesterSystem(ArrowheadSystem requesterSystem) {
    this.requesterSystem = requesterSystem;
  }

  public Boolean isOnlyActive() {
    return onlyActive;
  }

  public void setOnlyActive(Boolean onlyActive) {
    this.onlyActive = onlyActive;
  }

  public boolean isPayloadUsable() {
    return requesterSystem != null && requesterSystem.isValidForDatabase();
  }

}
