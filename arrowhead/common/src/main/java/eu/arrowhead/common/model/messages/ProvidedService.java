package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class ProvidedService {

	private ArrowheadSystem provider;
	private ArrowheadService offered;
	private String serviceURI;
	private String serviceInterface;

	public ProvidedService() {

	}

	public ProvidedService(ArrowheadSystem provider, ArrowheadService offered, String serviceURI, String serviceInterface) {
		this.provider = provider;
		this.offered = offered;
		this.serviceURI = serviceURI;
		this.serviceInterface = serviceInterface;
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

	public String getServiceInterface() {
		return serviceInterface;
	}

	public void setServiceInterface(String serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	public ArrowheadService getOffered() {
		return offered;
	}

	public void setOffered(ArrowheadService offered) {
		this.offered = offered;
	}
	
	public boolean isPayloadUsable(){
		if(provider == null || !provider.isValid() || provider.getAddress() == null || serviceURI == null)
			return false;
		return true;
	}
	
	
}
