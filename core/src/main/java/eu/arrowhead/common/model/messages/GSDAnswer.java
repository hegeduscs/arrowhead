package eu.arrowhead.common.model.messages;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;

@XmlRootElement
public class GSDAnswer {

	
	private ArrowheadService requestedService;
	private ArrowheadCloud providerCloud;
	
	public GSDAnswer() {
		super();
	}

	public GSDAnswer(ArrowheadService requestedService,
			ArrowheadCloud providerCloud) {
		super();
		this.requestedService = requestedService;
		this.providerCloud = providerCloud;
	}

	public ArrowheadService getRequestedService() {
		return requestedService;
	}

	public void setRequestedService(ArrowheadService requestedService) {
		this.requestedService = requestedService;
	}

	public ArrowheadCloud getProviderCloud() {
		return providerCloud;
	}

	public void setProviderCloud(ArrowheadCloud providerCloud) {
		this.providerCloud = providerCloud;
	}

	
	
}
