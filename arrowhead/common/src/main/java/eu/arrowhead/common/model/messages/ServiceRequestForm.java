package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class ServiceRequestForm {

	private ArrowheadService requestedService;
	private String requestedQoS;
	private ArrowheadSystem requesterSystem;
	private Map<String, Boolean> orchestrationFlags = new HashMap<String, Boolean>();
	private List<ArrowheadSystem> preferredSystems = new ArrayList<ArrowheadSystem>();
	private List<ArrowheadCloud> preferredClouds = new ArrayList<ArrowheadCloud>();
	
	public ServiceRequestForm (){
	}

	public ServiceRequestForm(ArrowheadService requestedService, String requestedQoS, 
			ArrowheadSystem requesterSystem, List<ArrowheadSystem> preferredSystems,
			List<ArrowheadCloud> preferredClouds) {
		this.requestedService = requestedService;
		this.requestedQoS = requestedQoS;
		this.requesterSystem = requesterSystem;
		this.preferredSystems = preferredSystems;
		this.preferredClouds = preferredClouds;
		this.orchestrationFlags.put("externalServiceRequest", false);
		this.orchestrationFlags.put("triggerInterCloud", false);
		this.orchestrationFlags.put("enableInterCloud", false);
		this.orchestrationFlags.put("metadataSearch", false);
		this.orchestrationFlags.put("pingProvider", false);
		this.orchestrationFlags.put("overrideStore", false);
		this.orchestrationFlags.put("storeOnlyActive", false);
		this.orchestrationFlags.put("matchmaking", false);
		this.orchestrationFlags.put("generateToken", false);
	}
	
	public ServiceRequestForm(ArrowheadService requestedService, String requestedQoS, 
			ArrowheadSystem requesterSystem, Map<String, Boolean> orchestrationFlags, 
			List<ArrowheadSystem> preferredSystems, List<ArrowheadCloud> preferredClouds) {
		this.requestedService = requestedService;
		this.requestedQoS = requestedQoS;
		this.requesterSystem = requesterSystem;
		this.orchestrationFlags = orchestrationFlags;
		this.preferredSystems = preferredSystems;
		this.preferredClouds = preferredClouds;
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

	public List<ArrowheadSystem> getPreferredSystems() {
		return preferredSystems;
	}

	public void setPreferredSystems(List<ArrowheadSystem> preferredSystems) {
		this.preferredSystems = preferredSystems;
	}

	public List<ArrowheadCloud> getPreferredClouds() {
		return preferredClouds;
	}

	public void setPreferredClouds(List<ArrowheadCloud> preferredClouds) {
		this.preferredClouds = preferredClouds;
	}
	
	public boolean isPayloadUsable(){
		if(requesterSystem == null || !requesterSystem.isValid())
			return false;
		return true;
	}

	
}
