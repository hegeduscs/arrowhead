package eu.arrowhead.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;

public class GSDAnswer {

  private ArrowheadService requestedService;
  private ArrowheadCloud providerCloud;

  public GSDAnswer() {
  }

  public GSDAnswer(ArrowheadService requestedService, ArrowheadCloud providerCloud) {
    this.requestedService = requestedService;
    this.providerCloud = providerCloud;
  }

  public ArrowheadService getRequestedService() {
    return requestedService;
  }

  public void setRequestedService(ArrowheadService requestedService) {
    this.requestedService = requestedService;
  }

  public ArrowheadCloud getProviderCloud() {
    return providerCloud;
  }

  public void setProviderCloud(ArrowheadCloud providerCloud) {
    this.providerCloud = providerCloud;
  }

  @JsonIgnore
  public boolean isValid() {
    return requestedService.isValidForDatabase() && providerCloud.isValid();
  }

}
