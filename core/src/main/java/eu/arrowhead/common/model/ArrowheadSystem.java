package eu.arrowhead.common.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ArrowheadSystem {
	
	private String systemGroup;
	private String systemName;
	private String IPAddress;
	private String port;
	private String authenticationInfo;
	
	public ArrowheadSystem(){
		
	}
	
	public ArrowheadSystem(String systemGroup, String systemName, 
			String iPAddress, String port, String authenticationInfo) {
		super();
		this.systemGroup = systemGroup;
		this.systemName = systemName;
		this.IPAddress = iPAddress;
		this.port = port;
		this.authenticationInfo = authenticationInfo;
	}

	public String getSystemGroup() {
		return systemGroup;
	}

	public void setSystemGroup(String systemGroup) {
		this.systemGroup = systemGroup;
	}

	public String getSystemName() {
		return systemName;
	}

	public void setSystemName(String systemName) {
		this.systemName = systemName;
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

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}
	
	
}
