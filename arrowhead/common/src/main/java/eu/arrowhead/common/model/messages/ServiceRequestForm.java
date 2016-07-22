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

	private ArrowheadSystem requesterSystem;
	private ArrowheadService requestedService;
	private String requestedQoS;
	private Map<String, Boolean> orchestrationFlags = new HashMap<String, Boolean>();
	private List<ArrowheadCloud> preferredClouds = new ArrayList<ArrowheadCloud>();
	private List<ArrowheadSystem> preferredProviders = new ArrayList<ArrowheadSystem>();
	
	public ServiceRequestForm (){
	}

	public ServiceRequestForm(ArrowheadSystem requesterSystem, ArrowheadService requestedService, 
			String requestedQoS, List<ArrowheadCloud> preferredClouds, 
			List<ArrowheadSystem> preferredProviders) {
		this.requesterSystem = requesterSystem;
		this.requestedService = requestedService;
		this.requestedQoS = requestedQoS;
		this.preferredClouds = preferredClouds;
		this.preferredProviders = preferredProviders;
		this.orchestrationFlags.put("triggerInterCloud", false);
		this.orchestrationFlags.put("externalServiceRequest", false);
		this.orchestrationFlags.put("enableInterCloud", false);
		this.orchestrationFlags.put("metadataSearch", false);
		this.orchestrationFlags.put("pingProviders", false);
		this.orchestrationFlags.put("overrideStore", false);
		this.orchestrationFlags.put("storeOnlyActive", false);
		this.orchestrationFlags.put("matchmaking", false);
		this.orchestrationFlags.put("hasPreferences", false);
		this.orchestrationFlags.put("onlyPreferred", false);
		this.orchestrationFlags.put("generateToken", false);
	}
	
	public ServiceRequestForm(ArrowheadSystem requesterSystem, ArrowheadService requestedService, 
			String requestedQoS, Map<String, Boolean> orchestrationFlags, 
			List<ArrowheadCloud> preferredClouds, List<ArrowheadSystem> preferredProviders) {
		this.requesterSystem = requesterSystem;
		this.requestedService = requestedService;
		this.requestedQoS = requestedQoS;
		this.orchestrationFlags = orchestrationFlags;
		this.preferredClouds = preferredClouds;
		this.preferredProviders = preferredProviders;
	}

	public ArrowheadSystem getRequesterSystem() {
		return requesterSystem;
	}

	public void setRequesterSystem(ArrowheadSystem requesterSystem) {
		this.requesterSystem = requesterSystem;
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

	public Map<String, Boolean> getOrchestrationFlags() {
		return orchestrationFlags;
	}

	public void setOrchestrationFlags(Map<String, Boolean> orchestrationFlags) {
		this.orchestrationFlags = orchestrationFlags;
	}

	public List<ArrowheadCloud> getPreferredClouds() {
		return preferredClouds;
	}

	public void setPreferredClouds(List<ArrowheadCloud> preferredClouds) {
		this.preferredClouds = preferredClouds;
	}

	public List<ArrowheadSystem> getPreferredProviders() {
		return preferredProviders;
	}

	public void setPreferredProviders(List<ArrowheadSystem> preferredProviders) {
		this.preferredProviders = preferredProviders;
	}

	public boolean isPayloadUsable(){
		if(requesterSystem == null || !requesterSystem.isValid() ||
				requestedService == null || !requestedService.isValid())
			return false;
		return true;
	}

	
}
