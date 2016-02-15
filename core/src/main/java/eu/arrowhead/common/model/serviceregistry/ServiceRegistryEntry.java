package eu.arrowhead.common.model.serviceregistry;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServiceRegistryEntry {

	@XmlElement(name = "Provider")
	ArrowheadSystem arrowheadSystem;
	@XmlElement(name = "ServiceURI")
	String serviceURI;
	@XmlElement(name = "ServiceMeataData")
	String serviceMeataData;
	@XmlElement(name = "TSIG_key")
	String TSigKey;
	@XmlElement(name = "Version")
	String version;

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

	public String getServiceMeataData() {
		return serviceMeataData;
	}

	public void setServiceMeataData(String serviceMeataData) {
		this.serviceMeataData = serviceMeataData;
	}

	public String getTSigKey() {
		return TSigKey;
	}

	public void setTSigKey(String tSigKey) {
		TSigKey = tSigKey;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
