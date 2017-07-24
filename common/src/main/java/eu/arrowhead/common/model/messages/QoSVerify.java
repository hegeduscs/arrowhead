package eu.arrowhead.common.model.messages;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class QoSVerify {

	private ArrowheadSystem consumer;
	private ArrowheadService requestedService;
	private List<ArrowheadSystem> provider = new ArrayList<ArrowheadSystem>();
	private Map<String, String> requestedQoS;
	private Map<String, String> commands;

	public QoSVerify() {
		super();
	}

	public QoSVerify(ArrowheadSystem consumer, ArrowheadService requestedService,
					 List<ArrowheadSystem> provider,
					 Map<String, String> specifications,
					 Map<String, String> commands) {
		super();
		this.consumer = consumer;
		this.requestedService = requestedService;
		this.provider = provider;
		this.requestedQoS = specifications;
		this.commands = commands;
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

	public Map<String, String> getRequestedQoS() {
		return requestedQoS;
	}

	public void setRequestedQoS(Map<String, String> requestedQoS) {
		this.requestedQoS = requestedQoS;
	}

	public Map<String, String> getCommands() {
		return commands;
	}

	public void setCommands(Map<String, String> commands) {
		this.commands = commands;
	}

}
