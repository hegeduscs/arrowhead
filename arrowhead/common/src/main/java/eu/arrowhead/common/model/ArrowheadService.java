package eu.arrowhead.common.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 * Entity class for storing Arrowhead Services in the database.
 * The "service_group" and service_definition" columns must be unique together.
 */
@Entity
@Table(name="arrowhead_service", uniqueConstraints={@UniqueConstraint(columnNames = {"service_group", "service_definition"})})
@XmlRootElement
public class ArrowheadService {

	@Column(name="id")
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;
	
	@Column(name="service_group")
	private String serviceGroup;
	
	@Column(name="service_definition")
	private String serviceDefinition;
	
	@ElementCollection(fetch = FetchType.LAZY)
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<String> interfaces = new ArrayList<String>();
	
	@Transient
	private List<ServiceMetadata> serviceMetadata;
	
	public ArrowheadService(){
	}
	
	public ArrowheadService(String serviceGroup, String serviceDefinition,
			List<String> interfaces, List<ServiceMetadata> serviceMetadata) {
		this.serviceGroup = serviceGroup;
		this.serviceDefinition = serviceDefinition;
		this.interfaces = interfaces;
		this.serviceMetadata = serviceMetadata;
	}

	@XmlTransient
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public List<String> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<String> interfaces) {
		this.interfaces = interfaces;
	}

	public List<ServiceMetadata> getServiceMetadata() {
		return serviceMetadata;
	}

	public void setServiceMetadata(List<ServiceMetadata> metaData) {
		this.serviceMetadata = metaData;
	}
	
	public boolean isValid(){
		if(serviceGroup == null || serviceDefinition == null)
			return false;
		return true;
	}
	
	
}
