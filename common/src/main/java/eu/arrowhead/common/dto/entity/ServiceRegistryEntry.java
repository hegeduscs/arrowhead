/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.dto.entity;

import java.time.LocalDateTime;
import java.util.Optional;
import javax.persistence.CascadeType;
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

  @Size(max = 255, message = "Service URI must be 255 character at max")
  private String serviceUri;

  @Type(type = "yes_no")
  private boolean udp;

  @FutureOrPresent(message = "End of validity date cannot be in the past")
  private LocalDateTime endOfValidity;

  public ServiceRegistryEntry() {
  }

  public ServiceRegistryEntry(ArrowheadService service, ArrowheadSystem provider, String serviceUri, boolean udp, LocalDateTime endOfValidity) {
    this.service = service;
    this.provider = provider;
    this.serviceUri = serviceUri;
    this.udp = udp;
    this.endOfValidity = endOfValidity;
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

  public Optional<String> getServiceUri() {
    return Optional.ofNullable(serviceUri);
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

  public Optional<LocalDateTime> getEndOfValidity() {
    return Optional.ofNullable(endOfValidity);
  }

  public void setEndOfValidity(LocalDateTime endOfValidity) {
    this.endOfValidity = endOfValidity;
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

    if (!service.equals(that.service)) {
      return false;
    }
    return provider.equals(that.provider);
  }

  @Override
  public int hashCode() {
    int result = service.hashCode();
    result = 31 * result + provider.hashCode();
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ServiceRegistryEntry{");
    sb.append("providedServiceName=").append(service.getDefinition());
    sb.append(", providerSystemName='").append(provider.getName()).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
