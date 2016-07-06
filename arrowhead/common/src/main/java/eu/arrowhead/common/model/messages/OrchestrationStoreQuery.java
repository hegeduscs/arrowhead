package eu.arrowhead.common.model.messages;

import java.util.HashMap;
import java.util.Map;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

public class OrchestrationStoreQuery {
	
	private ArrowheadService requestedService;
	private ArrowheadSystem requesterSystem;
	//private Map<String, Boolean> queryFlags = new HashMap<String, Boolean>();
	private boolean legacyMode;
	
	public OrchestrationStoreQuery() {
	}
	
	public OrchestrationStoreQuery(ArrowheadService requestedService, ArrowheadSystem requesterSystem,
			boolean legacyMode) {
		this.requestedService = requestedService;
		this.requesterSystem = requesterSystem;
		this.legacyMode = legacyMode;
	}

	public ArrowheadService getRequestedService() {
		return requestedService;
	}

	public void setRequestedService(ArrowheadService requestedService) {
		this.requestedService = requestedService;
	}

	public ArrowheadSystem getRequesterSystem() {
		return requesterSystem;
	}

	public void setRequesterSystem(ArrowheadSystem requesterSystem) {
		this.requesterSystem = requesterSystem;
	}

	public boolean isLegacyMode() {
		return legacyMode;
	}

	public void setLegacyMode(boolean legacyMode) {
		this.legacyMode = legacyMode;
	}

	public boolean isPayloadUsable(){
		if(requestedService == null || requesterSystem == null ||
				requestedService.isValid() || requesterSystem.isValid())
			return false;
		return true;
	}
	
	
}
