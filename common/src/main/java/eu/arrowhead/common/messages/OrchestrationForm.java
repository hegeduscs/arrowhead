package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;

public class OrchestrationForm {

  private ArrowheadService service;
  private ArrowheadSystem provider;
  private String serviceURI;
  private String instruction;
  private String authorizationToken;
  private String signature;

  public OrchestrationForm() {
  }

  public OrchestrationForm(ArrowheadService service, ArrowheadSystem provider, String serviceURI) {
    this.service = service;
    this.provider = provider;
    this.serviceURI = serviceURI;
  }

  public OrchestrationForm(ArrowheadService service, ArrowheadSystem provider, String serviceURI, String instruction, String authorizationToken,
                           String signature) {
    this.service = service;
    this.provider = provider;
    this.serviceURI = serviceURI;
    this.instruction = instruction;
    this.authorizationToken = authorizationToken;
    this.signature = signature;
  }

  public ArrowheadService getService() {
    return service;
  }

  public void setService(ArrowheadService service) {
    this.service = service;
  }

  public ArrowheadSystem getProvider() {
    return provider;
  }

  public void setProvider(ArrowheadSystem provider) {
    this.provider = provider;
  }

  public String getServiceURI() {
    return serviceURI;
  }

  public void setServiceURI(String serviceURI) {
    this.serviceURI = serviceURI;
  }

  public String getInstruction() {
    return instruction;
  }

  public void setInstruction(String instruction) {
    this.instruction = instruction;
  }

  public String getAuthorizationToken() {
    return authorizationToken;
  }

  public void setAuthorizationToken(String authorizationToken) {
    this.authorizationToken = authorizationToken;
  }

  public String getSignature() {
    return signature;
  }

  public void setSignature(String signature) {
    this.signature = signature;
  }

}
