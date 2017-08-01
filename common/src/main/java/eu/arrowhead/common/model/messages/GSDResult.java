package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

public class GSDResult {

  private List<GSDAnswer> response = new ArrayList<>();

  public GSDResult() {
  }

  public GSDResult(List<GSDAnswer> response) {
    this.response = response;
  }

  public List<GSDAnswer> getResponse() {
    return response;
  }

  public void setResponse(List<GSDAnswer> response) {
    this.response = response;
  }

  public boolean isPayloadUsable() {
    return response != null && !response.isEmpty();
  }

}
