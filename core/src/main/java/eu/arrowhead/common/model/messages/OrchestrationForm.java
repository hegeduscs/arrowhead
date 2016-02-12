package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class OrchestrationForm {

	private ArrowheadService service;
	private ArrowheadSystem provider;
	private String serviceURI;
	private String authorizationInfo;

	public OrchestrationForm() {
		super();
	}

	public OrchestrationForm(ArrowheadService service, ArrowheadSystem provider, String serviceURI,
			String authorizationInfo) {
		super();
		this.service = service;
		this.provider = provider;
		this.serviceURI = serviceURI;
		this.authorizationInfo = authorizationInfo;
	}

	public ArrowheadService getService() {
		return service;
	}

	public void setService(ArrowheadService service) {
		this.service = service;
	}

	public ArrowheadSystem getProvider() {
		return provider;
	}

	public void setProvider(ArrowheadSystem provider) {
		this.provider = provider;
	}

	public String getServiceURI() {
		return serviceURI;
	}

	public void setServiceURI(String serviceURI) {
		this.serviceURI = serviceURI;
	}

	public String getAuthorizationInfo() {
		return authorizationInfo;
	}

	public void setAuthorizationInfo(String authorizationInfo) {
		this.authorizationInfo = authorizationInfo;
	}

}
