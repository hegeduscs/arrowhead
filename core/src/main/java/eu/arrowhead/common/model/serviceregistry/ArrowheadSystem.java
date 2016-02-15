package eu.arrowhead.common.model.serviceregistry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ArrowheadSystem {

	@XmlElement(name = "OwnerCloud")
	ArrowheadCloud arrowheadCloud;
	@XmlElement(name = "SystemGroup")
	String systemGroup;
	@XmlElement(name = "SystemName")
	String systemName;
	@XmlElement(name = "IPAddress")
	String ipAddress;
	@XmlElement(name = "Port")
	String port;
	@XmlElement(name = "AuthenticationInfo")
	String authenticationInfo;

	public ArrowheadCloud getArrowheadCloud() {
		return arrowheadCloud;
	}

	public void setArrowheadCloud(ArrowheadCloud arrowheadCloud) {
		this.arrowheadCloud = arrowheadCloud;
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

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
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
