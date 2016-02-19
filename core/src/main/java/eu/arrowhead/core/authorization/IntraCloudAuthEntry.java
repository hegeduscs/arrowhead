package eu.arrowhead.core.authorization;

import java.util.ArrayList;

import eu.arrowhead.core.authorization.database.ArrowheadService;
import eu.arrowhead.core.authorization.database.ArrowheadSystem;

public class IntraCloudAuthEntry {
	
	private String authenticationInfo;
	private String IPAddress;
	private String port;
    private ArrayList<ArrowheadService> serviceList = new ArrayList<ArrowheadService>();
    private ArrayList<ArrowheadSystem> providerList = new ArrayList<ArrowheadSystem>();
	
    public IntraCloudAuthEntry(){
    	
    }

	public IntraCloudAuthEntry(String authenticationInfo, String iPAddress, String port,
			ArrayList<ArrowheadService> serviceList, ArrayList<ArrowheadSystem> providerList) {
		super();
		this.authenticationInfo = authenticationInfo;
		this.IPAddress = iPAddress;
		this.port = port;
		this.serviceList = serviceList;
		this.providerList = providerList;
	}

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}

	public String getIPAddress() {
		return IPAddress;
	}

	public void setIPAddress(String iPAddress) {
		IPAddress = iPAddress;
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
    
	public boolean isPayloadUsable(){
		if(authenticationInfo == null || serviceList.isEmpty() || providerList.isEmpty()
				|| IPAddress.isEmpty() || port.isEmpty())
			return false;
		return true;
	}

}
