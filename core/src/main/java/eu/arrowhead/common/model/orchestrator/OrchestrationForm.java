package eu.arrowhead.common.model.orchestrator;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

public class OrchestrationForm {

	ArrowheadService Service;
	ArrowheadSystem Provider;
	String ServiceURI;
	String AuthorizationInfo;

	public OrchestrationForm() {

	}

	public OrchestrationForm(ArrowheadService service, ArrowheadSystem provider, String serviceURI,
			String authorizationInfo) {
		Service = service;
		Provider = provider;
		ServiceURI = serviceURI;
		AuthorizationInfo = authorizationInfo;
	}

	public ArrowheadService getService() {
		return Service;
	}

	public void setService(ArrowheadService service) {
		Service = service;
	}

	public ArrowheadSystem getProvider() {
		return Provider;
	}

	public void setProvider(ArrowheadSystem provider) {
		Provider = provider;
	}

	public String getServiceURI() {
		return ServiceURI;
	}

	public void setServiceURI(String serviceURI) {
		ServiceURI = serviceURI;
	}

	public String getAuthorizationInfo() {
		return AuthorizationInfo;
	}

	public void setAuthorizationInfo(String authorizationInfo) {
		AuthorizationInfo = authorizationInfo;
	}

}
