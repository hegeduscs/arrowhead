/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.json.support;

import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.messages.PreferredProvider;
import eu.arrowhead.common.messages.ServiceRequestForm;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class ServiceRequestFormSupport {

  private ArrowheadSystemSupport requesterSystem;
  private ArrowheadCloud requesterCloud;
  private ArrowheadServiceSupport requestedService;
  @XmlJavaTypeAdapter(BooleanMapAdapter.class)
  private Map<String, Boolean> orchestrationFlags = new HashMap<>();
  private List<PreferredProviderSupport> preferredProviders = new ArrayList<>();
  @XmlJavaTypeAdapter(StringMapAdapter.class)
  private Map<String, String> requestedQoS = new HashMap<>();
  @XmlJavaTypeAdapter(StringMapAdapter.class)
  private Map<String, String> commands = new HashMap<>();

  public ServiceRequestFormSupport() {
  }

  public ServiceRequestFormSupport(ServiceRequestForm srf) {
    this.requesterSystem = new ArrowheadSystemSupport(srf.getRequesterSystem());
    this.requesterCloud = srf.getRequesterCloud();
    this.requestedService = new ArrowheadServiceSupport(srf.getRequestedService());
    this.orchestrationFlags = srf.getOrchestrationFlags();
    for (PreferredProvider provider : srf.getPreferredProviders()) {
      preferredProviders.add(new PreferredProviderSupport(provider));
    }
  }

  public ServiceRequestFormSupport(ArrowheadSystemSupport requesterSystem, ArrowheadCloud requesterCloud, ArrowheadServiceSupport requestedService,
                                   Map<String, Boolean> orchestrationFlags, List<PreferredProviderSupport> preferredProviders) {
    this.requesterSystem = requesterSystem;
    this.requesterCloud = requesterCloud;
    this.requestedService = requestedService;
    this.orchestrationFlags = orchestrationFlags;
    this.preferredProviders = preferredProviders;
  }

  public ArrowheadSystemSupport getRequesterSystem() {
    return requesterSystem;
  }

  public void setRequesterSystem(ArrowheadSystemSupport requesterSystem) {
    this.requesterSystem = requesterSystem;
  }

  public ArrowheadCloud getRequesterCloud() {
    return requesterCloud;
  }

  public void setRequesterCloud(ArrowheadCloud requesterCloud) {
    this.requesterCloud = requesterCloud;
  }

  public ArrowheadServiceSupport getRequestedService() {
    return requestedService;
  }

  public void setRequestedService(ArrowheadServiceSupport requestedService) {
    this.requestedService = requestedService;
  }

  public Map<String, Boolean> getOrchestrationFlags() {
    return orchestrationFlags;
  }

  public void setOrchestrationFlags(Map<String, Boolean> orchestrationFlags) {
    this.orchestrationFlags = orchestrationFlags;
  }

  public List<PreferredProviderSupport> getPreferredProviders() {
    return preferredProviders;
  }

  public void setPreferredProviders(List<PreferredProviderSupport> preferredProviders) {
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
}
