package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;

public class ICNRequestForm {

	ArrowheadService RequestedService;
	String AuthenticationInfo;
	ArrowheadCloud TargetCloud;

	public ICNRequestForm() {

	}

	public ICNRequestForm(ArrowheadService requestedService, String authenticationInfo, ArrowheadCloud targetCloud) {
		RequestedService = requestedService;
		AuthenticationInfo = authenticationInfo;
		TargetCloud = targetCloud;
	}

	public ArrowheadService getRequestedService() {
		return RequestedService;
	}

	public void setRequestedService(ArrowheadService requestedService) {
		RequestedService = requestedService;
	}

	public String getAuthenticationInfo() {
		return AuthenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		AuthenticationInfo = authenticationInfo;
	}

	public ArrowheadCloud getTargetCloud() {
		return TargetCloud;
	}

	public void setTargetCloud(ArrowheadCloud targetCloud) {
		TargetCloud = targetCloud;
	}

}
