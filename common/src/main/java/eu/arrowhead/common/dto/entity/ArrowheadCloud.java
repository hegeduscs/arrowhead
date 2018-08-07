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
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
public class ArrowheadCloud {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @NotNull(message = "Gatekeeper ArrowheadSystem cannot be null")
  @JoinColumn(name = "gatekeeper_system_id", unique = true)
  @OneToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ArrowheadSystem gatekeeper;

  private String serviceUri;

  @NotBlank
  @Size(max = 255, message = "Cloud operator must be 255 character at max")
  private String operator;

  public ArrowheadCloud() {
  }

  public ArrowheadCloud(ArrowheadSystem gatekeeper, String serviceUri, String operator) {
    this.gatekeeper = gatekeeper;
    this.serviceUri = serviceUri;
    this.operator = operator;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public ArrowheadSystem getGatekeeper() {
    return gatekeeper;
  }

  public void setGatekeeper(ArrowheadSystem gatekeeper) {
    this.gatekeeper = gatekeeper;
  }

  public Optional<String> getServiceUri() {
    return Optional.ofNullable(serviceUri);
  }

  public void setServiceUri(String serviceUri) {
    this.serviceUri = serviceUri;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArrowheadCloud)) {
      return false;
    }

    ArrowheadCloud that = (ArrowheadCloud) o;

    if (!gatekeeper.equals(that.gatekeeper)) {
      return false;
    }
    if (serviceUri != null ? !serviceUri.equals(that.serviceUri) : that.serviceUri != null) {
      return false;
    }
    return operator != null ? operator.equals(that.operator) : that.operator == null;
  }

  @Override
  public int hashCode() {
    int result = gatekeeper.hashCode();
    result = 31 * result + (serviceUri != null ? serviceUri.hashCode() : 0);
    result = 31 * result + (operator != null ? operator.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ArrowheadCloud{");
    sb.append("gatekeeper=").append(gatekeeper);
    sb.append(", serviceUri='").append(serviceUri).append('\'');
    sb.append(", operator='").append(operator).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
