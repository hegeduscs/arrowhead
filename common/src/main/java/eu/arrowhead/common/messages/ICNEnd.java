/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.messages;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ICNEnd {

  @Valid
  @NotNull
  private OrchestrationForm orchestrationForm;
  private GatewayConnectionInfo gatewayConnInfo;

  public ICNEnd() {
  }

  public ICNEnd(OrchestrationForm orchestrationForm, GatewayConnectionInfo gatewayConnInfo) {
    this.orchestrationForm = orchestrationForm;
    this.gatewayConnInfo = gatewayConnInfo;
  }

  public OrchestrationForm getOrchestrationForm() {
    return orchestrationForm;
  }

  public void setOrchestrationForm(OrchestrationForm orchestrationForm) {
    this.orchestrationForm = orchestrationForm;
  }

  public GatewayConnectionInfo getGatewayConnInfo() {
    return gatewayConnInfo;
  }

  public void setGatewayConnInfo(GatewayConnectionInfo gatewayConnInfo) {
    this.gatewayConnInfo = gatewayConnInfo;
  }

}
