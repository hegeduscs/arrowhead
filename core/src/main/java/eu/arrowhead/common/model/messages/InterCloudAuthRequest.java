package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadService;

@XmlRootElement
public class InterCloudAuthRequest {
	
	private String authenticationInfo;
	private ArrowheadService arrowheadService;
	private boolean generateToken;
	
	public InterCloudAuthRequest(){
		
	}
	
	public InterCloudAuthRequest(String authenticationInfo, ArrowheadService arrowheadService, boolean generateToken) {
		super();
		this.authenticationInfo = authenticationInfo;
		this.arrowheadService = arrowheadService;
		this.generateToken = generateToken;
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
	
	public boolean isPayloadUsable(){
		if(authenticationInfo == null || arrowheadService == null)
			return false;
		return true;
	}
	
}
