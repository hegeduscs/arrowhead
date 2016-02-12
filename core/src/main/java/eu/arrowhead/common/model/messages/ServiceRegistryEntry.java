package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class ServiceRegistryEntry {

	ArrowheadSystem provider;
	String serviceURI;
	String serviceMetadata;
	String tSIG_key;

	public ServiceRegistryEntry() {
		super();
	}

	public ServiceRegistryEntry(ArrowheadSystem provider, String serviceURI, String serviceMetadata, String tSIG_key) {
		super();
		this.provider = provider;
		this.serviceURI = serviceURI;
		this.serviceMetadata = serviceMetadata;
		this.tSIG_key = tSIG_key;
	}

	public ArrowheadSystem getProvider() {
		return provider;
	}

	public void setProvider(ArrowheadSystem provider) {
		this.provider = provider;
	}

	public String getServiceURI() {
		return serviceURI;
	}

	public void setServiceURI(String serviceURI) {
		this.serviceURI = serviceURI;
	}

	public String getServiceMetadata() {
		return serviceMetadata;
	}

	public void setServiceMetadata(String serviceMetadata) {
		this.serviceMetadata = serviceMetadata;
	}

	public String getTSIG_key() {
		return tSIG_key;
	}

	public void setTSIG_key(String tSIG_key) {
		this.tSIG_key = tSIG_key;
	}

}
