package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadService;

@XmlRootElement
public class GSDResult {

	private List<GSDEntry> response = new ArrayList<GSDEntry>();

	public GSDResult() {
		super();
	}

	public GSDResult(List<GSDEntry> response) {
		super();
		this.response = response;
	}

	public List<GSDEntry> getResponse() {
		return response;
	}

	public void setResponse(List<GSDEntry> response) {
		this.response = response;
	}

}
