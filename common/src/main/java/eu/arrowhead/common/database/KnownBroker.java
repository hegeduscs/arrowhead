package eu.arrowhead.common.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "known_broker", uniqueConstraints = { @UniqueConstraint(columnNames = { "broker_name" }) })
public class KnownBroker {

	@Column(name = "id")
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@XmlTransient
	private int id;

	@Column(name = "broker_name")
	@NotNull
	private String brokerName;

	@Column(name = "address")
	@NotNull
	private String address;

	@Column(name = "port")
	private Integer port;

	@Column(name = "is_secure")
	private boolean isSecure;

	@Column(name = "authentication_info", length = 2047)
	private String authenticationInfo;

	public KnownBroker() {
	}

	public KnownBroker(String brokerName, String address, Integer port, boolean isSecure, String authenticationInfo) {
		this.brokerName = brokerName;
		this.address = address;
		this.port = port;
		this.isSecure = isSecure;
		this.authenticationInfo = authenticationInfo;
	}

	@XmlTransient
	public int getId() {
		return id;
	}

	public String getBrokerName() {
		return brokerName;
	}

	public void setBrokerName(String brokerName) {
		this.brokerName = brokerName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isSecure() {
		return isSecure;
	}

	public void setSecure(boolean secure) {
		isSecure = secure;
	}

	public String getAuthenticationInfo() {
		return authenticationInfo;
	}

	public void setAuthenticationInfo(String authenticationInfo) {
		this.authenticationInfo = authenticationInfo;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof KnownBroker)) {
			return false;
		}
		KnownBroker other = (KnownBroker) obj;
		if (address == null) {
			if (other.address != null) {
				return false;
			}
		} else if (!address.equals(other.address)) {
			return false;
		}
		if (port == null) {
			if (other.port != null) {
				return false;
			}
		} else if (!port.equals(other.port)) {
			return false;
		}
		return true;
	}

	public boolean isValid() {
		return brokerName != null && address != null;
	}
}
