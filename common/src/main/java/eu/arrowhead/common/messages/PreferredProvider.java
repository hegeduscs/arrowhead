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
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.json.support.PreferredProviderSupport;
import javax.validation.Valid;

public class PreferredProvider {

  @Valid
  private ArrowheadSystem providerSystem;
  @Valid
  private ArrowheadCloud providerCloud;

  public PreferredProvider() {
  }

  public PreferredProvider(ArrowheadSystem providerSystem, ArrowheadCloud providerCloud) {
    this.providerSystem = providerSystem;
    this.providerCloud = providerCloud;
  }

  public PreferredProvider(PreferredProviderSupport supportProvider) {
    this.providerSystem = new ArrowheadSystem(supportProvider.getProviderSystem());
    this.providerCloud = supportProvider.getProviderCloud();
  }

  public ArrowheadSystem getProviderSystem() {
    return providerSystem;
  }

  public void setProviderSystem(ArrowheadSystem providerSystem) {
    this.providerSystem = providerSystem;
  }

  public ArrowheadCloud getProviderCloud() {
    return providerCloud;
  }

  public void setProviderCloud(ArrowheadCloud providerCloud) {
    this.providerCloud = providerCloud;
  }

  public boolean isLocal() {
    return providerSystem != null && providerCloud == null;
  }

  public boolean isGlobal() {
    return providerCloud != null;
  }

  public boolean isValid() {
    return isLocal() || isGlobal();
  }

  @Override
  public String toString() {
    return "PreferredProvider{" + "providerSystem=" + providerSystem + ", providerCloud=" + providerCloud + '}';
  }

}
