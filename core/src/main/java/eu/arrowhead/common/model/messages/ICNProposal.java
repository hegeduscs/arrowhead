package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadService;

public class ICNProposal {

	private ArrowheadService requestedService;
	private String authenticationInfo;
	
	public ICNProposal() {
		super();
	}
	
	public ICNProposal(ArrowheadService requestedService,
			String authenticationInfo) {
		super();
		this.requestedService = requestedService;
		this.authenticationInfo = authenticationInfo;
	}

	public ArrowheadService getRequestedService() {
		return requestedService;
	}

	public void setRequestedService(ArrowheadService requestedService) {
		this.requestedService = requestedService;
	}

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}

	
	
}
