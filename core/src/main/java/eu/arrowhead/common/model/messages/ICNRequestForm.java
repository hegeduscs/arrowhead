package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;

public class ICNRequestForm {

	private ArrowheadService requestedService;
	private String authenticationInfo;
	private ArrowheadCloud targetCloud;

	public ICNRequestForm() {
		super();
	}

	public ICNRequestForm(ArrowheadService requestedService, String authenticationInfo, ArrowheadCloud targetCloud) {
		super();
		this.requestedService = requestedService;
		this.authenticationInfo = authenticationInfo;
		this.targetCloud = targetCloud;
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

	public ArrowheadCloud getTargetCloud() {
		return targetCloud;
	}

	public void setTargetCloud(ArrowheadCloud targetCloud) {
		this.targetCloud = targetCloud;
	}

}
