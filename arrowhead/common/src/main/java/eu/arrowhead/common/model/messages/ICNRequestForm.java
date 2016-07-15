package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

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
	private List<ArrowheadSystem> preferredProviders = new ArrayList<ArrowheadSystem>();
	
	public ICNRequestForm() {
	}

	public ICNRequestForm(ArrowheadService requestedService, String authenticationInfo,
			ArrowheadCloud targetCloud, ArrowheadSystem requesterSystem,
			List<ArrowheadSystem> preferredProviders) {
		this.requestedService = requestedService;
		this.authenticationInfo = authenticationInfo;
		this.targetCloud = targetCloud;
		this.requesterSystem = requesterSystem;
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
	
	public List<ArrowheadSystem> getPreferredProviders() {
		return preferredProviders;
	}

	public void setPreferredProviders(List<ArrowheadSystem> preferredProviders) {
		this.preferredProviders = preferredProviders;
	}

	public boolean isPayloadUsable(){
		if(requestedService == null || authenticationInfo == null 
				|| targetCloud == null || requesterSystem == null)
			return false;
		if(!requestedService.isValid() || !targetCloud.isValid() || !requesterSystem.isValid())
			return false;
		return true;
	}

}
