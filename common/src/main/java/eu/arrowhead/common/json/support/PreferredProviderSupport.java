/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.json.support;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.messages.PreferredProvider;

public class PreferredProviderSupport {

  private ArrowheadSystemSupport providerSystem;
  private ArrowheadCloud providerCloud;

  public PreferredProviderSupport() {
  }

  public PreferredProviderSupport(PreferredProvider provider) {
    this.providerSystem = new ArrowheadSystemSupport(provider.getProviderSystem());
    this.providerCloud = provider.getProviderCloud();
  }

  public PreferredProviderSupport(ArrowheadSystemSupport providerSystem, ArrowheadCloud providerCloud) {
    this.providerSystem = providerSystem;
    this.providerCloud = providerCloud;
  }

  public ArrowheadSystemSupport getProviderSystem() {
    return providerSystem;
  }

  public void setProviderSystem(ArrowheadSystemSupport providerSystem) {
    this.providerSystem = providerSystem;
  }

  public ArrowheadCloud getProviderCloud() {
    return providerCloud;
  }

  public void setProviderCloud(ArrowheadCloud providerCloud) {
    this.providerCloud = providerCloud;
  }

  @JsonIgnore
  public boolean isValid() {
    return isLocal() || isGlobal();
  }

  @JsonIgnore
  public boolean isLocal() {
    return providerSystem != null && providerSystem.isValid() && providerCloud == null;
  }

  @JsonIgnore
  public boolean isGlobal() {
    return providerCloud != null && providerCloud.isValid();
  }

}
