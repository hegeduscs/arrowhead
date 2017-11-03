package eu.arrowhead.common.messages;

import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ICNProposal {

	private ArrowheadService requestedService;
	private ArrowheadCloud requesterCloud;
	private ArrowheadSystem requesterSystem;
	private List<ArrowheadSystem> preferredSystems = new ArrayList<>();
	private Map<String, Boolean> negotiationFlags = new HashMap<>();
	private String authenticationInfo;
	private List<String> preferredBrokers;
	private int timeout;
	private PublicKey gatewayPublicKey;

	public ICNProposal() {
	}

	public ICNProposal(ArrowheadService requestedService, ArrowheadCloud requesterCloud,
			ArrowheadSystem requesterSystem, List<ArrowheadSystem> preferredSystems,
			Map<String, Boolean> negotiationFlags, String authenticationInfo, List<String> preferredBrokers,
			int timeout, PublicKey gatewayPublicKey) {
		this.requestedService = requestedService;
		this.requesterCloud = requesterCloud;
		this.requesterSystem = requesterSystem;
		this.preferredSystems = preferredSystems;
		this.negotiationFlags = negotiationFlags;
		this.authenticationInfo = authenticationInfo;
		this.preferredBrokers = preferredBrokers;
		this.timeout = timeout;
		this.gatewayPublicKey = gatewayPublicKey;
	}

	public ArrowheadService getRequestedService() {
		return requestedService;
	}

	public void setRequestedService(ArrowheadService requestedService) {
		this.requestedService = requestedService;
	}

	public ArrowheadCloud getRequesterCloud() {
		return requesterCloud;
	}

	public void setRequesterCloud(ArrowheadCloud requesterCloud) {
		this.requesterCloud = requesterCloud;
	}

	public ArrowheadSystem getRequesterSystem() {
		return requesterSystem;
	}

	public void setRequesterSystem(ArrowheadSystem requesterSystem) {
		this.requesterSystem = requesterSystem;
	}

	public List<ArrowheadSystem> getPreferredSystems() {
		return preferredSystems;
	}

	public void setPreferredSystems(List<ArrowheadSystem> preferredSystems) {
		this.preferredSystems = preferredSystems;
	}

	public Map<String, Boolean> getNegotiationFlags() {
		return negotiationFlags;
	}

	public void setNegotiationFlags(Map<String, Boolean> negotiationFlags) {
		this.negotiationFlags = negotiationFlags;
	}

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}

	public List<String> getPreferredBrokers() {
		return preferredBrokers;
	}

	public void setPreferredBrokers(List<String> preferredBrokers) {
		this.preferredBrokers = preferredBrokers;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public PublicKey getGatewayPublicKey() {
		return gatewayPublicKey;
	}

	public void setGatewayPublicKey(PublicKey gatewayPublicKey) {
		this.gatewayPublicKey = gatewayPublicKey;
	}

	public boolean isValid() {
		return requestedService != null && requesterCloud != null && requesterSystem != null
				&& requestedService.isValid() && requesterCloud.isValidForDatabase() && requesterSystem.isValid();
	}

}
