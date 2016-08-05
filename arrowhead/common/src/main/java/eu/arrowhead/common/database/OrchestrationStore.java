package eu.arrowhead.common.database;

import java.util.Date;

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

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.Type;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

/**
 * @author umlaufz
 * 
 * Entity class for storing Orchestration Store entries in the database.
 * The name column must be unique.
 */
@Entity
@Table(name="orchestration_store", uniqueConstraints={@UniqueConstraint(
		columnNames = {"consumer_system_id", "arrowhead_service_id", "provider_system_id",
				"provider_cloud_id", "priority", "is_active"})})
public class OrchestrationStore implements Comparable<OrchestrationStore>{
	
	@Column(name="id")
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;
	
	@JoinColumn(name="consumer_system_id")
	@ManyToOne(fetch = FetchType.EAGER, cascade=CascadeType.MERGE)
	private ArrowheadSystem consumer;
	
	@JoinColumn(name="arrowhead_service_id")
	@ManyToOne(fetch = FetchType.EAGER, cascade=CascadeType.MERGE)
	private ArrowheadService service;
	
	@JoinColumn(name="provider_system_id")
	@ManyToOne(fetch = FetchType.EAGER, cascade=CascadeType.MERGE)
	@NotFound(action = NotFoundAction.IGNORE)
	private ArrowheadSystem providerSystem;
	
	@JoinColumn(name="provider_cloud_id")
	@ManyToOne(fetch = FetchType.EAGER, cascade=CascadeType.MERGE)
	@NotFound(action = NotFoundAction.IGNORE)
	private ArrowheadCloud providerCloud;
	
	@Column(name="priority")
	private Integer priority;
	
	@Column(name="is_active")
	private boolean isActive;
	
	@Column(name="name")
	private String name;
	
	@Column(name="last_updated")
	@Type(type="timestamp")
	private Date lastUpdated;
	
	@Column(name="orchestration_rule")
	private String orchestrationRule;

	public OrchestrationStore(){
	}
	
	public OrchestrationStore(ArrowheadSystem consumer, ArrowheadService service,
			ArrowheadSystem providerSystem, ArrowheadCloud providerCloud, Integer priority) {
		this.consumer = consumer;
		this.service = service;
		this.providerSystem = providerSystem;
		this.providerCloud = providerCloud;
		this.priority = priority;
	}

	public OrchestrationStore(ArrowheadSystem consumer, ArrowheadService service, 
			ArrowheadSystem providerSystem, ArrowheadCloud providerCloud, Integer priority, 
			boolean isActive, String name, Date lastUpdated, String orchestrationRule) {
		this.consumer = consumer;
		this.service = service;
		this.providerSystem = providerSystem;
		this.providerCloud = providerCloud;
		this.priority = priority;
		this.isActive = isActive;
		this.name = name;
		this.lastUpdated = lastUpdated;
		this.orchestrationRule = orchestrationRule;
	}

	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	public ArrowheadSystem getConsumer() {
		return consumer;
	}

	public void setConsumer(ArrowheadSystem consumer) {
		this.consumer = consumer;
	}

	public ArrowheadService getService() {
		return service;
	}

	public void setService(ArrowheadService service) {
		this.service = service;
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

	public boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
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

	public boolean isPayloadUsable(){
		if(consumer == null || service == null || !consumer.isValid() || !service.isValid())
			return false;
		if(priority == null || priority < 0)
			return false;
		if(isActive && (providerCloud != null && providerCloud.isValid()))
			return false;
		if((providerSystem == null || !providerSystem.isValid()) && 
				(providerCloud == null || !providerCloud.isValid()))
			return false;
		return true;
	}

	@Override
	public int compareTo(OrchestrationStore other) {
		return this.priority - other.priority;
	}
	
}
