package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadSystem;

public class PreferredProvider {

  private ArrowheadSystem providerSystem;
  private ArrowheadCloud providerCloud;

  public PreferredProvider() {
  }

  public PreferredProvider(ArrowheadSystem providerSystem, ArrowheadCloud providerCloud) {
    this.providerSystem = providerSystem;
    this.providerCloud = providerCloud;
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

  public boolean isValid() {
    return isLocal() || isGlobal();
  }

  public boolean isLocal() {
    return providerSystem != null && providerSystem.isValid() && providerCloud == null;
  }

  public boolean isGlobal() {
    return providerCloud != null && providerCloud.isValid();
  }

}
