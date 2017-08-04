package eu.arrowhead.common.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Entity class for storing Arrowhead Clouds in the database. The "operator" and "cloud_name" columns must be unique together.
 */
@Entity
@Table(name = "arrowhead_cloud", uniqueConstraints = {@UniqueConstraint(columnNames = {"operator", "cloud_name"})})
public class ArrowheadCloud {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "operator")
  private String operator;

  @Column(name = "cloud_name")
  private String cloudName;

  @Column(name = "address")
  private String address;

  @Column(name = "port")
  private String port;

  @Column(name = "gatekeeper_service_uri")
  private String gatekeeperServiceURI;

  @Column(name = "authentication_info")
  private String authenticationInfo;

  public ArrowheadCloud() {
  }

  public ArrowheadCloud(String operator, String cloudName, String address, String port, String gatekeeperServiceURI, String authenticationInfo) {
    this.operator = operator;
    this.cloudName = cloudName;
    this.address = address;
    this.port = port;
    this.gatekeeperServiceURI = gatekeeperServiceURI;
    this.authenticationInfo = authenticationInfo;
  }

  @XmlTransient
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

  public boolean isValid() {
    return operator != null && cloudName != null && address != null && gatekeeperServiceURI != null;
  }

  public boolean isValidForDatabase() {
    return operator != null && cloudName != null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((address == null) ? 0 : address.hashCode());
    result = prime * result + ((cloudName == null) ? 0 : cloudName.hashCode());
    result = prime * result + ((gatekeeperServiceURI == null) ? 0 : gatekeeperServiceURI.hashCode());
    result = prime * result + ((operator == null) ? 0 : operator.hashCode());
    result = prime * result + ((port == null) ? 0 : port.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ArrowheadCloud other = (ArrowheadCloud) obj;
    if (address == null) {
      if (other.address != null) {
        return false;
      }
    } else if (!address.equals(other.address)) {
      return false;
    }
    if (cloudName == null) {
      if (other.cloudName != null) {
        return false;
      }
    } else if (!cloudName.equals(other.cloudName)) {
      return false;
    }
    if (gatekeeperServiceURI == null) {
      if (other.gatekeeperServiceURI != null) {
        return false;
      }
    } else if (!gatekeeperServiceURI.equals(other.gatekeeperServiceURI)) {
      return false;
    }
    if (operator == null) {
      if (other.operator != null) {
        return false;
      }
    } else if (!operator.equals(other.operator)) {
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

  @Override
  public String toString() {
    return "(" + operator + ":" + cloudName + ")";
  }

}