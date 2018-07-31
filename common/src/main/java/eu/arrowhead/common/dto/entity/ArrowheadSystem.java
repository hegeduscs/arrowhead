/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.dto.entity;

import java.util.Optional;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "address", "port"})})
public class ArrowheadSystem {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @NotBlank
  @Size(max = 255, message = "System name must be 255 character at max")
  private String name;

  @NotBlank
  @Size(min = 3, max = 255, message = "Address must be between 3 and 255 characters")
  private String address;

  @Min(value = 0, message = "Port can not be less than 0")
  @Max(value = 65535, message = "Port can not be greater than 65535")
  private int port;

  @Size(max = 2047, message = "Authentication information must be 2047 character at max")
  private String authenticationInfo;

  public ArrowheadSystem() {
  }

  public ArrowheadSystem(String name, String address, int port, String authenticationInfo) {
    this.name = name;
    this.address = address;
    this.port = port;
    this.authenticationInfo = authenticationInfo;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public Optional<String> getAuthenticationInfo() {
    return Optional.of(authenticationInfo);
  }

  public void setAuthenticationInfo(String authenticationInfo) {
    this.authenticationInfo = authenticationInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArrowheadSystem)) {
      return false;
    }

    ArrowheadSystem that = (ArrowheadSystem) o;

    if (port != that.port) {
      return false;
    }
    if (!name.equals(that.name)) {
      return false;
    }
    return address.equals(that.address);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + address.hashCode();
    result = 31 * result + port;
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ArrowheadSystem{");
    sb.append("id=").append(id);
    sb.append(", name='").append(name).append('\'');
    sb.append(", address='").append(address).append('\'');
    sb.append(", port=").append(port);
    sb.append('}');
    return sb.toString();
  }
}
