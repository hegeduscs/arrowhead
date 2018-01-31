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
import eu.arrowhead.common.json.support.ArrowheadServiceSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 * Entity class for storing Arrowhead Services in the database. The "service_group" and service_definition" columns must be unique together.
 */
@Entity
@Table(name = "arrowhead_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"service_definition"})})
public class ArrowheadService {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @Column(name = "service_definition")
  private String serviceDefinition;

  @ElementCollection(fetch = FetchType.LAZY)
  @LazyCollection(LazyCollectionOption.FALSE)
  @CollectionTable(name = "arrowhead_service_interface_list", joinColumns = @JoinColumn(name = "arrowhead_service_id"))
  private List<String> interfaces = new ArrayList<>();

  @Transient
  private Map<String, String> serviceMetadata = new HashMap<>();

  public ArrowheadService() {
  }

  public ArrowheadService(String serviceDefinition, List<String> interfaces, Map<String, String> serviceMetadata) {
    this.serviceDefinition = serviceDefinition;
    this.interfaces = interfaces;
    this.serviceMetadata = serviceMetadata;
  }

  public ArrowheadService(ArrowheadServiceSupport service) {
    this.serviceDefinition = service.getServiceGroup() + "_" + service.getServiceDefinition();
    this.interfaces = service.getInterfaces();
    this.serviceMetadata = service.getServiceMetadata();
  }

  @XmlTransient
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getServiceDefinition() {
    return serviceDefinition;
  }

  public void setServiceDefinition(String serviceDefinition) {
    this.serviceDefinition = serviceDefinition;
  }

  public List<String> getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(List<String> interfaces) {
    this.interfaces = interfaces;
  }

  public void setOneInterface(String oneInterface) {
    this.interfaces.clear();
    this.interfaces.add(oneInterface);
  }

  public Map<String, String> getServiceMetadata() {
    return serviceMetadata;
  }

  public void setServiceMetadata(Map<String, String> metaData) {
    this.serviceMetadata = metaData;
  }

  @JsonIgnore
  public boolean isValid() {
    return (serviceDefinition != null && !interfaces.isEmpty());
  }

  @JsonIgnore
  public boolean isValidForDatabase() {
    return serviceDefinition != null;
  }

  @JsonIgnore
  public boolean isValidForDNSSD() {
    boolean areInterfacesClean = true;
    for (String interf : interfaces) {
      if (interf.contains("_")) {
        areInterfacesClean = false;
      }
    }

    return (serviceDefinition != null && !interfaces.isEmpty() && !serviceDefinition.contains("_") && areInterfacesClean);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ArrowheadService that = (ArrowheadService) o;

    return serviceDefinition.equals(that.serviceDefinition);
  }

  @Override
  public int hashCode() {
    return serviceDefinition.hashCode();
  }

  @Override
  public String toString() {
    return "\"" + serviceDefinition + "\"";
  }

}
