package eu.arrowhead.common.model.messages;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class AuthorizationResponse {

	private Map<ArrowheadSystem, Boolean> authorizationState = new HashMap<>();
	private int validityPeriod;
	private Map<String, String> authorizationTickets = new HashMap<>();

	public AuthorizationResponse() {

	}

	public AuthorizationResponse(Map<ArrowheadSystem, Boolean> authorizationState, int validityPeriod,
			Map<String, String> authorizationTickets) {
		this.authorizationState = authorizationState;
		this.validityPeriod = validityPeriod;
		this.authorizationTickets = authorizationTickets;
	}

	public Map<ArrowheadSystem, Boolean> getAuthorizationState() {
		return authorizationState;
	}

	public void setAuthorizationState(Map<ArrowheadSystem, Boolean> authorizationState) {
		this.authorizationState = authorizationState;
	}

	public int getValidityPeriod() {
		return validityPeriod;
	}

	public void setValidityPeriod(int validityPeriod) {
		this.validityPeriod = validityPeriod;
	}

	public Map<String, String> getAuthorizationTickets() {
		return authorizationTickets;
	}

	public void setAuthorizationTickets(Map<String, String> authorizationTickets) {
		this.authorizationTickets = authorizationTickets;
	}

}
