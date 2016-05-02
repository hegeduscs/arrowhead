package eu.arrowhead.core.authorization;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

/**
 * @author umlaufz
 */
@XmlRootElement
public class IntraCloudAuthEntry {
	
	private String address;
	private String port;
    private ArrayList<ArrowheadService> serviceList = new ArrayList<ArrowheadService>();
    private ArrayList<ArrowheadSystem> providerList = new ArrayList<ArrowheadSystem>();
    private String authenticationInfo;
	
    public IntraCloudAuthEntry(){
    }

	public IntraCloudAuthEntry(String address, String port, ArrayList<ArrowheadService> serviceList, 
			ArrayList<ArrowheadSystem> providerList, String authenticationInfo) {
		this.address = address;
		this.port = port;
		this.serviceList = serviceList;
		this.providerList = providerList;
		this.authenticationInfo = authenticationInfo;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
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
		if(address == null || port == null || authenticationInfo == null || 
				serviceList.isEmpty() || providerList.isEmpty())
			return false;
		return true;
	}

}
