package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;

@XmlRootElement
public class GSDEntry {
	
	ArrowheadCloud cloud;
	ArrowheadService service;
	
	public GSDEntry() {
		super();
	}

	public GSDEntry(ArrowheadCloud cloud, ArrowheadService service) {
		this.cloud = cloud;
		this.service = service;
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
