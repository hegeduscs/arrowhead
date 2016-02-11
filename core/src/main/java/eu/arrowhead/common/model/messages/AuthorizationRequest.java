package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class AuthorizationRequest {

	private ArrowheadService service;
	private List<ArrowheadSystem> providers = new ArrayList<ArrowheadSystem>();
	private String authenticationInfo;
	private boolean generateToken;

	public AuthorizationRequest() {

	}

	public AuthorizationRequest(ArrowheadService service, List<ArrowheadSystem> providers, String authenticationInfo,
			boolean generateToken) {
		this.service = service;
		this.providers = providers;
		this.authenticationInfo = authenticationInfo;
		this.generateToken = generateToken;
	}

	public ArrowheadService getService() {
		return service;
	}

	public void setService(ArrowheadService service) {
		this.service = service;
	}

	public List<ArrowheadSystem> getProviders() {
		return providers;
	}

	public void setProviders(List<ArrowheadSystem> providers) {
		this.providers = providers;
	}

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}

	public boolean isGenerateToken() {
		return generateToken;
	}

	public void setGenerateToken(boolean generateToken) {
		this.generateToken = generateToken;
	}

}
