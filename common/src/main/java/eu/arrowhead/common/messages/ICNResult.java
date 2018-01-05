package eu.arrowhead.common.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ICNResult {

  private OrchestrationResponse orchResponse;

  public ICNResult() {
  }

  public ICNResult(OrchestrationResponse orchResponse) {
    this.orchResponse = orchResponse;
  }

  public OrchestrationResponse getOrchResponse() {
    return orchResponse;
  }

  public void setOrchResponse(OrchestrationResponse orchResponse) {
    this.orchResponse = orchResponse;
  }

  @JsonIgnore
  public boolean isValid() {
    return orchResponse != null && orchResponse.getResponse() != null && !orchResponse.getResponse().isEmpty();
  }

}
