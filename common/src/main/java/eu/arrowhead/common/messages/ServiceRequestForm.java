/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.exception.BadPayloadException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is what the Orchestrator Core System receives from Arrowhead Systems trying to request services.
 */
@JsonIgnoreProperties({"flagKeys", "alwaysMandatoryFields"})
public class ServiceRequestForm extends ArrowheadBase {

  private static final List<String> flagKeys = new ArrayList<>(Arrays.asList("triggerInterCloud", "externalServiceRequest", "enableInterCloud",
                                                                             "metadataSearch", "pingProviders", "overrideStore", "matchmaking",
                                                                             "onlyPreferred", "enableQoS"));
  private static final Set<String> alwaysMandatoryFields = new HashSet<>(Collections.singleton("requesterSystem"));

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

  public Set<String> missingFields(boolean throwException, Set<String> mandatoryFields) {
    setOrchestrationFlags(getOrchestrationFlags());
    Set<String> mf = new HashSet<>(alwaysMandatoryFields);
    if (mandatoryFields != null) {
      mf.addAll(mandatoryFields);
    }

    Set<String> nonNullFields = getFieldNamesWithNonNullValue();
    mf.removeAll(nonNullFields);
    if (requesterSystem == null) {
      mf.add("requesterSystem");
    } else {
      mf = requesterSystem.missingFields(false, mf);
    }
    if (requestedService == null && orchestrationFlags.get("overrideStore")) {
      mf.add("requestedService can not be null when overrideStore is TRUE");
    } else if (requestedService != null) {
      mf.add("interfaces");
      mf = requestedService.missingFields(false, false, mf);
    }
    if (orchestrationFlags.get("onlyPreferred")) {
      List<PreferredProvider> tmp = new ArrayList<>();
      for (PreferredProvider provider : preferredProviders) {
        if (!provider.isValid()) {
          tmp.add(provider);
        }
      }
      preferredProviders.removeAll(tmp);
      if (preferredProviders.isEmpty()) {
        mf.add("There is no valid PreferredProvider, but \"onlyPreferred\" is set to true");
      }
    }
    if (orchestrationFlags.get("enableQoS") && (requestedQoS.isEmpty() || commands.isEmpty())) {
      mf.add("RequestedQoS or commands hashmap is empty while \"enableQoS\" is set to true");
    }

    if (throwException && !mf.isEmpty()) {
      throw new BadPayloadException("Missing mandatory fields for " + getClass().getSimpleName() + ": " + String.join(", ", mf));
    }
    return mf;
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
