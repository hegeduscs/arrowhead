package eu.arrowhead.core.authorization;

import eu.arrowhead.core.authorization.database.ArrowheadService;
import eu.arrowhead.core.authorization.database.ArrowheadSystem;

public class IntraCloudAuthRequest {
	
	private String authenticationInfo; //consumers
	private ArrowheadService arrowheadService;
	private boolean generateToken;
	private ArrowheadSystem provider;
	
	public IntraCloudAuthRequest() {
		super();
	}
	
	public IntraCloudAuthRequest(String authenticationInfo, ArrowheadService arrowheadService, boolean generateToken,
			ArrowheadSystem provider) {
		super();
		this.authenticationInfo = authenticationInfo;
		this.arrowheadService = arrowheadService;
		this.generateToken = generateToken;
		this.provider = provider;
	}

	public ArrowheadSystem getProvider() {
		return provider;
	}

	public void setProvider(ArrowheadSystem provider) {
		this.provider = provider;
	}

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}

	public ArrowheadService getArrowheadService() {
		return arrowheadService;
	}

	public void setArrowheadService(ArrowheadService arrowheadService) {
		this.arrowheadService = arrowheadService;
	}

	public boolean isGenerateToken() {
		return generateToken;
	}

	public void setGenerateToken(boolean generateToken) {
		this.generateToken = generateToken;
	}
	
}
