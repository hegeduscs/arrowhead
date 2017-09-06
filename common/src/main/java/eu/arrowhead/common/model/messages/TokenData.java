package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadSystem;

public class TokenData {

  private ArrowheadSystem system;
  private String token;
  private String signature;

  public TokenData() {
  }

  public TokenData(ArrowheadSystem system, String token, String signature) {
    this.system = system;
    this.token = token;
    this.signature = signature;
  }

  public ArrowheadSystem getSystem() {
    return system;
  }

  public void setSystem(ArrowheadSystem system) {
    this.system = system;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

  //Because of Csaba :P

}
