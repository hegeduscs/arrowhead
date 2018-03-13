/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common.database;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@Entity
@JsonIgnoreProperties({"id", "port", "metadata", "endOfValidity"})
@Table(name = "service_registry", uniqueConstraints = {@UniqueConstraint(columnNames = {"arrowhead_service_id", "provider_system_id"})})
public class ServiceRegistryEntry {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  //mandatory fields for JSON
  @JoinColumn(name = "arrowhead_service_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  private ArrowheadService providedService;

  @JoinColumn(name = "provider_system_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  private ArrowheadSystem provider;

  //non-mandatory fields for JSON
  @Column(name = "port")
  private Integer port;

  @Column(name = "service_uri")
  private String serviceURI;

  @Column(name = "version")
  private Integer version = 1;

  @Column(name = "udp")
  private boolean UDP = false;

  //Time to live in seconds - endOfValidity is calculated from this upon registering and TTL is calculated from endOfValidity when queried
  @Transient
  private int ttl;

  //only for database
  @Column(name = "metadata")
  private String metadata;

  @Column(name = "end_of_validity")
  private LocalDateTime endOfValidity;

  public ServiceRegistryEntry() {
  }

  public ServiceRegistryEntry(ArrowheadService providedService, ArrowheadSystem provider) {
    this.providedService = providedService;
    this.provider = provider;
  }

  public ServiceRegistryEntry(ArrowheadService providedService, ArrowheadSystem provider, String serviceURI) {
    this.providedService = providedService;
    this.provider = provider;
    this.port = provider.getPort();
    this.serviceURI = serviceURI;
  }

  public ServiceRegistryEntry(ArrowheadService providedService, ArrowheadSystem provider, Integer port, String serviceURI) {
    this.providedService = providedService;
    this.provider = provider;
    this.port = port;
    this.serviceURI = serviceURI;
  }

  public ServiceRegistryEntry(ArrowheadService providedService, ArrowheadSystem provider, Integer port, String serviceURI, Integer version,
                              boolean UDP, int ttl, String metadata, LocalDateTime endOfValidity) {
    this.providedService = providedService;
    this.provider = provider;
    this.port = port;
    this.serviceURI = serviceURI;
    this.version = version;
    this.UDP = UDP;
    this.ttl = ttl;
    this.metadata = metadata;
    this.endOfValidity = endOfValidity;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public ArrowheadService getProvidedService() {
    return providedService;
  }

  public void setProvidedService(ArrowheadService providedService) {
    this.providedService = providedService;
  }

  public ArrowheadSystem getProvider() {
    return provider;
  }

  public void setProvider(ArrowheadSystem provider) {
    this.provider = provider;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }

  public String getServiceURI() {
    return serviceURI;
  }

  public void setServiceURI(String serviceURI) {
    this.serviceURI = serviceURI;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  @JsonGetter("UDP")
  public boolean isUDP() {
    return UDP;
  }

  //We have 2 Setters, so Jackson can parse both upper- and lowercase forms without a problem
  @JsonSetter("UDP")
  public void setUDP(boolean UDP) {
    this.UDP = UDP;
  }

  @JsonSetter("udp")
  private void setUdp(boolean UDP) {
    this.UDP = UDP;
  }

  @JsonGetter("ttl")
  public int getTtl() {
    return ttl;
  }

  @JsonSetter("ttl")
  public void setTtl(int ttl) {
    this.ttl = ttl;
  }

  @JsonSetter("TTL")
  private void setTTL(int ttl) {
    this.ttl = ttl;
  }

  public String getMetadata() {
    return metadata;
  }

  public void setMetadata(String metadata) {
    this.metadata = metadata;
  }

  public LocalDateTime getEndOfValidity() {
    return endOfValidity;
  }

  public void setEndOfValidity(LocalDateTime endOfValidity) {
    this.endOfValidity = endOfValidity;
  }

  @JsonIgnore
  public boolean isValid() {
    return provider != null && provider.isValid() && providedService != null && providedService.isValid();
  }

  @Override
  public String toString() {
    return providedService.getServiceDefinition() + ":" + provider.getSystemName();
  }

  @JsonIgnore
  public void toDatabase() {
    if (providedService.getServiceMetadata() != null && !providedService.getServiceMetadata().isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<String, String> entry : providedService.getServiceMetadata().entrySet()) {
        sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
      }
      metadata = sb.toString().substring(0, sb.length() - 1);
    }

    if (provider.getPort() != 0 && (port == null || port == 0)) {
      port = provider.getPort();
    }

    endOfValidity = ttl > 0 ? LocalDateTime.now().plusSeconds(ttl) : null;
  }

  @JsonIgnore
  public void fromDatabase() {
    ArrowheadService temp = providedService;
    providedService = new ArrowheadService();
    providedService.setServiceDefinition(temp.getServiceDefinition());
    providedService.setInterfaces(temp.getInterfaces());

    if (metadata != null) {
      String[] parts = metadata.split(",");
      providedService.getServiceMetadata().clear();
      for (String part : parts) {
        String[] pair = part.split("=");
        providedService.getServiceMetadata().put(pair[0], pair[1]);
      }
    }

    if (port != null && provider.getPort() == 0) {
      provider.setPort(port);
    }

    if (endOfValidity != null) {
      if (LocalDateTime.now().isAfter(endOfValidity)) {
        ttl = 0;
      } else {
        ttl = (int) Duration.between(LocalDateTime.now(), endOfValidity).toMillis() / 1000;
      }
    } else {
      ttl = 0;
    }
  }

}
