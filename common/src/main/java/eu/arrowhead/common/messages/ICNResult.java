package eu.arrowhead.common.messages;

public class ICNResult {

	OrchestrationResponse orchResponse;

	public ICNResult(OrchestrationResponse orchResponse) {
		this.orchResponse = orchResponse;
	}

	public OrchestrationResponse getOrchResponse() {
		return orchResponse;
	}

	public void setOrchResponse(OrchestrationResponse orchResponse) {
		this.orchResponse = orchResponse;
	}

	public boolean isValid() {
	    return orchResponse != null && orchResponse.getResponse() != null && !orchResponse.getResponse().isEmpty();
	  }
	
}
