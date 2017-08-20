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

/**
 * JPA entity class for storing <tt>CoreSystem</tt> information in the database. The <i>system_name</i> column must be unique.
 * <p>
 * The information is used by the Core Systems to contact each other. Arrowhead Core Systems include but not limited to the Orchestrator, Service
 * Registry, Authorization and the Gatekeeper.
 *
 * @author Umlauf Zoltán
 */
@Entity
@Table(name = "core_system", uniqueConstraints = {@UniqueConstraint(columnNames = {"system_name"})})
public class CoreSystem {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @XmlTransient
  private int id;

  @Column(name = "system_name")
  @NotNull
  private String systemName;

  @Column(name = "address")
  @NotNull
  private String address;

  @Column(name = "port")
  private int port;

  @Column(name = "service_uri")
  @NotNull
  private String serviceURI;

  @Column(name = "is_secure")
  private boolean isSecure;

  @Column(name = "authentication_info")
  private String authenticationInfo;

  public CoreSystem() {
  }

  /**
   * @param systemName Name of the Core System
   * @param address IP address or hostname for the Core System (e.g. "127.0.0.1" or "arrowhead.tmit.bme.hu")
   * @param port The port number where the Core System offers its services (optional)
   * @param serviceURI The path where the REST resource(s) is/are available
   * @param isSecure Indicates weather the server uses HTTP or HTTPS protocol
   * @param authenticationInfo In case <tt>isSecure</tt> is true, this field holds the Base64 coded public key of the Core System certificate
   */
  public CoreSystem(String systemName, String address, int port, String serviceURI, boolean isSecure, String authenticationInfo) {
    this.systemName = systemName;
    this.address = address;
    this.port = port;
    this.serviceURI = serviceURI;
    this.isSecure = isSecure;
    this.authenticationInfo = authenticationInfo;
  }

  @XmlTransient
  public int getId() {
    return id;
  }

  public String getSystemName() {
    return systemName;
  }

  public void setSystemName(String systemName) {
    this.systemName = systemName;
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

  public String getServiceURI() {
    return serviceURI;
  }

  public void setServiceURI(String serviceURI) {
    this.serviceURI = serviceURI;
  }

  public boolean getIsSecure() {
    return isSecure;
  }

  public void setIsSecure(boolean isSecure) {
    this.isSecure = isSecure;
  }

  public String getAuthenticationInfo() {
    return authenticationInfo;
  }

  public void setAuthenticationInfo(String authenticationInfo) {
    this.authenticationInfo = authenticationInfo;
  }

  /**
   * Simple inspector method to check weather a CoreSystem instance is valid to be stored in the database.
   *
   * @return False if <tt>systemName</tt>, <tt>address</tt> or <tt>serviceURI</tt> is null, true otherwise
   */
  public boolean isValid() {
    return systemName != null && address != null && serviceURI != null;
  }

}
