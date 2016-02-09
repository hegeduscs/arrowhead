package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class ServiceRegistryEntry {

	ArrowheadSystem Provider;
	String ServiceURI;
	String ServiceMetadata;
	String TSIG_key;

	public ServiceRegistryEntry(){
		
	}
	
	public ServiceRegistryEntry(ArrowheadSystem provider, String serviceURI, String serviceMetadata, String tSIG_key) {
		Provider = provider;
		ServiceURI = serviceURI;
		ServiceMetadata = serviceMetadata;
		TSIG_key = tSIG_key;
	}

	public ArrowheadSystem getProvider() {
		return Provider;
	}

	public void setProvider(ArrowheadSystem provider) {
		Provider = provider;
	}

	public String getServiceURI() {
		return ServiceURI;
	}

	public void setServiceURI(String serviceURI) {
		ServiceURI = serviceURI;
	}

	public String getServiceMetadata() {
		return ServiceMetadata;
	}

	public void setServiceMetadata(String serviceMetadata) {
		ServiceMetadata = serviceMetadata;
	}

	public String getTSIG_key() {
		return TSIG_key;
	}

	public void setTSIG_key(String tSIG_key) {
		TSIG_key = tSIG_key;
	}

}
