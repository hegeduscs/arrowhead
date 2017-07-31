package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServiceRequestForm {

  private ArrowheadSystem requesterSystem;
  private ArrowheadCloud requesterCloud;
  private ArrowheadService requestedService;
  private Map<String, Boolean> orchestrationFlags = new HashMap<>();
  private List<ArrowheadSystem> preferredProviders = new ArrayList<>();
  private List<ArrowheadCloud> preferredClouds = new ArrayList<>();
  private Map<String, String> requestedQoS = new HashMap<>();
  private Map<String, String> commands = new HashMap<>();

  public ServiceRequestForm() {
    this.orchestrationFlags.put("triggerInterCloud", false);
    this.orchestrationFlags.put("externalServiceRequest", false);
    this.orchestrationFlags.put("enableInterCloud", false);
    this.orchestrationFlags.put("metadataSearch", false);
    this.orchestrationFlags.put("pingProviders", false);
    this.orchestrationFlags.put("overrideStore", false);
    this.orchestrationFlags.put("storeOnlyActive", false);
    this.orchestrationFlags.put("matchmaking", false);
    this.orchestrationFlags.put("onlyPreferred", false);
  }

  public ServiceRequestForm(ArrowheadSystem requesterSystem, ArrowheadCloud requesterCloud, ArrowheadService requestedService,
                            Map<String, Boolean> orchestrationFlags, List<ArrowheadSystem> preferredProviders, List<ArrowheadCloud> preferredClouds,
                            Map<String, String> requestedQoS, Map<String, String> commands) {
    this.requesterSystem = requesterSystem;
    this.requesterCloud = requesterCloud;
    this.requestedService = requestedService;
    this.orchestrationFlags = orchestrationFlags;
    this.preferredProviders = preferredProviders;
    this.preferredClouds = preferredClouds;
    this.requestedQoS = requestedQoS;
    this.commands = commands;
  }

  public ServiceRequestForm(ArrowheadSystem requesterSystem, ArrowheadCloud requesterCloud, ArrowheadService requestedService,
                            List<ArrowheadSystem> preferredProviders, List<ArrowheadCloud> preferredClouds, Map<String, String> requestedQoS,
                            Map<String, String> commands) {
    this.requesterSystem = requesterSystem;
    this.requesterCloud = requesterCloud;
    this.requestedService = requestedService;
    this.preferredProviders = preferredProviders;
    this.preferredClouds = preferredClouds;
    this.requestedQoS = requestedQoS;
    this.commands = commands;
    this.orchestrationFlags.put("triggerInterCloud", false);
    this.orchestrationFlags.put("externalServiceRequest", false);
    this.orchestrationFlags.put("enableInterCloud", false);
    this.orchestrationFlags.put("metadataSearch", false);
    this.orchestrationFlags.put("pingProviders", false);
    this.orchestrationFlags.put("overrideStore", false);
    this.orchestrationFlags.put("storeOnlyActive", false);
    this.orchestrationFlags.put("matchmaking", false);
    this.orchestrationFlags.put("onlyPreferred", false);
  }

  public ArrowheadSystem getRequesterSystem() {
    return requesterSystem;
  }

  public void setRequesterSystem(ArrowheadSystem requesterSystem) {
    this.requesterSystem = requesterSystem;
  }

  public ArrowheadCloud getRequesterCloud() {
    return requesterCloud;
  }

  public void setRequesterCloud(ArrowheadCloud requesterCloud) {
    this.requesterCloud = requesterCloud;
  }

  public ArrowheadService getRequestedService() {
    return requestedService;
  }

  public void setRequestedService(ArrowheadService requestedService) {
    this.requestedService = requestedService;
  }

  public Map<String, Boolean> getOrchestrationFlags() {
    return orchestrationFlags;
  }

  public void setOrchestrationFlags(Map<String, Boolean> orchestrationFlags) {
    this.orchestrationFlags = orchestrationFlags;
  }

  public List<ArrowheadSystem> getPreferredProviders() {
    return preferredProviders;
  }

  public void setPreferredProviders(List<ArrowheadSystem> preferredProviders) {
    this.preferredProviders = preferredProviders;
  }

  public List<ArrowheadCloud> getPreferredClouds() {
    return preferredClouds;
  }

  public void setPreferredClouds(List<ArrowheadCloud> preferredClouds) {
    this.preferredClouds = preferredClouds;
  }

  public Map<String, String> getRequestedQoS() {
    return requestedQoS;
  }

  public void setRequestedQoS(Map<String, String> requestedQoS) {
    this.requestedQoS = requestedQoS;
  }

  public Map<String, String> getCommands() {
    return commands;
  }

  public void setCommands(Map<String, String> commands) {
    this.commands = commands;
  }

  public boolean isPayloadUsable() {
    if (requesterSystem == null || !requesterSystem.isValid()) {
      return false;
    }
    if (!orchestrationFlags.get("storeOnlyActive") && (requestedService == null || !requestedService.isValidStrict())) {
      return false;
    }
    return !orchestrationFlags.get("onlyPreferred") || !preferredProviders.isEmpty();
  }

}
