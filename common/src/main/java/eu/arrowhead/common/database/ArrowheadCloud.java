package eu.arrowhead.common.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
  @NotNull
  private String operator;

  @Column(name = "cloud_name")
  @NotNull
  private String cloudName;

  @Column(name = "address")
  private String address;

  @Column(name = "port")
  private int port;

  @Column(name = "gatekeeper_service_uri")
  private String gatekeeperServiceURI;

  @Column(name = "authentication_info")
  private String authenticationInfo;

  @Column(name = "is_secure")
  private boolean isSecure;

  public ArrowheadCloud() {
  }

  public ArrowheadCloud(@NotNull String operator, @NotNull String cloudName, String address, int port, String gatekeeperServiceURI,
                        String authenticationInfo, boolean isSecure) {
    this.operator = operator;
    this.cloudName = cloudName;
    this.address = address;
    this.port = port;
    this.gatekeeperServiceURI = gatekeeperServiceURI;
    this.authenticationInfo = authenticationInfo;
    this.isSecure = isSecure;
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

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
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

  public boolean isSecure() {
    return isSecure;
  }

  public void setSecure(boolean secure) {
    isSecure = secure;
  }

  public boolean isValid() {
    return operator != null && cloudName != null && address != null && gatekeeperServiceURI != null;
  }

  public boolean isValidForDatabase() {
    return operator != null && cloudName != null;
  }

  @Override
  public int hashCode() {
    int result = operator != null ? operator.hashCode() : 0;
    result = 31 * result + (cloudName != null ? cloudName.hashCode() : 0);
    result = 31 * result + (address != null ? address.hashCode() : 0);
    result = 31 * result + port;
    result = 31 * result + (gatekeeperServiceURI != null ? gatekeeperServiceURI.hashCode() : 0);
    return result;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ArrowheadCloud that = (ArrowheadCloud) o;

    if (port != that.port) {
      return false;
    }
    if (operator != null ? !operator.equals(that.operator) : that.operator != null) {
      return false;
    }
    if (cloudName != null ? !cloudName.equals(that.cloudName) : that.cloudName != null) {
      return false;
    }
    if (address != null ? !address.equals(that.address) : that.address != null) {
      return false;
    }
    return gatekeeperServiceURI != null ? gatekeeperServiceURI.equals(that.gatekeeperServiceURI) : that.gatekeeperServiceURI == null;
  }

  @org.jetbrains.annotations.NotNull
  @Override
  public String toString() {
    return "(" + operator + ":" + cloudName + ")";
  }

}