package eu.arrowhead.core.authorization.database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author umlaufz
 * Entity class for storing Arrowhead Services in the database.
 * The "service_group" and service_definition" columns must be unique together.
 */
@Entity
@Table(name="arrowhead_service", uniqueConstraints={@UniqueConstraint(columnNames = {"service_group", "service_definition"})})
@XmlRootElement
public class ArrowheadService {

	@Column(name="id")
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    @XmlTransient
	private int id;
	
	@Column(name="service_group")
	private String serviceGroup;
	
	@Column(name="service_definition")
	private String serviceDefinition;
	
	@Column(name="meta_data")
	private String metaData;
	
	public ArrowheadService(){
	}
	
	public ArrowheadService(String serviceGroup, String serviceDefinition, 
			String metaData) {
		this.serviceGroup = serviceGroup;
		this.serviceDefinition = serviceDefinition;
		this.metaData = metaData;
	}

	public int getId() {
		return id;
	}

	public String getServiceGroup() {
		return serviceGroup;
	}

	public void setServiceGroup(String serviceGroup) {
		this.serviceGroup = serviceGroup;
	}

	public String getServiceDefinition() {
		return serviceDefinition;
	}

	public void setServiceDefinition(String serviceDefinition) {
		this.serviceDefinition = serviceDefinition;
	}

	public String getMetaData() {
		return metaData;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}
	
	
}
