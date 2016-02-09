package eu.arrowhead.common.model.messages;

import java.util.HashMap;
import java.util.Map;

import eu.arrowhead.common.model.ArrowheadSystem;

public class AuthorizationResponse {

	Map<ArrowheadSystem, Boolean> AuthorizationState = new HashMap<>();
	int ValidityPeriod;
	Map<String, String> AuthorizationTickets = new HashMap<>();

	public AuthorizationResponse() {

	}

	public AuthorizationResponse(Map<ArrowheadSystem, Boolean> authorizationState, int validityPeriod,
			Map<String, String> authorizationTickets) {
		AuthorizationState = authorizationState;
		ValidityPeriod = validityPeriod;
		AuthorizationTickets = authorizationTickets;
	}

	public Map<ArrowheadSystem, Boolean> getAuthorizationState() {
		return AuthorizationState;
	}

	public void setAuthorizationState(Map<ArrowheadSystem, Boolean> authorizationState) {
		AuthorizationState = authorizationState;
	}

	public int getValidityPeriod() {
		return ValidityPeriod;
	}

	public void setValidityPeriod(int validityPeriod) {
		ValidityPeriod = validityPeriod;
	}

	public Map<String, String> getAuthorizationTickets() {
		return AuthorizationTickets;
	}

	public void setAuthorizationTickets(Map<String, String> authorizationTickets) {
		AuthorizationTickets = authorizationTickets;
	}

}
