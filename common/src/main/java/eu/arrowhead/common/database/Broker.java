/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
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
import org.hibernate.annotations.Type;

@Entity
@JsonIgnoreProperties({"alwaysMandatoryFields"})
@Table(name = "broker", uniqueConstraints = {@UniqueConstraint(columnNames = {"broker_name"})})
public class Broker extends ArrowheadBase {

  @Transient
  private static final Set<String> alwaysMandatoryFields = new HashSet<>(Arrays.asList("brokerName", "address", "port"));

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "broker_name")
  private String brokerName;

  @Column(name = "address")
  private String address;

  @Column(name = "port")
  private Integer port;

  @Column(name = "is_secure")
  @Type(type = "yes_no")
  private boolean secure;

  @Column(name = "authentication_info", length = 2047)
  private String authenticationInfo;

  public Broker() {
  }

  public Broker(String brokerName, String address, Integer port, boolean secure, String authenticationInfo) {
    this.brokerName = brokerName;
    this.address = address;
    this.port = port;
    this.secure = secure;
    this.authenticationInfo = authenticationInfo;
  }

  public int getId() {
    return id;
  }

  public String getBrokerName() {
    return brokerName;
  }

  public void setBrokerName(String brokerName) {
    this.brokerName = brokerName;
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

  public boolean isSecure() {
    return secure;
  }

  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  public String getAuthenticationInfo() {
    return authenticationInfo;
  }

  public void setAuthenticationInfo(String authenticationInfo) {
    this.authenticationInfo = authenticationInfo;
  }

  public Set<String> missingFields(boolean throwException, Set<String> mandatoryFields) {
    if (mandatoryFields == null) {
      mandatoryFields = new HashSet<>(alwaysMandatoryFields);
    }
    mandatoryFields.addAll(alwaysMandatoryFields);
    Set<String> nonNullFields = getFieldNamesWithNonNullValue();
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

    Broker broker = (Broker) o;

    if (!brokerName.equals(broker.brokerName)) {
      return false;
    }
    if (!address.equals(broker.address)) {
      return false;
    }
    return port.equals(broker.port);
  }

  @Override
  public int hashCode() {
    int result = brokerName.hashCode();
    result = 31 * result + address.hashCode();
    result = 31 * result + port.hashCode();
    return result;
  }

}
