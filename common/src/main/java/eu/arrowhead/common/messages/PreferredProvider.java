package eu.arrowhead.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadSystem;

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
