package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class ICNProposal {

	private ArrowheadService requestedService;
	private String authenticationInfo;
	private ArrowheadCloud requestedCloud;
	private ArrowheadSystem requesterSystem;
	
	public ICNProposal() {
		super();
	}

	public ICNProposal(ArrowheadService requestedService, String authenticationInfo, ArrowheadCloud requestedCloud,
			ArrowheadSystem requesterSystem) {
		super();
		this.requestedService = requestedService;
		this.authenticationInfo = authenticationInfo;
		this.requestedCloud = requestedCloud;
		this.requesterSystem = requesterSystem;
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

	public ArrowheadCloud getRequestedCloud() {
		return requestedCloud;
	}

	public void setRequestedCloud(ArrowheadCloud requestedCloud) {
		this.requestedCloud = requestedCloud;
	}

	public ArrowheadSystem getRequesterSystem() {
		return requesterSystem;
	}

	public void setRequesterSystem(ArrowheadSystem requesterSystem) {
		this.requesterSystem = requesterSystem;
	}


	
	

	
	
}
