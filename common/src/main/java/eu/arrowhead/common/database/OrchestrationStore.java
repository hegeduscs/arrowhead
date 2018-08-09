/*
 *  Copyright (c) 2018 AITIA International Inc.
 *
 *  This work is part of the Productive 4.0 innovation project, which receives grants from the
 *  European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 *  (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 *  national funding authorities from involved countries.
 */

package eu.arrowhead.common.database;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import eu.arrowhead.common.exception.BadPayloadException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

/**
 * JPA entity class for storing <tt>OrchestrationStore</tt> information in the database. The <i>arrowhead_service_id</i>, <i>consumer_system_id</i>,
 * <i>priority</i> and <i>is_default</i> columns must be unique together. The <i>priority</i> integer can not be negative. <p> The class implements
 * the <tt>Comparable</tt> interface based on the priority field (but does not override the equals() method).
 *
 * @author Umlauf Zoltán
 */
@Entity
@Table(name = "orchestration_store", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"arrowhead_service_id", "consumer_system_id", "priority", "is_default"})})
@Check(constraints = "provider_cloud_id IS NULL OR is_default = false")
public class OrchestrationStore implements Comparable<OrchestrationStore> {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @Valid
  @NotNull
  @JoinColumn(name = "arrowhead_service_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ArrowheadService service;

  @Valid
  @NotNull
  @JoinColumn(name = "consumer_system_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ArrowheadSystem consumer;

  @Valid
  @NotNull
  @JoinColumn(name = "provider_system_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ArrowheadSystem providerSystem;

  @Valid
  @JoinColumn(name = "provider_cloud_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ArrowheadCloud providerCloud;

  @Size(min = 1, message = "Priority can not be less than 1")
  private int priority;

  @Column(name = "is_default")
  @Type(type = "yes_no")
  private boolean defaultEntry;

  private String name;

  @Column(name = "last_updated")
  private LocalDateTime lastUpdated;

  private String instruction;

  @JsonInclude(Include.NON_EMPTY)
  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyColumn(name = "attribute_key")
  @Column(name = "attribute_value", length = 2047)
  @CollectionTable(name = "orchestration_store_attributes", joinColumns = @JoinColumn(name = "store_entry_id"))
  private Map<String, String> attributes = new HashMap<>();

  //Only to convert ServiceRegistryEntries to Store entries without data loss
  @Transient
  private String serviceURI;

  public OrchestrationStore() {
  }

  public OrchestrationStore(ArrowheadService service, ArrowheadSystem consumer, ArrowheadSystem providerSystem, ArrowheadCloud providerCloud,
                            int priority) {
    this.service = service;
    this.consumer = consumer;
    this.providerSystem = providerSystem;
    this.providerCloud = providerCloud;
    this.priority = priority;
  }

  public OrchestrationStore(ArrowheadService service, ArrowheadSystem consumer, ArrowheadSystem providerSystem, ArrowheadCloud providerCloud,
                            int priority, boolean defaultEntry, String name, LocalDateTime lastUpdated, String instruction,
                            Map<String, String> attributes, String serviceURI) {
    this.service = service;
    this.consumer = consumer;
    this.providerSystem = providerSystem;
    this.providerCloud = providerCloud;
    this.priority = priority;
    this.defaultEntry = defaultEntry;
    this.name = name;
    this.lastUpdated = lastUpdated;
    this.instruction = instruction;
    this.attributes = attributes;
    this.serviceURI = serviceURI;
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

  public ArrowheadSystem getConsumer() {
    return consumer;
  }

  public void setConsumer(ArrowheadSystem consumer) {
    this.consumer = consumer;
  }

  public ArrowheadSystem getProviderSystem() {
    return providerSystem;
  }

  public void setProviderSystem(ArrowheadSystem providerSystem) {
    this.providerSystem = providerSystem;
  }

  public ArrowheadCloud getProviderCloud() {
    return providerCloud;
  }

  public void setProviderCloud(ArrowheadCloud providerCloud) {
    this.providerCloud = providerCloud;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public boolean isDefaultEntry() {
    return defaultEntry;
  }

  public void setDefaultEntry(boolean defaultEntry) {
    this.defaultEntry = defaultEntry;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LocalDateTime getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(LocalDateTime lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public String getInstruction() {
    return instruction;
  }

  public void setInstruction(String instruction) {
    this.instruction = instruction;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  public String getServiceURI() {
    return serviceURI;
  }

  public void setServiceURI(String serviceURI) {
    this.serviceURI = serviceURI;
  }

  /**
   * Note: This class has a natural ordering that is inconsistent with equals(). <p> The field <i>priority</i> is used to sort instances of this class
   * in a collection. Priority is non-negative. If this.priority < other.priority that means <i>this</i> is more ahead in a collection than
   * <i>other</i> and therefore has a higher priority. This means priority = 0 is the highest priority for a Store entry.
   */
  @Override
  public int compareTo(OrchestrationStore other) {
    return this.priority - other.priority;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OrchestrationStore)) {
      return false;
    }

    OrchestrationStore that = (OrchestrationStore) o;

    if (priority != that.priority) {
      return false;
    }
    if (defaultEntry != that.defaultEntry) {
      return false;
    }
    if (!service.equals(that.service)) {
      return false;
    }
    if (!consumer.equals(that.consumer)) {
      return false;
    }
    if (!providerSystem.equals(that.providerSystem)) {
      return false;
    }
    return providerCloud != null ? providerCloud.equals(that.providerCloud) : that.providerCloud == null;
  }

  @Override
  public int hashCode() {
    int result = service.hashCode();
    result = 31 * result + consumer.hashCode();
    result = 31 * result + providerSystem.hashCode();
    result = 31 * result + (providerCloud != null ? providerCloud.hashCode() : 0);
    result = 31 * result + priority;
    result = 31 * result + (defaultEntry ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("OrchestrationStore{");
    sb.append("service=").append(service);
    sb.append(", consumer=").append(consumer);
    sb.append(", priority=").append(priority);
    sb.append(", defaultEntry=").append(defaultEntry);
    sb.append('}');
    return sb.toString();
  }

  public void validateCrossParameterConstraints() {
    if (defaultEntry && providerCloud != null) {
      throw new BadPayloadException("Default store entries can only have intra-cloud providers!");
    }
  }
}
