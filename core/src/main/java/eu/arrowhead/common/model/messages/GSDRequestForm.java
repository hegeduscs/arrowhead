package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadService;

public class GSDRequestForm {

	private ArrowheadService requestedService;

	public GSDRequestForm() {
		super();
	}

	public GSDRequestForm(ArrowheadService requestedService) {
		super();
		this.requestedService = requestedService;
	}

	public ArrowheadService getRequestedService() {
		return requestedService;
	}

	public void setRequestedService(ArrowheadService requestedService) {
		this.requestedService = requestedService;
	}

}
