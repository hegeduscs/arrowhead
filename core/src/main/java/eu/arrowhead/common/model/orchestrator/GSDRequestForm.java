package eu.arrowhead.common.model.orchestrator;

import eu.arrowhead.common.model.ArrowheadService;

public class GSDRequestForm {

	ArrowheadService RequestedService;
	
	public GSDRequestForm(){
		
	}

	public GSDRequestForm(ArrowheadService requestedService) {
		RequestedService = requestedService;
	}

	public ArrowheadService getRequestedService() {
		return RequestedService;
	}

	public void setRequestedService(ArrowheadService requestedService) {
		RequestedService = requestedService;
	}
	
	
}
