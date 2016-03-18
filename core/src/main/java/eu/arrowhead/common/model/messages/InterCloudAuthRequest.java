package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadService;

@XmlRootElement
public class InterCloudAuthRequest {
	
	private ArrowheadService service;
	private String authenticationInfo;
	private boolean generateToken;
	
	public InterCloudAuthRequest(){
	}
	
	public InterCloudAuthRequest(ArrowheadService service, String authenticationInfo, 
			boolean generateToken) {
		this.service = service;
		this.authenticationInfo = authenticationInfo;
		this.generateToken = generateToken;
	}

	public ArrowheadService getService() {
		return service;
	}

	public void setService(ArrowheadService service) {
		this.service = service;
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
		if(authenticationInfo == null || service == null)
			return false;
		return true;
	}
	
}
