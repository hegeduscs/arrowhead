package eu.arrowhead.core.authorization;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadService;

/**
 * @author umlaufz
 */
@XmlRootElement
public class InterCloudAuthEntry {
	
    private Collection<ArrowheadService> serviceList = new ArrayList<ArrowheadService>();
    private String authenticationInfo;
	
    public InterCloudAuthEntry(){
    }
    
    public InterCloudAuthEntry(Collection<ArrowheadService> serviceList, String authenticationInfo) {
		this.serviceList = serviceList;
		this.authenticationInfo = authenticationInfo;
	}

	public Collection<ArrowheadService> getServiceList() {
		return serviceList;
	}

	public void setServiceList(Collection<ArrowheadService> serviceList) {
		this.serviceList = serviceList;
	}
	
	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}
   
	public boolean isPayloadUsable(){
		if(serviceList.isEmpty() || authenticationInfo == null)
			return false;
		return true;
	}
	
	
}
