package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

public class OrchestrationResponse {

	List<OrchestrationForm> Response = new ArrayList<OrchestrationForm>();
	List<String> OrchestrationURI = new ArrayList<String>();
	int OrchestrationTimeOut;

	public OrchestrationResponse() {

	}

	public OrchestrationResponse(List<OrchestrationForm> response, List<String> orchestrationURI,
			int orchestrationTimeOut) {
		Response = response;
		OrchestrationURI = orchestrationURI;
		OrchestrationTimeOut = orchestrationTimeOut;
	}

	public List<OrchestrationForm> getResponse() {
		return Response;
	}

	public void setResponse(List<OrchestrationForm> response) {
		Response = response;
	}

	public List<String> getOrchestrationURI() {
		return OrchestrationURI;
	}

	public void setOrchestrationURI(List<String> orchestrationURI) {
		OrchestrationURI = orchestrationURI;
	}

	public int getOrchestrationTimeOut() {
		return OrchestrationTimeOut;
	}

	public void setOrchestrationTimeOut(int orchestrationTimeOut) {
		OrchestrationTimeOut = orchestrationTimeOut;
	}

}
