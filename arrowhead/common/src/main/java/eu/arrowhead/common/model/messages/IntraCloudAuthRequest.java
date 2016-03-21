package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class IntraCloudAuthRequest {
	
	private ArrowheadService service;
	private Collection<ArrowheadSystem> providers = new ArrayList<ArrowheadSystem>();
	private String authenticationInfo; //consumers
	private boolean generateToken;	
	
	public IntraCloudAuthRequest() {
	}

	public IntraCloudAuthRequest(ArrowheadService service, Collection<ArrowheadSystem> providers,
			String authenticationInfo, boolean generateToken) {
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

	public Collection<ArrowheadSystem> getProviders() {
		return providers;
	}

	public void setProviders(Collection<ArrowheadSystem> providers) {
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

	public boolean isPayloadUsable(){
		if(authenticationInfo == null|| service == null || providers.isEmpty())
			return false;
		return true;
	}
	
	
}
