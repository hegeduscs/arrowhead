package eu.arrowhead.common.configuration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author umlaufz
 * 
 * Entity class for storing Core System informations in the database.
 * The "system_name" column must be unique.
 */
@Entity
@Table(name="core_system", uniqueConstraints={@UniqueConstraint(columnNames = {"system_name"})})
public class CoreSystem {
	
	@Column(name="id")
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    @XmlTransient
    private int id;
	@Column(name="system_name")
	private String systemName;
	@Column(name="ip_address")
	private String IPAddress;
	@Column(name="port")
	private String port;
	@Column(name="service_uri")
	private String serviceURI;
	@Column(name="authentication_info")
	private String authenticationInfo;
	
	public CoreSystem(){
	}
	
	public CoreSystem(String systemName, String IPAddress, String port, 
				String serviceURI, String authenticationInfo) {
		this.systemName = systemName;
		this.IPAddress = IPAddress;
		this.port = port;
		this.serviceURI = serviceURI;
		this.authenticationInfo = authenticationInfo;
	}
	
	public int getId() {
		return id;
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

	public void setIPAddress(String IPAddress) {
		this.IPAddress = IPAddress;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
	
	public String getServiceURI() {
		return serviceURI;
	}

	public void setServiceURI(String serviceURI) {
		this.serviceURI = serviceURI;
	}

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}

	
}
