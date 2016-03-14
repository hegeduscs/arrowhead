package eu.arrowhead.core.authorization;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.core.authorization.database.ArrowheadService;
import eu.arrowhead.core.authorization.database.ArrowheadSystem;

/**
 * @author umlaufz
 */
@XmlRootElement
public class IntraCloudAuthEntry {
	
	private String IPAddress;
	private String port;
    private ArrayList<ArrowheadService> serviceList = new ArrayList<ArrowheadService>();
    private ArrayList<ArrowheadSystem> providerList = new ArrayList<ArrowheadSystem>();
    private String authenticationInfo;
	
    public IntraCloudAuthEntry(){
    }

	public IntraCloudAuthEntry(String IPAddress, String port, ArrayList<ArrowheadService> serviceList, 
			ArrayList<ArrowheadSystem> providerList, String authenticationInfo) {
		this.IPAddress = IPAddress;
		this.port = port;
		this.serviceList = serviceList;
		this.providerList = providerList;
		this.authenticationInfo = authenticationInfo;
	}

	public String getIPAddress() {
		return IPAddress;
	}

	public void setIPAddress(String IPAddress) {
		this.IPAddress = IPAddress;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public ArrayList<ArrowheadService> getServiceList() {
		return serviceList;
	}

	public void setServiceList(ArrayList<ArrowheadService> serviceList) {
		this.serviceList = serviceList;
	}

	public ArrayList<ArrowheadSystem> getProviderList() {
		return providerList;
	}

	public void setProviderList(ArrayList<ArrowheadSystem> providerList) {
		this.providerList = providerList;
	}
	
	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}
    
	public boolean isPayloadUsable(){
		if(IPAddress == null || port == null || authenticationInfo == null || 
				serviceList.isEmpty() || providerList.isEmpty())
			return false;
		return true;
	}

}
