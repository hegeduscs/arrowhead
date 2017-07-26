package eu.arrowhead.common.model.messages;

import java.util.List;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

public class TokenGenerationRequest {
	
	private ArrowheadSystem consumer;
	private ArrowheadCloud consumerCloud;
	private List<ArrowheadSystem> providers;
	private ArrowheadService service;
	private int duration;
	
	public TokenGenerationRequest() {
	}

	public TokenGenerationRequest(ArrowheadSystem consumer, ArrowheadCloud consumerCloud,
			List<ArrowheadSystem> providers, ArrowheadService service, int duration) {
		this.consumer = consumer;
		this.consumerCloud = consumerCloud;
		this.providers = providers;
		this.service = service;
		this.duration = duration;
	}

	public ArrowheadSystem getConsumer() {
		return consumer;
	}

	public ArrowheadCloud getConsumerCloud() {
		return consumerCloud;
	}

	public List<ArrowheadSystem> getProviders() {
		return providers;
	}

	public ArrowheadService getService() {
		return service;
	}

	public int getDuration() {
		return duration;
	}

	public void setConsumer(ArrowheadSystem consumer) {
		this.consumer = consumer;
	}

	public void setConsumerCloud(ArrowheadCloud consumerCloud) {
		this.consumerCloud = consumerCloud;
	}

	public void setProviders(List<ArrowheadSystem> providers) {
		this.providers = providers;
	}

	public void setService(ArrowheadService service) {
		this.service = service;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}
}