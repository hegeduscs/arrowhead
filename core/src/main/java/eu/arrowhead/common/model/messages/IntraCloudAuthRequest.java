package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.Collection;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

public class IntraCloudAuthRequest {
	
	private String authenticationInfo; //consumers
	private ArrowheadService arrowheadService;
	private boolean generateToken;
	private Collection<ArrowheadSystem> providerList = new ArrayList<ArrowheadSystem>();
	
	public IntraCloudAuthRequest() {
		super();
	}

	public IntraCloudAuthRequest(String authenticationInfo, ArrowheadService arrowheadService, boolean generateToken,
			Collection<ArrowheadSystem> providerList) {
		super();
		this.authenticationInfo = authenticationInfo;
		this.arrowheadService = arrowheadService;
		this.generateToken = generateToken;
		this.providerList = providerList;
	}

	public Collection<ArrowheadSystem> getProviderList() {
		return providerList;
	}

	public void setProviderList(Collection<ArrowheadSystem> providerList) {
		this.providerList = providerList;
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
