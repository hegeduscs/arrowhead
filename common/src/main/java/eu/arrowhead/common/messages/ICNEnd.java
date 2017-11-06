package eu.arrowhead.common.messages;

public class ICNEnd {
	
	OrchestrationForm instructions;
	GatewayConnectionInfo useGateway;
	
	public ICNEnd(OrchestrationForm instructions, GatewayConnectionInfo useGateway) {
		this.instructions = instructions;
		this.useGateway = useGateway;
	}
	public OrchestrationForm getInstructions() {
		return instructions;
	}
	public void setInstructions(OrchestrationForm instructions) {
		this.instructions = instructions;
	}
	public GatewayConnectionInfo getUseGateway() {
		return useGateway;
	}
	public void setUseGateway(GatewayConnectionInfo useGateway) {
		this.useGateway = useGateway;
	}

}
