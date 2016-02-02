package eu.arrowhead.core.authorization;

import java.util.ArrayList;
import java.util.Collection;

import eu.arrowhead.core.authorization.database.ArrowheadService;

public class InterCloudAuthEntry {
	
	private String authenticationInfo;
    private Collection<ArrowheadService> serviceList = new ArrayList<ArrowheadService>();
	
    public InterCloudAuthEntry(){
    	
    }
    
    public InterCloudAuthEntry(String authenticationInfo, Collection<ArrowheadService> serviceList) {
		super();
		this.authenticationInfo = authenticationInfo;
		this.serviceList = serviceList;
	}

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}

	public Collection<ArrowheadService> getServiceList() {
		return serviceList;
	}

	public void setServiceList(Collection<ArrowheadService> serviceList) {
		this.serviceList = serviceList;
	}
   
	
}
