package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;

@XmlRootElement
public class ICNProposal {

	private ArrowheadService requestedService;
	private String authenticationInfo;
	private ArrowheadCloud requestedCloud;
	
	public ICNProposal() {
		super();
	}

	public ICNProposal(ArrowheadService requestedService, String authenticationInfo, ArrowheadCloud requestedCloud) {
		super();
		this.requestedService = requestedService;
		this.authenticationInfo = authenticationInfo;
		this.requestedCloud = requestedCloud;
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
	
	

	
	
}
