/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class OrchestrationForm {

  @Valid
  @NotNull
  private ArrowheadService service;
  @Valid
  @NotNull
  private ArrowheadSystem provider;
  private String serviceURI;
  private String instruction;
  private String authorizationToken;
  private String signature;
  private List<OrchestratorWarnings> warnings = new ArrayList<>();

  public OrchestrationForm() {
  }

  public OrchestrationForm(ArrowheadService service, ArrowheadSystem provider, String serviceURI) {
    this.service = service;
    this.provider = provider;
    this.serviceURI = serviceURI;
  }

  public OrchestrationForm(ArrowheadService service, ArrowheadSystem provider, String serviceURI, String instruction, String authorizationToken,
                           String signature, List<OrchestratorWarnings> warnings) {
    this.service = service;
    this.provider = provider;
    this.serviceURI = serviceURI;
    this.instruction = instruction;
    this.authorizationToken = authorizationToken;
    this.signature = signature;
    this.warnings = warnings;
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

  public List<OrchestratorWarnings> getWarnings() {
    return warnings;
  }

  public void setWarnings(List<OrchestratorWarnings> warnings) {
    this.warnings = warnings;
  }

}
