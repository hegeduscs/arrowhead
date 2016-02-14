package eu.arrowhead.core.authorization.database;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames = {"service_id", "consumer_id", "provider_id"})})
public class Systems_Services {
	
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	@ManyToOne(fetch = FetchType.EAGER, cascade={CascadeType.MERGE})
	@JoinColumn(name="service_id")
	private ArrowheadService service;
	@ManyToOne(fetch = FetchType.EAGER, cascade={CascadeType.MERGE})
	@JoinColumn(name="consumer_id")
	private ArrowheadSystem consumer;
	@ManyToOne(fetch = FetchType.EAGER, cascade={CascadeType.MERGE})
	@JoinColumn(name="provider_id")
	private ArrowheadSystem provider;
	
	public Systems_Services() {
		super();
	}

	public Systems_Services(ArrowheadService service, ArrowheadSystem consumer, ArrowheadSystem provider) {
		super();
		this.service = service;
		this.consumer = consumer;
		this.provider = provider;
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

	public ArrowheadSystem getProvider() {
		return provider;
	}
	
	public void setProvider(ArrowheadSystem providers) {
		this.provider = providers;
	}


}
