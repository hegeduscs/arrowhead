package eu.arrowhead.common.model.serviceregistry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Provider {

	@XmlElement(name = "ArrowheadSystem")
	ArrowheadSystem arrowheadSystem;
	@XmlElement(name = "ServiceURI")
	String serviceURI;

	public ArrowheadSystem getArrowheadSystem() {
		return arrowheadSystem;
	}

	public void setArrowheadSystem(ArrowheadSystem arrowheadSystem) {
		this.arrowheadSystem = arrowheadSystem;
	}

	public String getServiceURI() {
		return serviceURI;
	}

	public void setServiceURI(String serviceURI) {
		this.serviceURI = serviceURI;
	}

}
