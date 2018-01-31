/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
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
  private String networkIp;

  @Column(name = "network_type")
  private String networkType;

  @ElementCollection(fetch = FetchType.LAZY)
  @MapKeyColumn(name = "config_key")
  @Column(name = "config_value")
  @CollectionTable(name = "network_config_map", joinColumns = @JoinColumn(name = "id"))
  private Map<String, String> networkConfigurations = new HashMap<>();

  public Network() {
  }

  public Network(String name, String networkIp, String networkType, Map<String, String> networkConfigurations) {
    this.name = name;
    this.networkIp = networkIp;
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

  public String getNetworkIp() {
    return networkIp;
  }

  public void setNetworkIp(String networkIp) {
    this.networkIp = networkIp;
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
