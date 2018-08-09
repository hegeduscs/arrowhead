/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.database;

import java.time.LocalDateTime;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "service_registry", uniqueConstraints = {@UniqueConstraint(columnNames = {"arrowhead_service_id", "provider_system_id"})})
public class ServiceRegistryEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @Valid
  @NotNull(message = "Provided ArrowheadService cannot be null")
  @JoinColumn(name = "arrowhead_service_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ArrowheadService service;

  @Valid
  @NotNull(message = "Provider ArrowheadSystem cannot be null")
  @JoinColumn(name = "provider_system_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ArrowheadSystem provider;

  @Column(name = "service_uri")
  @Size(max = 255, message = "Service URI must be 255 character at max")
  private String serviceUri;

  @Type(type = "yes_no")
  private boolean udp;

  @Column(name = "end_of_validity")
  @FutureOrPresent(message = "End of validity date cannot be in the past")
  private LocalDateTime endOfValidity;

  private int version = 1;

  public ServiceRegistryEntry() {
  }

  public ServiceRegistryEntry(ArrowheadService service, ArrowheadSystem provider, String serviceUri) {
    this.service = service;
    this.provider = provider;
    this.serviceUri = serviceUri;
  }


  public ServiceRegistryEntry(ArrowheadService service, ArrowheadSystem provider, String serviceUri, boolean udp, LocalDateTime endOfValidity,
                              int version) {
    this.service = service;
    this.provider = provider;
    this.serviceUri = serviceUri;
    this.udp = udp;
    this.endOfValidity = endOfValidity;
    this.version = version;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public ArrowheadService getService() {
    return service;
  }

  public void setService(ArrowheadService service) {
    this.service = service;
  }

  public ArrowheadSystem getProvider() {
    return provider;
  }

  public void setProvider(ArrowheadSystem provider) {
    this.provider = provider;
  }

  public String getServiceUri() {
    return serviceUri;
  }

  public void setServiceUri(String serviceUri) {
    this.serviceUri = serviceUri;
  }

  public boolean isUdp() {
    return udp;
  }

  public void setUdp(boolean udp) {
    this.udp = udp;
  }

  public LocalDateTime getEndOfValidity() {
    return endOfValidity;
  }

  public void setEndOfValidity(LocalDateTime endOfValidity) {
    this.endOfValidity = endOfValidity;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ServiceRegistryEntry)) {
      return false;
    }

    ServiceRegistryEntry that = (ServiceRegistryEntry) o;

    if (version != that.version) {
      return false;
    }
    if (!service.equals(that.service)) {
      return false;
    }
    if (!provider.equals(that.provider)) {
      return false;
    }
    return serviceUri != null ? serviceUri.equals(that.serviceUri) : that.serviceUri == null;
  }

  @Override
  public int hashCode() {
    int result = service.hashCode();
    result = 31 * result + provider.hashCode();
    result = 31 * result + (serviceUri != null ? serviceUri.hashCode() : 0);
    result = 31 * result + version;
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ServiceRegistryEntry{");
    sb.append("service=").append(service);
    sb.append(", provider=").append(provider);
    sb.append('}');
    return sb.toString();
  }
}