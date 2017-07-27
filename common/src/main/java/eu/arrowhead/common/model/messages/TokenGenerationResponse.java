package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

public class TokenGenerationResponse {

  private List<String> token = new ArrayList<String>();
  private List<String> signature = new ArrayList<String>();

  public TokenGenerationResponse() {
  }

  public List<String> getToken() {
    return token;
  }

  public void setToken(List<String> token) {
    this.token = token;
  }

  public List<String> getSignature() {
    return signature;
  }

  public void setSignature(List<String> signature) {
    this.signature = signature;
  }
}