/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.database;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Representation of a service within Arrowhead.
 *
 * @author uzoltan
 * @since 4.2
 */
@Entity
@Table(name = "arrowhead_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"service_definition"})})
public class ArrowheadService {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @NotBlank
  @Size(max = 255, message = "Service serviceDefinition must be 255 character at max")
  @Column(name = "service_definition")
  private String serviceDefinition;

  @Size(max = 100, message = "Service can only have 100 interfaces at max")
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "arrowhead_service_interfaces", joinColumns = @JoinColumn(name = "arrowhead_service_id"))
  private Set<@NotBlank String> interfaces = new HashSet<>();

  @Size(max = 100, message = "Service can only have 100 serviceMetadata key-value pairs at max")
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "arrowhead_service_metadata", joinColumns = @JoinColumn(name = "arrowhead_service_id"))
  private Map<@NotBlank String, @NotBlank String> serviceMetadata = new HashMap<>();

  public ArrowheadService() {
  }

  /**
   * Constructor with all the fields of the ArrowheadService class.
   *
   * @param serviceDefinition A descriptive name for the service
   * @param interfaces The set of interfaces that can be used to consume this service (helps interoperability between
   *     ArrowheadSystems). Concrete meaning of what is an interface is service specific (e.g. JSON, I2C)
   * @param serviceMetadata Arbitrary additional serviceMetadata belonging to the service, stored as key-value pairs.
   */
  public ArrowheadService(String serviceDefinition, Set<String> interfaces, Map<String, String> serviceMetadata) {
    this.serviceDefinition = serviceDefinition;
    this.interfaces = interfaces;
    this.serviceMetadata = serviceMetadata;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getServiceDefinition() {
    return serviceDefinition;
  }

  public void setServiceDefinition(String serviceDefinition) {
    this.serviceDefinition = serviceDefinition;
  }

  public Set<String> getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(Set<String> interfaces) {
    this.interfaces = interfaces;
  }

  public Map<String, String> getServiceMetadata() {
    return serviceMetadata;
  }

  public void setServiceMetadata(Map<String, String> serviceMetadata) {
    this.serviceMetadata = serviceMetadata;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArrowheadService)) {
      return false;
    }

    ArrowheadService that = (ArrowheadService) o;

    if (!serviceDefinition.equals(that.serviceDefinition)) {
      return false;
    }

    //2 services can be equal if they have at least 1 common interface
    Set<String> intersection = new HashSet<>(interfaces);
    intersection.retainAll(that.interfaces);
    return !intersection.isEmpty();
  }

  @Override
  public int hashCode() {
    return serviceDefinition.hashCode();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ArrowheadService{");
    sb.append("id=").append(id);
    sb.append(", serviceDefinition='").append(serviceDefinition).append('\'');
    sb.append('}');
    return sb.toString();
  }
}