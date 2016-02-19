package eu.arrowhead.common.model.messages;

import java.util.HashMap;

import eu.arrowhead.common.model.ArrowheadSystem;

public class IntraCloudAuthResponse {
	
	private HashMap<ArrowheadSystem, Boolean> authorizationMap = new HashMap<ArrowheadSystem, Boolean>();

	public IntraCloudAuthResponse() {
		super();
	}

	public IntraCloudAuthResponse(HashMap<ArrowheadSystem, Boolean> authorizationMap) {
		super();
		this.authorizationMap = authorizationMap;
	}

	public HashMap<ArrowheadSystem, Boolean> getAuthorizationMap() {
		return authorizationMap;
	}

	public void setAuthorizationMap(HashMap<ArrowheadSystem, Boolean> authorizationMap) {
		this.authorizationMap = authorizationMap;
	}
	
	public boolean isPayloadUsable(){
		if(authorizationMap.isEmpty())
			return false;
		return true;
	}

}
