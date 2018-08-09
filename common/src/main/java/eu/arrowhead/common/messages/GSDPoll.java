/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class GSDPoll {

  @Valid
  @NotNull
  private ArrowheadService requestedService;
  @Valid
  @NotNull
  private ArrowheadCloud requesterCloud;
  private Map<String, Boolean> registryFlags = new HashMap<>();

  public GSDPoll() {
  }

  public GSDPoll(ArrowheadService requestedService, ArrowheadCloud requesterCloud, Map<String, Boolean> registryFlags) {
    this.requestedService = requestedService;
    this.requesterCloud = requesterCloud;
    this.registryFlags = registryFlags;
  }

  public ArrowheadService getRequestedService() {
    return requestedService;
  }

  public void setRequestedService(ArrowheadService requestedService) {
    this.requestedService = requestedService;
  }

  public ArrowheadCloud getRequesterCloud() {
    return requesterCloud;
  }

  public void setRequesterCloud(ArrowheadCloud requesterCloud) {
    this.requesterCloud = requesterCloud;
  }

  public Map<String, Boolean> getRegistryFlags() {
    return registryFlags;
  }

  public void setRegistryFlags(Map<String, Boolean> registryFlags) {
    this.registryFlags = registryFlags;
  }

}
