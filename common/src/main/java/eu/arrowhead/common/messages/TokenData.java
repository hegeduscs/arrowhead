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
import javax.validation.Valid;

public class TokenData {

  @Valid
  private ArrowheadSystem system;
  @Valid
  private ArrowheadService service;
  private String token;
  private String signature;

  public TokenData() {
  }

  public TokenData(ArrowheadSystem system, ArrowheadService service, String token, String signature) {
    this.system = system;
    this.service = service;
    this.token = token;
    this.signature = signature;
  }

  public ArrowheadSystem getSystem() {
    return system;
  }

  public void setSystem(ArrowheadSystem system) {
    this.system = system;
  }

  public ArrowheadService getService() {
    return service;
  }

  public void setService(ArrowheadService service) {
    this.service = service;
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

}
