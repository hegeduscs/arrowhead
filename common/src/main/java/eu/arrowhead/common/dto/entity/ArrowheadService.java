/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.dto.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Representation of a service within Arrowhead.
 *
 * @author uzoltan
 * @since 4.2
 */
@Entity
public class ArrowheadService {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @NotBlank
  @Size(max = 255)
  private String definition;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "arrowhead_service_interfaces", joinColumns = @JoinColumn(name = "arrowhead_service_id"))
  private Set<String> interfaces = new HashSet<>();

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "arrowhead_service_metadata", joinColumns = @JoinColumn(name = "arrowhead_service_id"))
  private Map<String, String> metadata = new HashMap<>();

  public ArrowheadService() {
  }

  /**
   * Constructor with all the fields of the ArrowheadService class.
   *
   * @param definition A descriptive name for the service
   * @param interfaces The set of interfaces that can be used to consume this service (helps interoperability between
   *     ArrowheadSystems). Concrete meaning of what is an interface is service specific (e.g. JSON, I2C)
   * @param metadata Arbitrary additional metadata belonging to the service, stored as key-value pairs.
   */
  public ArrowheadService(@NotBlank @Size(max = 255) String definition, Set<String> interfaces, Map<String, String> metadata) {
    this.definition = definition;
    this.interfaces = interfaces;
    this.metadata = metadata;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getDefinition() {
    return definition;
  }

  public void setDefinition(String definition) {
    this.definition = definition;
  }

  public Set<String> getInterfaces() {
    return interfaces;
  }

  public void setInterfaces(Set<String> interfaces) {
    this.interfaces = interfaces;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
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

    return definition.equals(that.definition);
  }

  @Override
  public int hashCode() {
    return definition.hashCode();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ArrowheadService{");
    sb.append("id=").append(id);
    sb.append(", definition='").append(definition).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
