package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class ProvidedInterface {

	ArrowheadSystem ProvidedSystem;
	String ServiceURI;
	String ServiceInterface;

	public ProvidedInterface() {

	}

	public ProvidedInterface(ArrowheadSystem providedSystem, String serviceURI, String serviceInterface) {
		ProvidedSystem = providedSystem;
		ServiceURI = serviceURI;
		ServiceInterface = serviceInterface;
	}

	public ArrowheadSystem getProvidedSystem() {
		return ProvidedSystem;
	}

	public void setProvidedSystem(ArrowheadSystem providedSystem) {
		ProvidedSystem = providedSystem;
	}

	public String getServiceURI() {
		return ServiceURI;
	}

	public void setServiceURI(String serviceURI) {
		ServiceURI = serviceURI;
	}

	public String getServiceInterface() {
		return ServiceInterface;
	}

	public void setServiceInterface(String serviceInterface) {
		ServiceInterface = serviceInterface;
	}

}
