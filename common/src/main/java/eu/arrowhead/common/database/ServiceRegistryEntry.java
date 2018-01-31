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
import java.util.List;
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
import javax.xml.bind.annotation.XmlTransient;

@Entity
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

  @JsonIgnore
  @Column(name = "metadata")
  private String metadata;

  @Column(name = "version")
  private Integer version = 1;

  @Column(name = "udp")
  private boolean UDP = false;

  //only for backwards compatibility, used by DNS-SD
  @Transient
  private List<String> interfaces;
  @Transient
  private String TSIG_key;

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

  public ServiceRegistryEntry(ArrowheadService providedService, ArrowheadSystem provider, Integer port, String serviceURI, String metadata, Integer
      version, boolean UDP) {
    this.providedService = providedService;
    this.provider = provider;
    this.port = port;
    this.serviceURI = serviceURI;
    this.metadata = metadata;
    this.version = version;
    this.UDP = UDP;
  }

  @XmlTransient
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

  public String getMetadata() {
    return metadata;
  }

  public void setMetadata(String metadata) {
    this.metadata = metadata;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public boolean isUDP() {
    return UDP;
  }

  public void setUDP(boolean UDP) {
    this.UDP = UDP;
  }

  public List<String> getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(List<String> interfaces) {
    this.interfaces = interfaces;
  }

  public String getTSIG_key() {
    return TSIG_key;
  }

  public void setTSIG_key(String TSIG_key) {
    this.TSIG_key = TSIG_key;
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
    if (!providedService.getServiceMetadata().isEmpty()) {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<String, String> entry : providedService.getServiceMetadata().entrySet()) {
        sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
      }
      metadata = sb.toString().substring(0, sb.length() - 1);
    }
    if (provider.getPort() != 0 && port == 0) {
      port = provider.getPort();
    }
  }

  @JsonIgnore
  public void fromDatabase() {
    if (metadata != null) {
      String[] parts = metadata.split(",");
      for (String part : parts) {
        String[] pair = part.split("=");
        this.providedService.getServiceMetadata().put(pair[0], pair[1]);
      }
    }
    if (port != null && provider.getPort() == 0) {
      provider.setPort(port);
    }
  }
}
