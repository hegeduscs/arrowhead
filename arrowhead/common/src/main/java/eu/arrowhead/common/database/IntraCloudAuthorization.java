package eu.arrowhead.common.database;

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

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

/**
 * @author umlaufz
 * This class maps the intra cloud authorization rights between Arrowhead Systems.
 * The table entry itself is the authorization right.
 * The "arrowhead_service_id", "consumer_system_id" and "provider_system_id" columns must be unique together.
 */
@Entity
@Table(name="intra_cloud_authorization", uniqueConstraints={@UniqueConstraint(columnNames = 
		{"consumer_system_id", "provider_system_id", "arrowhead_service_id"})})
public class IntraCloudAuthorization {
	
	@Column(name="id")
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	@JoinColumn(name="consumer_system_id")
	@ManyToOne(fetch = FetchType.EAGER, cascade={CascadeType.MERGE})
	private ArrowheadSystem consumer;
	
	@JoinColumn(name="provider_system_id")
	@ManyToOne(fetch = FetchType.EAGER, cascade={CascadeType.MERGE})
	private ArrowheadSystem provider;
	
	@JoinColumn(name="arrowhead_service_id")
	@ManyToOne(fetch = FetchType.EAGER, cascade={CascadeType.MERGE})
	private ArrowheadService service;
	
	public IntraCloudAuthorization() {
	}

	public IntraCloudAuthorization(ArrowheadSystem consumer, ArrowheadSystem provider, 
			ArrowheadService service) {
		this.consumer = consumer;
		this.provider = provider;
		this.service = service;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrowheadSystem getConsumer() {
		return consumer;
	}

	public void setConsumer(ArrowheadSystem consumer) {
		this.consumer = consumer;
	}

	public ArrowheadSystem getProvider() {
		return provider;
	}
	
	public void setProvider(ArrowheadSystem providers) {
		this.provider = providers;
	}

	public ArrowheadService getService() {
		return service;
	}

	public void setService(ArrowheadService service) {
		this.service = service;
	}

	
}
