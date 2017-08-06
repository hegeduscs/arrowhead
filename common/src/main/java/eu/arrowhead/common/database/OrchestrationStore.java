package eu.arrowhead.common.database;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import java.util.Date;
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
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;
import org.hibernate.annotations.Type;

/**
 * Entity class for storing Orchestration Store entries in the database. The name column must be unique. TODO do proper javadoc when this class is
 * finalized
 *
 * @author Umlauf Zoltán
 */
@Entity
@Table(name = "orchestration_store", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"arrowhead_service_id", "consumer_system_id", "provider_system_id", "provider_cloud_id", "priority",
        "is_default"})})
public class OrchestrationStore implements Comparable<OrchestrationStore> {

  @Column(name = "id")
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private int id;

  @JoinColumn(name = "arrowhead_service_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  private ArrowheadService service;

  @JoinColumn(name = "consumer_system_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  private ArrowheadSystem consumer;

  @JoinColumn(name = "provider_system_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  private ArrowheadSystem providerSystem;

  @JoinColumn(name = "provider_cloud_id")
  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  private ArrowheadCloud providerCloud;

  @Column(name = "priority")
  private int priority;

  @Column(name = "is_default")
  private boolean isDefault;

  @Column(name = "name")
  private String name;

  @Column(name = "last_updated")
  @Type(type = "timestamp")
  private Date lastUpdated;

  @Column(name = "orchestration_rule")
  private String orchestrationRule;

  @ElementCollection(fetch = FetchType.LAZY)
  @MapKeyColumn(name = "attribute_key")
  @Column(name = "attribute_value")
  @CollectionTable(name = "orchestration_store_attributes", joinColumns = @JoinColumn(name = "id"))
  private Map<String, String> attributes = new HashMap<>();

  @Column(name = "service_uri")
  private String serviceUri;

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
                            int priority, boolean isDefault, String name, Date lastUpdated, String orchestrationRule,
                            Map<String, String> attributes, String serviceUri) {
    this.service = service;
    this.consumer = consumer;
    this.providerSystem = providerSystem;
    this.providerCloud = providerCloud;
    this.priority = priority;
    this.isDefault = isDefault;
    this.name = name;
    this.lastUpdated = lastUpdated;
    this.orchestrationRule = orchestrationRule;
    this.attributes = attributes;
    this.serviceUri = serviceUri;
  }

  @XmlTransient
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
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

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public boolean isDefault() {
    return isDefault;
  }

  public void setDefault(boolean aDefault) {
    isDefault = aDefault;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  public String getOrchestrationRule() {
    return orchestrationRule;
  }

  public void setOrchestrationRule(String orchestrationRule) {
    this.orchestrationRule = orchestrationRule;
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  public String getServiceUri() {
    return serviceUri;
  }

  public void setServiceUri(String serviceUri) {
    this.serviceUri = serviceUri;
  }

  public boolean isValid() {
    return service != null && consumer != null && providerSystem != null && service.isValid() && consumer.isValid() && providerSystem.isValid()
        && priority >= 1 && (!isDefault || providerCloud == null);
  }

  @Override
  public int compareTo(OrchestrationStore other) {
    return this.priority - other.priority;
  }

}
