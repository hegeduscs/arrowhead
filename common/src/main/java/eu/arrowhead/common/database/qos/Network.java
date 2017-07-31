/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.arrowhead.common.database.qos;

import java.util.Map;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 * @author Paulo
 */
@Entity
@Table(name = "network", uniqueConstraints = {@UniqueConstraint(columnNames = {"network_name"})})
@XmlRootElement
public class Network {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @XmlTransient
  private int id;

  @Column(name = "network_name")
  private String name;

  @Column(name = "network_ip")
  private String networkIP;

  @Column(name = "network_type")
  private String networkType;

  @ElementCollection
  @LazyCollection(LazyCollectionOption.FALSE)
  private Map<String, String> networkConfigurations;

  Network() {
  }

  public Network(String name, String networkIP, String networkType, Map<String, String> networkConfigurations) {
    this.name = name;
    this.networkIP = networkIP;
    this.networkType = networkType;
    this.networkConfigurations = networkConfigurations;
  }

  /**
   * Name of network.
   *
   * @return Network name.
   */
  public String getName() {
    return name;
  }

  /**
   * Set name of network.
   *
   * @param name String with network name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * get network ip
   *
   * @return returns network ip
   */
  public String getNetworkIP() {
    return networkIP;
  }

  /**
   * set network ip
   *
   * @param networkIP network ip
   */
  public void setNetworkIP(String networkIP) {
    this.networkIP = networkIP;
  }

  /**
   * get network type
   *
   * @return returns string with network type
   */
  public String getNetworkType() {
    return networkType;
  }

  /**
   * set network type
   *
   * @param networkType network type
   */
  public void setNetworkType(String networkType) {
    this.networkType = networkType;
  }

  /**
   * get map with the network configurations
   *
   * @return returns map with the configurations
   */
  public Map<String, String> getNetworkConfigurations() {
    return networkConfigurations;
  }

  /**
   * set network configurations
   *
   * @param networkConfigurations map with the network configurations
   */
  public void setNetworkConfigurations(Map<String, String> networkConfigurations) {
    this.networkConfigurations = networkConfigurations;
  }

}
