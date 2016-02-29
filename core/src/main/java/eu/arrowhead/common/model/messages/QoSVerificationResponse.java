package eu.arrowhead.common.model.messages;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class QoSVerificationResponse {

	private Map<ArrowheadSystem, Boolean> response = new HashMap<>();
	private Map<ArrowheadSystem, String> rejectMotivation = new HashMap<>();

	public QoSVerificationResponse() {
		super();
	}

	public QoSVerificationResponse(Map<ArrowheadSystem, Boolean> response, Map<ArrowheadSystem, String> reject) {
		super();
		this.response = response;
		this.rejectMotivation = reject;
	}

	public Map<ArrowheadSystem, Boolean> getResponse() {
		return response;
	}

	public void setResponse(Map<ArrowheadSystem, Boolean> response) {
		this.response = response;
	}

	public Map<ArrowheadSystem, String> getRejectMotivation() {
		return rejectMotivation;
	}

	public void setRejectMotivation(Map<ArrowheadSystem, String> rejectMotivation) {
		this.rejectMotivation = rejectMotivation;
	}

}
