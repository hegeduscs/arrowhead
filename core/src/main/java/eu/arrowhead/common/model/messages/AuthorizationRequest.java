package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

public class AuthorizationRequest {

	ArrowheadService Service;
	List<ArrowheadSystem> Providers = new ArrayList<ArrowheadSystem>();
	String AuthenticationInfo;
	boolean GenerateToken;

	public AuthorizationRequest() {

	}

	public AuthorizationRequest(ArrowheadService service, List<ArrowheadSystem> providers, String authenticationInfo,
			boolean generateToken) {
		Service = service;
		Providers = providers;
		AuthenticationInfo = authenticationInfo;
		GenerateToken = generateToken;
	}

	public ArrowheadService getService() {
		return Service;
	}

	public void setService(ArrowheadService service) {
		Service = service;
	}

	public List<ArrowheadSystem> getProviders() {
		return Providers;
	}

	public void setProviders(List<ArrowheadSystem> providers) {
		Providers = providers;
	}

	public String getAuthenticationInfo() {
		return AuthenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		AuthenticationInfo = authenticationInfo;
	}

	public boolean isGenerateToken() {
		return GenerateToken;
	}

	public void setGenerateToken(boolean generateToken) {
		GenerateToken = generateToken;
	}

}
