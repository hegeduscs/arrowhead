package eu.arrowhead.common.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "broker", uniqueConstraints = {@UniqueConstraint(columnNames = {"broker_name"})})
public class Broker {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @XmlTransient
  private int id;

  @Column(name = "broker_name")

  private String brokerName;

  @Column(name = "address")

  private String address;

  @Column(name = "port")
  private Integer port;

  @Column(name = "is_secure")
  private boolean secure;

  @Column(name = "authentication_info", length = 2047)
  private String authenticationInfo;

  public Broker() {
  }

  public Broker(String brokerName, String address, Integer port, boolean secure, String authenticationInfo) {
    this.brokerName = brokerName;
    this.address = address;
    this.port = port;
    this.secure = secure;
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
    return secure;
  }

  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  public String getAuthenticationInfo() {
    return authenticationInfo;
  }

  public void setAuthenticationInfo(String authenticationInfo) {
    this.authenticationInfo = authenticationInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Broker broker = (Broker) o;

    if (!address.equals(broker.address)) {
      return false;
    }
    return port.equals(broker.port);
  }

  @Override
  public int hashCode() {
    int result = address.hashCode();
    result = 31 * result + port.hashCode();
    return result;
  }

  @JsonIgnore
  public boolean isValid() {
    return brokerName != null && address != null;
  }
}
