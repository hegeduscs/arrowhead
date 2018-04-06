/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.database;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.messages.ArrowheadBase;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

/**
 * Entity class for storing Arrowhead Clouds in the database. The "operator" and "cloud_name" columns must be unique together.
 */
@Entity
@JsonIgnoreProperties({"alwaysMandatoryFields"})
@Table(name = "arrowhead_cloud", uniqueConstraints = {@UniqueConstraint(columnNames = {"operator", "cloud_name"})})
public class ArrowheadCloud extends ArrowheadBase {

  @Transient
  private static final Set<String> alwaysMandatoryFields = new HashSet<>(Arrays.asList("operator", "cloudName"));

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "operator")
  private String operator;

  @Column(name = "cloud_name")
  private String cloudName;

  @Column(name = "address")
  private String address;

  @Column(name = "port")
  private Integer port;

  @Column(name = "gatekeeper_service_uri")
  private String gatekeeperServiceURI;

  @Column(name = "authentication_info", length = 2047)
  private String authenticationInfo;

  @Column(name = "is_secure")
  private Boolean secure;

  public ArrowheadCloud() {
  }

  public ArrowheadCloud(String operator, String cloudName, String address, Integer port, String gatekeeperServiceURI, String authenticationInfo,
                        Boolean secure) {
    this.operator = operator;
    this.cloudName = cloudName;
    this.address = address;
    this.port = port;
    this.gatekeeperServiceURI = gatekeeperServiceURI;
    this.authenticationInfo = authenticationInfo;
    this.secure = secure;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public String getCloudName() {
    return cloudName;
  }

  public void setCloudName(String cloudName) {
    this.cloudName = cloudName;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public String getGatekeeperServiceURI() {
    return gatekeeperServiceURI;
  }

  public void setGatekeeperServiceURI(String gatekeeperServiceURI) {
    this.gatekeeperServiceURI = gatekeeperServiceURI;
  }

  public String getAuthenticationInfo() {
    return authenticationInfo;
  }

  public void setAuthenticationInfo(String authenticationInfo) {
    this.authenticationInfo = authenticationInfo;
  }

  public Boolean isSecure() {
    return secure;
  }

  public void setSecure(Boolean secure) {
    this.secure = secure;
  }

  public Set<String> missingFields(boolean throwException, Set<String> mandatoryFields) {
    if (mandatoryFields == null) {
      mandatoryFields = new HashSet<>(alwaysMandatoryFields);
    }
    mandatoryFields.addAll(alwaysMandatoryFields);
    Set<String> nonNullFields = getFieldNamesWithNonNullValue();
    for (final String field : mandatoryFields) {
      if (field.startsWith(getClass().getSimpleName())) {
        nonNullFields = prefixFieldNames(nonNullFields);
        break;
      }
    }
    mandatoryFields.removeAll(nonNullFields);

    if (throwException && !mandatoryFields.isEmpty()) {
      throw new BadPayloadException("Missing mandatory fields for " + getClass().getSimpleName() + ": " + String.join(", ", mandatoryFields));
    }
    return mandatoryFields;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ArrowheadCloud that = (ArrowheadCloud) o;

    if (!operator.equals(that.operator)) {
      return false;
    }
    if (!cloudName.equals(that.cloudName)) {
      return false;
    }
    if (address != null ? !address.equals(that.address) : that.address != null) {
      return false;
    }
    if (port != null ? !port.equals(that.port) : that.port != null) {
      return false;
    }
    if (gatekeeperServiceURI != null ? !gatekeeperServiceURI.equals(that.gatekeeperServiceURI) : that.gatekeeperServiceURI != null) {
      return false;
    }
    if (authenticationInfo != null ? !authenticationInfo.equals(that.authenticationInfo) : that.authenticationInfo != null) {
      return false;
    }
    return secure != null ? secure.equals(that.secure) : that.secure == null;
  }

  @Override
  public int hashCode() {
    int result = operator.hashCode();
    result = 31 * result + cloudName.hashCode();
    result = 31 * result + (address != null ? address.hashCode() : 0);
    result = 31 * result + (port != null ? port.hashCode() : 0);
    result = 31 * result + (gatekeeperServiceURI != null ? gatekeeperServiceURI.hashCode() : 0);
    result = 31 * result + (authenticationInfo != null ? authenticationInfo.hashCode() : 0);
    result = 31 * result + (secure != null ? secure.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "(" + operator + ":" + cloudName + ")";
  }

}