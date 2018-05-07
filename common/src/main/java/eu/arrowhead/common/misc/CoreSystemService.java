package eu.arrowhead.common.misc;

public enum CoreSystemService {
  AUTH_CONTROL_SERVICE("AuthorizationControl", "authorization"),
  TOKEN_GEN_SERVICE("TokenGeneration", "authorization/token"),
  EVENT_PUBLISH("EventPublishing", "eventhandler/publish"),
  EVENT_SUBSCRIPTION("EventSubscription", "eventhandler/subscription"),
  GSD_SERVICE("GlobalServiceDiscovery", "gatekeeper/init_gsd"),
  ICN_SERVICE("InterCloudNegotiations", "gatekeeper/init_icn"),
  GW_CONSUMER_SERVICE("ConnectToConsumer", "gateway/connectToConsumer"),
  GW_PROVIDER_SERVICE("ConnectToProvider", "gateway/connectToProvider"),
  GW_SESSION_MGMT("SessionManagement", "gateway/management"),
  ORCH_SERVICE("OrchestrationService", "orchestrator/orchestration");

  private final String serviceDef;
  private final String serviceUri;

  CoreSystemService(String serviceDef, String serviceUri) {
    this.serviceDef = serviceDef;
    this.serviceUri = serviceUri;
  }

  public String getServiceDef() {
    return serviceDef;
  }

  public String getServiceUri() {
    return serviceUri;
  }

}
