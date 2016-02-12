package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;

@XmlRootElement
public class QoSVerify {

	private ArrowheadSystem consumer;
	private ArrowheadService requestedService;
	private List<ArrowheadSystem> provider = new ArrayList<ArrowheadSystem>();
	private String requestedQoS;

	public QoSVerify() {
		super();
	}

	public QoSVerify(ArrowheadSystem consumer, ArrowheadService requestedService, List<ArrowheadSystem> provider,
			String requestedQoS) {
		super();
		this.consumer = consumer;
		this.requestedService = requestedService;
		this.provider = provider;
		this.requestedQoS = requestedQoS;
	}

	public ArrowheadSystem getConsumer() {
		return consumer;
	}

	public void setConsumer(ArrowheadSystem consumer) {
		this.consumer = consumer;
	}

	public ArrowheadService getRequestedService() {
		return requestedService;
	}

	public void setRequestedService(ArrowheadService requestedService) {
		this.requestedService = requestedService;
	}

	public List<ArrowheadSystem> getProvider() {
		return provider;
	}

	public void setProvider(List<ArrowheadSystem> provider) {
		this.provider = provider;
	}

	public String getRequestedQoS() {
		return requestedQoS;
	}

	public void setRequestedQoS(String requestedQoS) {
		this.requestedQoS = requestedQoS;
	}

}
