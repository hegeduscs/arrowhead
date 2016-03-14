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
 * This class maps the inter cloud authorization rights.
 * The table entry itself is the authorization right.
 * The "cloud_id" and "service_id" columns must be unique together.
 */
@Entity
@Table(name="clouds_services", uniqueConstraints={@UniqueConstraint(columnNames = {"cloud_id", "service_id"})})
public class Clouds_Services {
	
	@Column(name="id")
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	
	@JoinColumn(name="cloud_id")
	@ManyToOne(fetch = FetchType.EAGER, cascade={CascadeType.MERGE})
	private ArrowheadCloud cloud;
	
	@JoinColumn(name="service_id")
	@ManyToOne(fetch = FetchType.EAGER, cascade={CascadeType.MERGE})
	private ArrowheadService service;
	
	public Clouds_Services() {
	}

	public Clouds_Services(ArrowheadCloud cloud, ArrowheadService service) {
		this.cloud = cloud;
		this.service = service;
	}
	
	public int getId() {
		return id;
	}

	public ArrowheadCloud getCloud() {
		return cloud;
	}

	public void setCloud(ArrowheadCloud cloud) {
		this.cloud = cloud;
	}

	public ArrowheadService getService() {
		return service;
	}

	public void setService(ArrowheadService service) {
		this.service = service;
	}

	
}
