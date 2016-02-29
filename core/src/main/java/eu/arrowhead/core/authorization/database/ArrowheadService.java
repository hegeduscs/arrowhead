package eu.arrowhead.core.authorization.database;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
public class ArrowheadService {

	@Id @GeneratedValue(strategy=GenerationType.AUTO)
    @XmlTransient
	private int id;
	private String serviceGroup;
	private String serviceDefinition;
	@ElementCollection(fetch = FetchType.LAZY)
	@LazyCollection(LazyCollectionOption.FALSE)
	@Column(name="interface")
	private List<String> interfaces = new ArrayList<String>();
	private String metaData;
	
	public ArrowheadService(){
		
	}
	
	public ArrowheadService(String serviceGroup, String serviceDefinition,
			List<String> interfaces, String metaData) {
		super();
		this.serviceGroup = serviceGroup;
		this.serviceDefinition = serviceDefinition;
		this.interfaces = interfaces;
		this.metaData = metaData;
	}

	public void setId(int id) {
		this.id = id;
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

	public List<String> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(List<String> interfaces) {
		this.interfaces = interfaces;
	}

	public String getMetaData() {
		return metaData;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}
	
	public boolean isEqual(ArrowheadService requestedService){
		boolean sg = (this.serviceGroup.equals(requestedService.getServiceGroup()));
		boolean sd = (this.serviceDefinition.equals(requestedService.getServiceDefinition()));
		
		boolean interfaces = false;
		
		for (int i =0 ; i < this.interfaces.size(); i++) {
			for (int j = 0; j < requestedService.getInterfaces().size(); j++ ) {
				if (this.interfaces.get(i).equals(requestedService.getInterfaces().get(j))) 
					interfaces = true;
			}
		}
		
		return sd && sg && interfaces;
	}
	
}
