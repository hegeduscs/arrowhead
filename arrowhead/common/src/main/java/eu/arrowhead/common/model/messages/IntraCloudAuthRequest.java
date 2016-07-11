package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class IntraCloudAuthRequest {
	
	private ArrowheadSystem consumer;
	private Collection<ArrowheadSystem> providers = new ArrayList<ArrowheadSystem>();
	private ArrowheadService service;
	private boolean generateToken;	
	
	public IntraCloudAuthRequest() {
	}

	public IntraCloudAuthRequest(ArrowheadSystem consumer, Collection<ArrowheadSystem> providers,
			ArrowheadService service, boolean generateToken) {
		this.consumer = consumer;
		this.providers = providers;
		this.service = service;
		this.generateToken = generateToken;
	}
	
	public ArrowheadSystem getConsumer() {
		return consumer;
	}

	public void setConsumer(ArrowheadSystem consumer) {
		this.consumer = consumer;
	}

	public Collection<ArrowheadSystem> getProviders() {
		return providers;
	}

	public void setProviders(Collection<ArrowheadSystem> providers) {
		this.providers = providers;
	}

	public ArrowheadService getService() {
		return service;
	}

	public void setService(ArrowheadService service) {
		this.service = service;
	}

	public boolean isGenerateToken() {
		return generateToken;
	}

	public void setGenerateToken(boolean generateToken) {
		this.generateToken = generateToken;
	}

	public boolean isPayloadUsable(){
		if(consumer == null|| service == null || providers.isEmpty() || 
				!consumer.isValid() || !service.isValid())
			return false;
		for(ArrowheadSystem provider : providers)
			if(!provider.isValid())
				return false;
		return true;
	}
	
	
}
