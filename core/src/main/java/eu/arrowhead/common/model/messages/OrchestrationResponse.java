package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OrchestrationResponse {

	private List<OrchestrationForm> response = new ArrayList<OrchestrationForm>();
	private List<String> orchestrationURI = new ArrayList<String>();
	private int orchestrationTimeout;

	public OrchestrationResponse() {

	}

	public OrchestrationResponse(List<OrchestrationForm> response, List<String> orchestrationURI,
			int orchestrationTimeOut) {
		this.response = response;
		this.orchestrationURI = orchestrationURI;
		this.orchestrationTimeout = orchestrationTimeOut;
	}

	public List<OrchestrationForm> getResponse() {
		return response;
	}

	public void setResponse(List<OrchestrationForm> response) {
		this.response = response;
	}

	public List<String> getOrchestrationURI() {
		return orchestrationURI;
	}

	public void setOrchestrationURI(List<String> orchestrationURI) {
		this.orchestrationURI = orchestrationURI;
	}

	public int getOrchestrationTimeOut() {
		return orchestrationTimeout;
	}

	public void setOrchestrationTimeOut(int orchestrationTimeout) {
		this.orchestrationTimeout = orchestrationTimeout;
	}

}
