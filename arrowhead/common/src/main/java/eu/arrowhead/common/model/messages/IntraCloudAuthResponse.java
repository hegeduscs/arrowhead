package eu.arrowhead.common.model.messages;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class IntraCloudAuthResponse {
	
	private HashMap<ArrowheadSystem, Boolean> authorizationState = new HashMap<ArrowheadSystem, Boolean>();

	public IntraCloudAuthResponse() {
	}

	public IntraCloudAuthResponse(HashMap<ArrowheadSystem, Boolean> authorizationState) {
		this.authorizationState = authorizationState;
	}
	
	public HashMap<ArrowheadSystem, Boolean> getAuthorizationMap() {
		return authorizationState;
	}

	public void setAuthorizationMap(HashMap<ArrowheadSystem, Boolean> authorizationState) {
		this.authorizationState = authorizationState;
	}

	public boolean isPayloadUsable(){
		if(authorizationState.isEmpty())
			return false;
		return true;
	}

}
