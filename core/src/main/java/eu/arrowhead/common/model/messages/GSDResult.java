package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import eu.arrowhead.common.model.ArrowheadService;

public class GSDResult {

	private List<ArrowheadService> response = new ArrayList<ArrowheadService>();

	public GSDResult() {
		super();
	}

	public GSDResult(List<ArrowheadService> response) {
		super();
		this.response = response;
	}

	public List<ArrowheadService> getResponse() {
		return response;
	}

	public void setResponse(List<ArrowheadService> response) {
		this.response = response;
	}

}
