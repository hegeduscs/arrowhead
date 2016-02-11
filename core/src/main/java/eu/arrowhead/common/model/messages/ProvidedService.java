package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class ProvidedService {

	private ArrowheadSystem providedSystem;
	private String serviceURI;
	private String serviceInterface;

	public ProvidedService() {

	}

	public ProvidedService(ArrowheadSystem providedSystem, String serviceURI, String serviceInterface) {
		this.providedSystem = providedSystem;
		this.serviceURI = serviceURI;
		this.serviceInterface = serviceInterface;
	}

	public ArrowheadSystem getProvidedSystem() {
		return providedSystem;
	}

	public void setProvidedSystem(ArrowheadSystem providedSystem) {
		this.providedSystem = providedSystem;
	}

	public String getServiceURI() {
		return serviceURI;
	}

	public void setServiceURI(String serviceURI) {
		this.serviceURI = serviceURI;
	}

	public String getServiceInterface() {
		return serviceInterface;
	}

	public void setServiceInterface(String serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

}
