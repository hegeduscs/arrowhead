package eu.arrowhead.common.database;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
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
@Table(name="orchestration_store")
public class OrchestrationStore implements Comparable<OrchestrationStore>{
	
	@Column(name="id")
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
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
	
	@Column(name="is_inter_cloud")
	private boolean isInterCloud;
	
	@Column(name="is_active")
	private boolean isActive;
	
	@Column(name="name", unique = true)
	private String name;
	
	/*
	 * Used for prioritizing between entries.
	 */
	@Column(name="serial_number")
	private Integer serialNumber;
	
	@Column(name="last_updated")
	@Type(type="timestamp")
	private Date lastUpdated;
	
	@ElementCollection(fetch = FetchType.LAZY)
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<String> orchestrationRule;

	public OrchestrationStore(){
	}

	public OrchestrationStore(ArrowheadSystem consumer, ArrowheadService service, 
			ArrowheadSystem providerSystem, ArrowheadCloud providerCloud, boolean isInterCloud, 
			boolean isActive, String name, Integer serialNumber, Date lastUpdated, 
			List<String> orchestrationRule) {
		this.consumer = consumer;
		this.service = service;
		this.providerSystem = providerSystem;
		this.providerCloud = providerCloud;
		this.isInterCloud = isInterCloud;
		this.isActive = isActive;
		this.name = name;
		this.serialNumber = serialNumber;
		this.lastUpdated = lastUpdated;
		this.orchestrationRule = orchestrationRule;
	}
	
	public int getId() {
		return id;
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

	public boolean getIsInterCloud() {
		return isInterCloud;
	}

	public void setIsInterCloud(boolean isInterCloud) {
		this.isInterCloud = isInterCloud;
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

	public Integer getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(Integer serialNumber) {
		this.serialNumber = serialNumber;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public List<String> getOrchestrationRule() {
		return orchestrationRule;
	}

	public void setOrchestrationRule(List<String> orchestrationRule) {
		this.orchestrationRule = orchestrationRule;
	}

	public boolean isPayloadUsable(){
		if(consumer == null || service == null || name == null || 
				!consumer.isValid() || !service.isValid())
			return false;
		if(!isInterCloud && (providerSystem == null || !providerSystem.isValid()))
			return false;
		if(isInterCloud && (providerCloud == null || !providerCloud.isValid()))
			return false;
		if(serialNumber < 0)
			return false;
		return true;
	}

	@Override
	public int compareTo(OrchestrationStore other) {
		return this.serialNumber - other.serialNumber;
	}
	
}
