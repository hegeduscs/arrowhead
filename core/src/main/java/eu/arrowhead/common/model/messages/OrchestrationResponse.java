package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OrchestrationResponse {

	private List<OrchestrationForm> response = new ArrayList<OrchestrationForm>();

	public OrchestrationResponse() {
		super();
	}

	public OrchestrationResponse(List<OrchestrationForm> response) {
		super();
		this.response = response;
	}

	public List<OrchestrationForm> getResponse() {
		return response;
	}

}
