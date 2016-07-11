package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

//TODO értelmesebb nevet találni neki
public class StorePayload {
	
	private ArrowheadSystem consumer;
	private ArrowheadService service;
	
	public StorePayload() {
	}
	
	public StorePayload(ArrowheadSystem consumer, ArrowheadService service) {
		this.consumer = consumer;
		this.service = service;
	}

	public ArrowheadSystem getConsumer() {
		return consumer;
	}

	public void setConsumer(ArrowheadSystem consumer) {
		this.consumer = consumer;
	}

	public ArrowheadService getService() {
		return service;
	}

	public void setService(ArrowheadService service) {
		this.service = service;
	}
	
	public boolean isPayloadUsable(){
		if(consumer == null || service == null || !consumer.isValid() || !service.isValid())
			return false;
		return true;
	}


}
