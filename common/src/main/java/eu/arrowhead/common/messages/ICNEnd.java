package eu.arrowhead.common.messages;

public class ICNEnd {

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
