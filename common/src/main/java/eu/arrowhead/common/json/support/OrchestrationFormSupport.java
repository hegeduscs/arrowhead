/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.json.support;

import eu.arrowhead.common.messages.OrchestrationForm;

public class OrchestrationFormSupport {

  private ArrowheadServiceSupport service;
  private ArrowheadSystemSupport provider;
  private String serviceURI;
  private String instruction;
  private String authorizationToken;
  private String signature;

  public OrchestrationFormSupport() {
  }

  public OrchestrationFormSupport(ArrowheadServiceSupport service, ArrowheadSystemSupport provider, String serviceURI) {
    this.service = service;
    this.provider = provider;
    this.serviceURI = serviceURI;
  }

  public OrchestrationFormSupport(ArrowheadServiceSupport service, ArrowheadSystemSupport provider, String serviceURI, String instruction, String
      authorizationToken, String signature) {
    this.service = service;
    this.provider = provider;
    this.serviceURI = serviceURI;
    this.instruction = instruction;
    this.authorizationToken = authorizationToken;
    this.signature = signature;
  }

  public OrchestrationFormSupport(OrchestrationForm form) {
    this.service = new ArrowheadServiceSupport(form.getService());
    this.provider = new ArrowheadSystemSupport(form.getProvider());
    this.serviceURI = form.getServiceURI();
    this.instruction = form.getInstruction();
    this.authorizationToken = form.getAuthorizationToken();
    this.signature = form.getSignature();
  }

  public ArrowheadServiceSupport getService() {
    return service;
  }

  public void setService(ArrowheadServiceSupport service) {
    this.service = service;
  }

  public ArrowheadSystemSupport getProvider() {
    return provider;
  }

  public void setProvider(ArrowheadSystemSupport provider) {
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
