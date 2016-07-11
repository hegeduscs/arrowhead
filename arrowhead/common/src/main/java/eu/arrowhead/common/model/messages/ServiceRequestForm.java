package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class ServiceRequestForm {

	private ArrowheadService requestedService;
	private String requestedQoS;
	private ArrowheadSystem requesterSystem;
	private Map<String, Boolean> orchestrationFlags = new HashMap<>();
	private List<ArrowheadSystem> preferredProviders = new ArrayList<ArrowheadSystem>();
	
	public ServiceRequestForm (){
		super();
	}

	public ServiceRequestForm(ArrowheadService requestedService, String requestedQoS, 
			ArrowheadSystem requesterSystem, List<ArrowheadSystem> preferredProviders) {
		this.requestedService = requestedService;
		this.requestedQoS = requestedQoS;
		this.requesterSystem = requesterSystem;
		this.orchestrationFlags.put("externalServiceRequest", false);
		this.orchestrationFlags.put("triggerInterCloud", false);
		this.orchestrationFlags.put("metadataSearch", false);
		this.orchestrationFlags.put("pingProvider", false);
		this.orchestrationFlags.put("overrideStore", false);
		this.orchestrationFlags.put("matchmaking", false);
		this.orchestrationFlags.put("hasPreferences", false);
		this.preferredProviders = preferredProviders;
	}
	
	

	public ServiceRequestForm(ArrowheadService requestedService, String requestedQoS, 
			ArrowheadSystem requesterSystem, Map<String, Boolean> orchestrationFlags,
			List<ArrowheadSystem> preferredProviders) {
		this.requestedService = requestedService;
		this.requestedQoS = requestedQoS;
		this.requesterSystem = requesterSystem;
		this.orchestrationFlags = orchestrationFlags;
		this.preferredProviders = preferredProviders;
	}

	public ArrowheadService getRequestedService() {
		return requestedService;
	}

	public void setRequestedService(ArrowheadService requestedService) {
		this.requestedService = requestedService;
	}

	public String getRequestedQoS() {
		return requestedQoS;
	}

	public void setRequestedQoS(String requestedQoS) {
		this.requestedQoS = requestedQoS;
	}

	public ArrowheadSystem getRequesterSystem() {
		return requesterSystem;
	}

	public void setRequesterSystem(ArrowheadSystem requesterSystem) {
		this.requesterSystem = requesterSystem;
	}

	public Map<String, Boolean> getOrchestrationFlags() {
		return orchestrationFlags;
	}

	public void setOrchestrationFlags(Map<String, Boolean> orchestrationFlags) {
		this.orchestrationFlags = orchestrationFlags;
	}

	public List<ArrowheadSystem> getPreferredProviders() {
		return preferredProviders;
	}

	public void setPreferredProviders(List<ArrowheadSystem> preferredProviders) {
		this.preferredProviders = preferredProviders;
	}
	
	public boolean areFlagsSet(){
		if(!orchestrationFlags.containsKey("externalServiceRequest") ||
			!orchestrationFlags.containsKey("triggerInterCloud") ||
			!orchestrationFlags.containsKey("metadataSearch") ||
			!orchestrationFlags.containsKey("pingProvider") ||
			!orchestrationFlags.containsKey("overrideStore") ||
			!orchestrationFlags.containsKey("matchmaking") ||
			!orchestrationFlags.containsKey("hasPreferences"))
			return false;
		return true;
	}
	
}
