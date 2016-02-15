package eu.arrowhead.common.model.serviceregistry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ArrowheadCloud {

	@XmlElement(name = "Operator")
	String operator;
	@XmlElement(name = "Name")
	String name;
	@XmlElement(name = "GateKeeperIP")
	String gateKeeperIP;
	@XmlElement(name = "GateKeeperPort")
	String gateKeeperPort;
	@XmlElement(name = "GateKeeperURI")
	String gateKeeperURI;
	@XmlElement(name = "AuthenticationInfo")
	String authenticationInfo;

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGateKeeperIP() {
		return gateKeeperIP;
	}

	public void setGateKeeperIP(String gateKeeperIP) {
		this.gateKeeperIP = gateKeeperIP;
	}

	public String getGateKeeperPort() {
		return gateKeeperPort;
	}

	public void setGateKeeperPort(String gateKeeperPort) {
		this.gateKeeperPort = gateKeeperPort;
	}

	public String getGateKeeperURI() {
		return gateKeeperURI;
	}

	public void setGateKeeperURI(String gateKeeperURI) {
		this.gateKeeperURI = gateKeeperURI;
	}

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}

}
