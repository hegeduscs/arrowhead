package eu.arrowhead.common.configuration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name="own_cloud", uniqueConstraints={@UniqueConstraint(columnNames = {"operator", "cloud_name"})})
public class OwnCloud {
	
	@Column(name="id")
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    @XmlTransient
    private int id;
	@Column(name="operator")
	private String operator;
	@Column(name="cloud_name")
	private String cloudName;
	@Column(name="ip_address")
	private String IPAddress;
	@Column(name="port")
	private String port;
	@Column(name="authentication_info")
	private String authenticationInfo;
	@Column(name="service_uri")
	private String serviceURI;
	
	public OwnCloud(){
	}
	
	public OwnCloud(String operator, String cloudName, String IPAddress, String port, 
			String authenticationInfo, String serviceURI) {
		super();
		this.operator = operator;
		this.cloudName = cloudName;
		this.IPAddress = IPAddress;
		this.port = port;
		this.authenticationInfo = authenticationInfo;
		this.serviceURI = serviceURI;
	}
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getCloudName() {
		return cloudName;
	}

	public void setCloudName(String cloudName) {
		this.cloudName = cloudName;
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
