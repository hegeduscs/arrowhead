/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.database.qos;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "network_device", uniqueConstraints = {@UniqueConstraint(columnNames = {"mac_address"})})
public class NetworkDevice {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "name")
  private String name;

  @Column(name = "mac_address")
  private String macAddress;

  @ElementCollection(fetch = FetchType.LAZY)
  @MapKeyColumn(name = "capability_key")
  @Column(name = "capability_value")
  @CollectionTable(name = "network_device_network_capabilities", joinColumns = @JoinColumn(name = "id"))
  private Map<String, String> networkCapabilities = new HashMap<>();

  @JoinColumn(name = "network_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE})
  private Network network;

  public NetworkDevice() {
  }

  public NetworkDevice(String name, String macAddress, Map<String, String> networkCapabilities, Network network) {
    this.name = name;
    this.macAddress = macAddress;
    this.networkCapabilities = networkCapabilities;
    this.network = network;
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
   * @param id Integer with ID.
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Get Name of network device.
   *
   * @return Returns string with name.
   */
  public String getName() {
    return name;
  }

  /**
   * Set name of network device.
   *
   * @param name String with name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get MAC Address.
   *
   * @return Returns a String with a MAC Address.
   */
  public String getMacAddress() {
    return macAddress;
  }

  /**
   * Set MAC Address.
   *
   * @param macAddress String with MAC address.
   */
  public void setMacAddress(String macAddress) {
    this.macAddress = macAddress;
  }

  /**
   * Get Network Device Capabilities (ex. bandwidth)
   *
   * @return Returns Map with capabilities.
   */
  public Map<String, String> getNetworkCapabilities() {
    return networkCapabilities;
  }

  /**
   * Set Network Capabilities.
   *
   * @param networkCapabilities Map with network device capabilities.
   */
  public void setNetworkCapabilities(Map<String, String> networkCapabilities) {
    this.networkCapabilities = networkCapabilities;
  }

  /**
   * Get network where the network device is included..
   *
   * @return Returns network.
   */
  public Network getNetwork() {
    return network;
  }

  /**
   * Set Network.
   *
   * @param network Network.
   */
  public void setNetwork(Network network) {
    this.network = network;
  }

}
