package eu.arrowhead.core.authorization;

import java.util.HashMap;

import eu.arrowhead.core.authorization.database.ArrowheadSystem;

public class IntraCloudAuthResp {
	
	private HashMap<ArrowheadSystem, Boolean> authorizationMap = new HashMap<ArrowheadSystem, Boolean>();

	
	public IntraCloudAuthResp() {
		super();
	}


	public IntraCloudAuthResp(HashMap<ArrowheadSystem, Boolean> authorizationMap) {
		super();
		this.authorizationMap = authorizationMap;
	}


	public HashMap<ArrowheadSystem, Boolean> getAuthorizationMap() {
		return authorizationMap;
	}


	public void setAuthorizationMap(HashMap<ArrowheadSystem, Boolean> authorizationMap) {
		this.authorizationMap = authorizationMap;
	}

}
