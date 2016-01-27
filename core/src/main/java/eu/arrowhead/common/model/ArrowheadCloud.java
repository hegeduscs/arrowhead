package eu.arrowhead.common.model;

public class ArrowheadCloud {
	
	private String operator;
	private String cloudName;
	private String gatekeeperIP;
	private String gatekeeperPort;
	private String gatekeeperURI;
	private String authenticationInfo;
	
	public ArrowheadCloud(){
		
	}
	
	public ArrowheadCloud(String operator, String cloudName, String gatekeeperIP,
			String gatekeeperPort, String gatekeeperURI, String authenticationInfo) {
		this.operator = operator;
		this.cloudName = cloudName;
		this.gatekeeperIP = gatekeeperIP;
		this.gatekeeperPort = gatekeeperPort;
		this.gatekeeperURI = gatekeeperURI;
		this.authenticationInfo = authenticationInfo;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getName() {
		return cloudName;
	}
	public void setName(String cloudName) {
		this.cloudName = cloudName;
	}
	public String getGatekeeperIP() {
		return gatekeeperIP;
	}
	public void setGatekeeperIP(String gatekeeperIP) {
		this.gatekeeperIP = gatekeeperIP;
	}
	public String getGatekeeperPort() {
		return gatekeeperPort;
	}
	public void setGatekeeperPort(String gatekeeperPort) {
		this.gatekeeperPort = gatekeeperPort;
	}
	public String getGatekeeperURI() {
		return gatekeeperURI;
	}
	public void setGatekeeperURI(String gatekeeperURI) {
		this.gatekeeperURI = gatekeeperURI;
	}
	
	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}
	

}