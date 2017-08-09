package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceRequestForm {

  private ArrowheadSystem requesterSystem;
  private ArrowheadCloud requesterCloud;
  private ArrowheadService requestedService;
  private Map<String, Boolean> orchestrationFlags = new HashMap<>();
  private List<ArrowheadSystem> preferredProviders = new ArrayList<>();
  private List<ArrowheadCloud> preferredClouds = new ArrayList<>();
  private Map<String, String> requestedQoS = new HashMap<>();
  private Map<String, String> commands = new HashMap<>();
  private static List<String> flagKeys = new ArrayList<>(Arrays.asList("triggerInterCloud", "externalServiceRequest", "enableInterCloud",
                                                                       "metadataSearch", "pingProviders", "overrideStore", "matchmaking",
                                                                       "onlyPreferred", "enableQoS"));

  public ServiceRequestForm() {
    for (String key : flagKeys) {
      if (!orchestrationFlags.containsKey(key)) {
        orchestrationFlags.put(key, false);
      }
    }
  }

  public static class Builder {

    // Required parameters
    private ArrowheadSystem requesterSystem;
    // Optional parameters
    private ArrowheadCloud requesterCloud;
    private ArrowheadService requestedService;
    private Map<String, Boolean> orchestrationFlags = new HashMap<>();
    private List<ArrowheadSystem> preferredProviders = new ArrayList<>();
    private List<ArrowheadCloud> preferredClouds = new ArrayList<>();
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
        if (!orchestrationFlags.containsKey(key)) {
          flags.put(key, false);
        }
      }
      orchestrationFlags = flags;
      return this;
    }

    public Builder preferredProviders(List<ArrowheadSystem> providers) {
      preferredProviders = providers;
      return this;
    }

    public Builder preferredClouds(List<ArrowheadCloud> clouds) {
      preferredClouds = clouds;
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

  private ServiceRequestForm(Builder builder) {
    requesterSystem = builder.requesterSystem;
    requesterCloud = builder.requesterCloud;
    requestedService = builder.requestedService;
    orchestrationFlags = builder.orchestrationFlags;
    preferredProviders = builder.preferredProviders;
    preferredClouds = builder.preferredClouds;
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

  /**
   * TODO detailed javadoc comment
   */
  public boolean isValid() {
    return requesterSystem != null && requesterSystem.isValid() && (requestedService == null || requestedService.isValid()) &&
        !(orchestrationFlags.get("onlyPreferred") && preferredProviders.isEmpty()) && !(orchestrationFlags.get("enableQoS") &&
        (requestedQoS.isEmpty() || commands.isEmpty()));
  }

}
