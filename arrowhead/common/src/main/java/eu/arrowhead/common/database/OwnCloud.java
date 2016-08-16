package eu.arrowhead.common.database;

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
 * Entity class for storing information about the local Cloud in the database.
 * (Gatekeeper needs this information for negotiations.)
 * The "operator" and "cloud_name" columns must be unique together.
 */
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
	@Column(name="address")
	private String address;
	@Column(name="port")
	private String port;
	@Column(name="authentication_info")
	private String authenticationInfo;
	@Column(name="gatekeeper_service_uri")
	private String gatekeeperServiceURI;
	
	public OwnCloud(){
	}
	
	public OwnCloud(String operator, String cloudName, String address, String port, 
			String gatekeeperServiceURI, String authenticationInfo) {
		this.operator = operator;
		this.cloudName = cloudName;
		this.address = address;
		this.port = port;
		this.gatekeeperServiceURI = gatekeeperServiceURI;
		this.authenticationInfo = authenticationInfo;
	}
	
	public int getId() {
		return id;
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

	public String getGatekeeperServiceURI() {
		return gatekeeperServiceURI;
	}

	public void setGatekeeperServiceURI(String gatekeeperServiceURI) {
		this.gatekeeperServiceURI = gatekeeperServiceURI;
	}

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}
	
	public boolean isPayloadUsable(){
		if(operator == null || cloudName == null || address == null)
			return false;
		return true;
	}
	
	
}
