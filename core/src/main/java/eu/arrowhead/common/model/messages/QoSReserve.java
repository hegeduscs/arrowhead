package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class QoSReserve {

	private ArrowheadSystem provider;
	private ArrowheadSystem consumer;
	private ArrowheadService service;
	private String requestedQoS;

	public QoSReserve() {
		super();
	}

	public QoSReserve(ArrowheadSystem provider, ArrowheadSystem consumer, ArrowheadService service, String requestedQoS) {
		super();
		this.provider = provider;
		this.consumer = consumer;
		this.service = service;
		this.requestedQoS = requestedQoS;
	}

	public String getRequestedQoS() {
		return requestedQoS;
	}

	public void setRequestedQoS(String requestedQoS) {
		this.requestedQoS = requestedQoS;
	}

	public ArrowheadSystem getProvider() {
		return provider;
	}

	public void setProvider(ArrowheadSystem provider) {
		this.provider = provider;
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

}
