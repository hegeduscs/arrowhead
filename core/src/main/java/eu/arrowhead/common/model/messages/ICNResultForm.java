package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ICNResultForm {
	
	OrchestrationResponse instructions;

	public ICNResultForm() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ICNResultForm(OrchestrationResponse instructions) {
		this.instructions = instructions;
	}
	
	public ICNResultForm(ICNEnd icnEnd) {
		this.instructions = icnEnd.getInstructions();
	}

	public OrchestrationResponse getInstructions() {
		return instructions;
	}

	public void setInstructions(OrchestrationResponse instructions) {
		this.instructions = instructions;
	}
}
