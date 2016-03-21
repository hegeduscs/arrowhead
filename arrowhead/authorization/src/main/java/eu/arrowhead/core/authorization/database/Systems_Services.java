package eu.arrowhead.core.authorization.database;

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

/**
 * @author umlaufz
 * This class maps the intra cloud authorization rights between Arrowhead Systems.
 * The table entry itself is the authorization right.
 * The "service_id", "consumer_id" and "provider_id" columns must be unique together.
 */
@Entity
@Table(name="systems_services", uniqueConstraints={@UniqueConstraint(columnNames = 
		{"consumer_id", "provider_id", "service_id"})})
public class Systems_Services {
	
	@Column(name="id")
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	@JoinColumn(name="consumer_id")
	@ManyToOne(fetch = FetchType.EAGER, cascade={CascadeType.MERGE})
	private ArrowheadSystem consumer;
	
	@JoinColumn(name="provider_id")
	@ManyToOne(fetch = FetchType.EAGER, cascade={CascadeType.MERGE})
	private ArrowheadSystem provider;
	
	@JoinColumn(name="service_id")
	@ManyToOne(fetch = FetchType.EAGER, cascade={CascadeType.MERGE})
	private ArrowheadService service;
	
	public Systems_Services() {
	}

	public Systems_Services(ArrowheadSystem consumer, ArrowheadSystem provider, 
			ArrowheadService service) {
		this.consumer = consumer;
		this.provider = provider;
		this.service = service;
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
