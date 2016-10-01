package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GSDResult {

	private List<GSDAnswer> response = new ArrayList<GSDAnswer>();

	public GSDResult() {
	}

	public GSDResult(List<GSDAnswer> response) {
		this.response = response;
	}

	public List<GSDAnswer> getResponse() {
		return response;
	}

	public void setResponse(List<GSDAnswer> response) {
		this.response = response;
	}
	
	public boolean isPayloadUsable(){
		if(response == null || response.isEmpty())
			return false;
		return true;
	}

}
