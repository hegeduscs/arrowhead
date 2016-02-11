package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class ProvidedService {

	private ArrowheadSystem provider;
	private String serviceURI;
	private String serviceInterface;

	public ProvidedService() {

	}

	public ProvidedService(ArrowheadSystem provider, String serviceURI, String serviceInterface) {
		this.provider = provider;
		this.serviceURI = serviceURI;
		this.serviceInterface = serviceInterface;
	}

	public ArrowheadSystem getProvider() {
		return provider;
	}

	public void setProvidedr(ArrowheadSystem provider) {
		this.provider = provider;
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
