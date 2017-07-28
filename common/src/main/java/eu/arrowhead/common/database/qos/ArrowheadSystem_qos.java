package eu.arrowhead.common.database.qos;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Entity class for storing Arrowhead Systems in the database. The "system_group" and "system_name"
 * columns must be unique together.
 */
@Entity
@Table(name = "arrowhead_system", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"system_group", "system_name"})})
@XmlRootElement
public class ArrowheadSystem_qos {

  @Column(name = "arrowhead_system_id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "system_group")
  private String systemGroup;

  @Column(name = "system_name")
  private String systemName;

  @Column(name = "address")
  private String address;

  @Column(name = "port")
  private String port;

  @Column(name = "authentication_info")
  private String authenticationInfo;

  public ArrowheadSystem_qos() {
  }

  public ArrowheadSystem_qos(String systemGroup, String systemName, String address,
                             String port,
                             String authenticationInfo) {
    this.systemGroup = systemGroup;
    this.systemName = systemName;
    this.address = address;
    this.port = port;
    this.authenticationInfo = authenticationInfo;
  }

  /**
   * Get ID.
   *
   * @return Returns integer with ID.
   */
  @XmlTransient
  public int getId() {
    return id;
  }

  /**
   * Set ID.
   *
   * @param id Set integer ID.
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Get System Group.
   *
   * @return Returns string with system group.
   */
  public String getSystemGroup() {
    return systemGroup;
  }

  /**
   * Set system group.
   *
   * @param systemGroup String System group.
   */
  public void setSystemGroup(String systemGroup) {
    this.systemGroup = systemGroup;
  }

  /**
   * Get system name.
   *
   * @return Returns syste name.
   */
  public String getSystemName() {
    return systemName;
  }

  /**
   * Set System Name.
   *
   * @param systemName System name.
   */
  public void setSystemName(String systemName) {
    this.systemName = systemName;
  }

  /**
   * Get address.
   *
   * @return Get Address.
   */
  public String getAddress() {
    return address;
  }

  /**
   * Get address.
   *
   * @param address Network Address.
   */
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * Get Port.
   *
   * @return Returns string with Port.
   */
  public String getPort() {
    return port;
  }

  /**
   * Set Port.
   *
   * @param port Port.
   */
  public void setPort(String port) {
    this.port = port;
  }

  /**
   * Get Authentication Information.
   *
   * @return Returns string with authentication Information .
   */
  public String getAuthenticationInfo() {
    return authenticationInfo;
  }

  /**
   * Set authenticationInfo.
   *
   * @param authenticationInfo Authentication Information.
   */
  public void setAuthenticationInfo(String authenticationInfo) {
    this.authenticationInfo = authenticationInfo;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((address == null) ? 0 : address.hashCode());
    result = prime * result + ((systemGroup == null) ? 0 : systemGroup.
        hashCode());
    result = prime * result + ((systemName == null) ? 0 : systemName.
        hashCode());
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
    if (!(obj instanceof ArrowheadSystem_qos)) {
      return false;
    }
    ArrowheadSystem_qos other = (ArrowheadSystem_qos) obj;
    if (address == null) {
      if (other.address != null) {
        return false;
      }
    } else if (!address.equals(other.address)) {
      return false;
    }
    if (systemGroup == null) {
      if (other.systemGroup != null) {
        return false;
      }
    } else if (!systemGroup.equals(other.systemGroup)) {
      return false;
    }
    if (systemName == null) {
      if (other.systemName != null) {
        return false;
      }
    } else if (!systemName.equals(other.systemName)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "(" + systemGroup + ":" + systemName + ")";
  }

}
