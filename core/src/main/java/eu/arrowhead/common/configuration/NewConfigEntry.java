package eu.arrowhead.common.configuration;

public class NewConfigEntry {
	private String IPAddress;
	private String port;
	private String authenticationInfo;
	private String serviceURI;
	
	public NewConfigEntry(){
		
	}
	
	public NewConfigEntry(String iPAddress, String port, String authenticationInfo, String serviceURI) {
		super();
		IPAddress = iPAddress;
		this.port = port;
		this.authenticationInfo = authenticationInfo;
		this.serviceURI = serviceURI;
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

	public String getServiceURI() {
		return serviceURI;
	}

	public void setServiceURI(String serviceURI) {
		this.serviceURI = serviceURI;
	}
	
}
