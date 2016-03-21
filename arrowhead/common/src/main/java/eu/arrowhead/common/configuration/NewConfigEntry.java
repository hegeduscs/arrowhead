package eu.arrowhead.common.configuration;

/**
 * @author umlaufz
 * 
 * Plain Old Java Object for using the Configuration Resource in this package.
 * New Core Systems, Neighbor Clouds and Own Cloud objects can be added 
 * to the configuration database with this class via a REST interface.
 */
public class NewConfigEntry {
	private String IPAddress;
	private String port;
	private String authenticationInfo;
	private String serviceURI;
	
	public NewConfigEntry(){
	}
	
	public NewConfigEntry(String IPAddress, String port, String authenticationInfo, 
			String serviceURI) {
		this.IPAddress = IPAddress;
		this.port = port;
		this.authenticationInfo = authenticationInfo;
		this.serviceURI = serviceURI;
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

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}

	public String getServiceURI() {
		return serviceURI;
	}

	public void setServiceURI(String serviceURI) {
		this.serviceURI = serviceURI;
	}
	
	
}
