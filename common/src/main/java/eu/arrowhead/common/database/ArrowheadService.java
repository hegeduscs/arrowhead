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
import eu.arrowhead.common.json.support.ArrowheadServiceSupport;
import eu.arrowhead.common.messages.ArrowheadBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 * Entity class for storing Arrowhead Services in the database. The "service_definition" column must be unique.
 */
@Entity
@JsonIgnoreProperties({"alwaysMandatoryFields"})
@Table(name = "arrowhead_service", uniqueConstraints = {@UniqueConstraint(columnNames = {"service_definition"})})
public class ArrowheadService extends ArrowheadBase {

  @Transient
  private static final Set<String> alwaysMandatoryFields = new HashSet<>(Arrays.asList("serviceDefinition"));

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

  public Set<String> missingFields(boolean throwException, boolean forDNSSD, Set<String> mandatoryFields) {
    if (mandatoryFields == null) {
      mandatoryFields = new HashSet<>(alwaysMandatoryFields);
    }
    mandatoryFields.addAll(alwaysMandatoryFields);
    Set<String> nonNullFields = getFieldNamesWithNonNullValue();
    mandatoryFields.removeAll(nonNullFields);

    if (forDNSSD) {
      for (String interf : interfaces) {
        if (interf.contains("_")) {
          mandatoryFields.add("Interfaces Can't Have Underscores!");
        }
      }
    }

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
