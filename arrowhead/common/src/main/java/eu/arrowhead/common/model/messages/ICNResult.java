package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ICNResult {
	
	OrchestrationResponse instructions;

	public ICNResult() {
		super();
	}

	public ICNResult(OrchestrationResponse instructions) {
		this.instructions = instructions;
	}
	
	public ICNResult(ICNEnd icnEnd) {
		this.instructions = icnEnd.getInstructions();
	}

	public OrchestrationResponse getInstructions() {
		return instructions;
	}

	public void setInstructions(OrchestrationResponse instructions) {
		this.instructions = instructions;
	}
}
