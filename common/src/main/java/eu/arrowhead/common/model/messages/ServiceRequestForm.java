package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is what the Orchestrator Core System receives from Arrowhead Systems trying to request services.
 */
public class ServiceRequestForm {

  private static final List<String> flagKeys = new ArrayList<>(Arrays.asList("triggerInterCloud", "externalServiceRequest", "enableInterCloud",
                                                                             "metadataSearch", "pingProviders", "overrideStore", "matchmaking",
                                                                             "onlyPreferred", "enableQoS"));
  private ArrowheadSystem requesterSystem;
  private ArrowheadCloud requesterCloud;
  private ArrowheadService requestedService;
  private Map<String, Boolean> orchestrationFlags = new HashMap<>();
  private List<PreferredProvider> preferredProviders = new ArrayList<>();
  private Map<String, String> requestedQoS = new HashMap<>();
  private Map<String, String> commands = new HashMap<>();

  public ServiceRequestForm() {
    for (String key : flagKeys) {
      if (!orchestrationFlags.containsKey(key)) {
        orchestrationFlags.put(key, false);
      }
    }
  }

  private ServiceRequestForm(Builder builder) {
    requesterSystem = builder.requesterSystem;
    requesterCloud = builder.requesterCloud;
    requestedService = builder.requestedService;
    orchestrationFlags = builder.orchestrationFlags;
    preferredProviders = builder.preferredProviders;
    requestedQoS = builder.requestedQoS;
    commands = builder.commands;
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
    for (String key : flagKeys) {
      if (!orchestrationFlags.containsKey(key)) {
        orchestrationFlags.put(key, false);
      }
    }
    this.orchestrationFlags = orchestrationFlags;
  }

  public List<PreferredProvider> getPreferredProviders() {
    return preferredProviders;
  }

  public void setPreferredProviders(List<PreferredProvider> preferredProviders) {
    this.preferredProviders = preferredProviders;
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

  /**
   * Simple inspector method to check weather a ServiceRequestForm instance is valid to be processed by the Orchestrator.
   * <p>
   * The mandatory requesterSystem field can not be null and has to be valid. If the requestedService is not null, it has to be valid.
   * PreferredProviders list can not be empty, if the onlyPreferred flag is true. RequestedQoS and commands lists can not be empty, if the enableQoS
   * flag is true.
   *
   * @return true if the instance is in compliance with all the restrictions, false otherwise
   */
  public boolean isValid() {
    return requesterSystem != null && requesterSystem.isValid() && requestedService != null && requestedService.isValid() && !(
        orchestrationFlags.get("onlyPreferred") && preferredProviders.isEmpty()) && !(orchestrationFlags.get("enableQoS") && (requestedQoS.isEmpty()
        || commands.isEmpty()));
  }

  public static class Builder {

    // Required parameters
    private ArrowheadSystem requesterSystem;
    // Optional parameters
    private ArrowheadCloud requesterCloud;
    private ArrowheadService requestedService;
    private Map<String, Boolean> orchestrationFlags = new HashMap<>();
    private List<PreferredProvider> preferredProviders = new ArrayList<>();
    private Map<String, String> requestedQoS = new HashMap<>();
    private Map<String, String> commands = new HashMap<>();

    public Builder(ArrowheadSystem requesterSystem) {
      this.requesterSystem = requesterSystem;
    }

    public Builder requesterCloud(ArrowheadCloud cloud) {
      requesterCloud = cloud;
      return this;
    }

    public Builder requestedService(ArrowheadService service) {
      requestedService = service;
      return this;
    }

    public Builder orchestrationFlags(Map<String, Boolean> flags) {
      for (String key : flagKeys) {
        if (!flags.containsKey(key)) {
          flags.put(key, false);
        }
      }
      orchestrationFlags = flags;
      return this;
    }

    public Builder preferredProviders(List<PreferredProvider> providers) {
      preferredProviders = providers;
      return this;
    }

    public Builder requestedQoS(Map<String, String> qos) {
      requestedQoS = qos;
      return this;
    }

    public Builder commands(Map<String, String> commands) {
      this.commands = commands;
      return this;
    }

    public ServiceRequestForm build() {
      return new ServiceRequestForm(this);
    }
  }

}
