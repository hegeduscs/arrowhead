package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class ICNRequestForm {

	private ArrowheadService requestedService;
	private String authenticationInfo;
	private ArrowheadCloud targetCloud;
	private ArrowheadSystem requesterSystem;
	
	

	public ICNRequestForm() {
		super();
	}

	
	public ICNRequestForm(ArrowheadService requestedService, String authenticationInfo, ArrowheadCloud targetCloud,
			ArrowheadSystem requesterSystem) {
		super();
		this.requestedService = requestedService;
		this.authenticationInfo = authenticationInfo;
		this.targetCloud = targetCloud;
		this.requesterSystem = requesterSystem;
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

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}

	public ArrowheadCloud getTargetCloud() {
		return targetCloud;
	}

	public void setTargetCloud(ArrowheadCloud targetCloud) {
		this.targetCloud = targetCloud;
	}

}
