/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Entity class for storing Arrowhead Systems in the database. The "system_name" column must be unique.
 */
@Entity
@Table(name = "arrowhead_system", uniqueConstraints = {@UniqueConstraint(columnNames = {"system_name"})})
public class ArrowheadSystem {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "system_name")
  private String systemName;

  @Column(name = "address")
  private String address;

  @Transient
  private int port;

  @Column(name = "authentication_info", length = 2047)
  private String authenticationInfo;

  public ArrowheadSystem() {
  }

  public ArrowheadSystem(String systemName, String address, int port, String authenticationInfo) {
    this.systemName = systemName;
    this.address = address;
    this.port = port;
    this.authenticationInfo = authenticationInfo;
  }

  public ArrowheadSystem(String json) {
    String[] fields = json.split(",");
    this.systemName = fields[0];

    if (fields.length == 4) {
      this.address = fields[1];
      this.port = Integer.valueOf(fields[2]);
      this.authenticationInfo = fields[3];
    }
  }

  public ArrowheadSystem(ArrowheadSystem system) {
    this.systemName = system.systemName;
    this.address = system.address;
    this.port = system.port;
    this.authenticationInfo = system.authenticationInfo;
  }

  @XmlTransient
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
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

  public String getAuthenticationInfo() {
    return authenticationInfo;
  }

  public void setAuthenticationInfo(String authenticationInfo) {
    this.authenticationInfo = authenticationInfo;
  }

  @JsonIgnore
  public boolean isValid() {
    return systemName != null && address != null;
  }

  @JsonIgnore
  public boolean isValidForDatabase() {
    return systemName != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ArrowheadSystem that = (ArrowheadSystem) o;

    if (!systemName.equals(that.systemName)) {
      return false;
    }
    return address.equals(that.address);
  }

  @Override
  public int hashCode() {
    int result = systemName.hashCode();
    result = 31 * result + address.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return systemName + "," + address + "," + port + "," + authenticationInfo;
  }

  public String toArrowheadCommonName(String operator, String cloudName) {
    if (systemName.contains(".") || operator.contains(".") || cloudName.contains(".")) {
      throw new IllegalArgumentException("The string fields can not contain dots!");
    }
    //throws NPE if any of the fields are null
    return systemName.concat(".").concat(cloudName).concat(".").concat(operator).concat(".").concat("arrowhead.eu");
  }

}
