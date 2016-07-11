package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GSDResult {

	private List<GSDAnswer> response = new ArrayList<GSDAnswer>();

	public GSDResult() {
		super();
	}

	public GSDResult(List<GSDAnswer> response) {
		super();
		this.response = response;
	}

	public List<GSDAnswer> getResponse() {
		return response;
	}

	public void setResponse(List<GSDAnswer> response) {
		this.response = response;
	}

}
