/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.json.support;

import eu.arrowhead.common.database.ArrowheadSystem;

public class ArrowheadSystemSupport {

  private String systemGroup;
  private String systemName;
  private String address;
  private int port;
  private String authenticationInfo;

  public ArrowheadSystemSupport() {
  }

  public ArrowheadSystemSupport(ArrowheadSystem system) {
    if (system.getSystemName().contains("_")) {
      String[] parts = system.getSystemName().split("_");
      this.systemGroup = parts[0];
      this.systemName = parts[1];
    } else {
      this.systemName = system.getSystemName();
    }
    this.address = system.getAddress();
    this.port = system.getPort();
    this.authenticationInfo = system.getAuthenticationInfo();
  }

  public ArrowheadSystemSupport(String systemGroup, String systemName, String address, int port, String authenticationInfo) {
    this.systemGroup = systemGroup;
    this.systemName = systemName;
    this.address = address;
    this.port = port;
    this.authenticationInfo = authenticationInfo;
  }

  public String getSystemGroup() {
    return systemGroup;
  }

  public void setSystemGroup(String systemGroup) {
    this.systemGroup = systemGroup;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ArrowheadSystemSupport that = (ArrowheadSystemSupport) o;

    if (port != that.port) {
      return false;
    }
    if (!systemGroup.equals(that.systemGroup)) {
      return false;
    }
    if (!systemName.equals(that.systemName)) {
      return false;
    }
    return address.equals(that.address);
  }

  @Override
  public int hashCode() {
    int result = systemGroup.hashCode();
    result = 31 * result + systemName.hashCode();
    result = 31 * result + address.hashCode();
    result = 31 * result + port;
    return result;
  }

  @Override
  public String toString() {
    return "(" + systemGroup + ":" + systemName + ")";
  }

}
