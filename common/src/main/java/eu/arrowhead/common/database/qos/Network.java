/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.common.database.qos;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Paulo
 */
@Entity
@Table(name = "network", uniqueConstraints = {@UniqueConstraint(columnNames = {"network_name"})})
public class Network {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "network_name")
  private String name;

  @Column(name = "network_ip")
  private String networkIP;

  @Column(name = "network_type")
  private String networkType;

  @ElementCollection(fetch = FetchType.LAZY)
  @MapKeyColumn(name = "config_key")
  @Column(name = "config_value")
  @CollectionTable(name = "network_config_map", joinColumns = @JoinColumn(name = "id"))
  private Map<String, String> networkConfigurations = new HashMap<>();

  public Network() {
  }

  public Network(String name, String networkIP, String networkType, Map<String, String> networkConfigurations) {
    this.name = name;
    this.networkIP = networkIP;
    this.networkType = networkType;
    this.networkConfigurations = networkConfigurations;
  }

  @XmlTransient
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNetworkIP() {
    return networkIP;
  }

  public void setNetworkIP(String networkIP) {
    this.networkIP = networkIP;
  }

  public String getNetworkType() {
    return networkType;
  }

  public void setNetworkType(String networkType) {
    this.networkType = networkType;
  }

  public Map<String, String> getNetworkConfigurations() {
    return networkConfigurations;
  }

  public void setNetworkConfigurations(Map<String, String> networkConfigurations) {
    this.networkConfigurations = networkConfigurations;
  }

}
