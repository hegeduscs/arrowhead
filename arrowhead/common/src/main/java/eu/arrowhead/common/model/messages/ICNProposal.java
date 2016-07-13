package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class ICNProposal {

	private ArrowheadService requestedService;
	private String authenticationInfo;
	private ArrowheadCloud requesterCloud;
	private ArrowheadSystem requesterSystem;
	private List<ArrowheadSystem> preferredSystems = new ArrayList<ArrowheadSystem>();
	
	public ICNProposal() {
	}

	public ICNProposal(ArrowheadService requestedService, String authenticationInfo, 
			ArrowheadCloud requesterCloud, ArrowheadSystem requesterSystem,
			List<ArrowheadSystem> preferredSystems) {
		this.requestedService = requestedService;
		this.authenticationInfo = authenticationInfo;
		this.requesterCloud = requesterCloud;
		this.requesterSystem = requesterSystem;
		this.preferredSystems = preferredSystems;
	}

	public ArrowheadService getRequestedService() {
		return requestedService;
	}

	public void setRequestedService(ArrowheadService requestedService) {
		this.requestedService = requestedService;
	}

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
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
	
	
}
