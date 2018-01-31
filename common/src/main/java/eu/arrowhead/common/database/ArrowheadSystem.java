/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import eu.arrowhead.common.json.support.ArrowheadSystemSupport;
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
 * Entity class for storing Arrowhead Systems in the database. The "system_group" and "system_name" columns must be unique together.
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

  public ArrowheadSystem(ArrowheadSystemSupport system) {
    this.systemName = system.getSystemGroup() + "_" + system.getSystemName();
    this.address = system.getAddress();
    this.port = system.getPort();
    this.authenticationInfo = system.getAuthenticationInfo();
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
    return systemName.concat(".").concat(".").concat(cloudName).concat(".").concat(operator).concat(".").concat("arrowhead.eu");
  }

}
