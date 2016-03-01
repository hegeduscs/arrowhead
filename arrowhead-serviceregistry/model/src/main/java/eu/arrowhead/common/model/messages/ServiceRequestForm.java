package eu.arrowhead.common.model.messages;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class ServiceRequestForm {

	ArrowheadService RequestedService;
	String RequestedQoS;
	ArrowheadSystem RequesterSystem;
	int ServicingLength;
	Map<String, Boolean> OrchestrationFlags = new HashMap<>();
	
	// TODO
	
	public ServiceRequestForm (){
		
	}

	public ServiceRequestForm(ArrowheadService requestedService, String requestedQoS, ArrowheadSystem requesterSystem,
			int servicingLength) {
		RequestedService = requestedService;
		RequestedQoS = requestedQoS;
		RequesterSystem = requesterSystem;
		ServicingLength = servicingLength;
		OrchestrationFlags.put("Matchmaking", false);
		OrchestrationFlags.put("ExternalServiceRequest", false);
		OrchestrationFlags.put("TriggerInterCloud", false);
		OrchestrationFlags.put("MetadataSearch", false);
		OrchestrationFlags.put("PingProvider", false);
	}
	
	

	public ServiceRequestForm(ArrowheadService requestedService, String requestedQoS, ArrowheadSystem requesterSystem,
		int servicingLength, Map<String, Boolean> orchestrationFlags) {
		RequestedService = requestedService;
		RequestedQoS = requestedQoS;
		RequesterSystem = requesterSystem;
		ServicingLength = servicingLength;
		OrchestrationFlags = orchestrationFlags;
	}

	public ArrowheadService getRequestedService() {
		return RequestedService;
	}

	public void setRequestedService(ArrowheadService requestedService) {
		RequestedService = requestedService;
	}

	public String getRequestedQoS() {
		return RequestedQoS;
	}

	public void setRequestedQoS(String requestedQoS) {
		RequestedQoS = requestedQoS;
	}

	public ArrowheadSystem getRequesterSystem() {
		return RequesterSystem;
	}

	public void setRequesterSystem(ArrowheadSystem requesterSystem) {
		RequesterSystem = requesterSystem;
	}

	public int getServicingLength() {
		return ServicingLength;
	}

	public void setServicingLength(int servicingLength) {
		ServicingLength = servicingLength;
	}

	public Map<String, Boolean> getOrchestrationFlags() {
		return OrchestrationFlags;
	}

	public void setOrchestrationFlags(Map<String, Boolean> orchestrationFlags) {
		OrchestrationFlags = orchestrationFlags;
	}
	
	

}
