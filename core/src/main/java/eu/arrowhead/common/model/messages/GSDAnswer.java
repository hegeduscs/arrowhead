package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import eu.arrowhead.common.model.ArrowheadCloud;

public class GSDAnswer {

	private List<ProvidedService> answer = new ArrayList<ProvidedService>();
	private ArrowheadCloud providerCloud;
	
	public GSDAnswer() {
		super();
	}
	
	public GSDAnswer(List<ProvidedService> answer, ArrowheadCloud providerCloud) {
		super();
		this.answer = answer;
		this.providerCloud = providerCloud;
	}

	public List<ProvidedService> getAnswer() {
		return answer;
	}

	public void setAnswer(List<ProvidedService> answer) {
		this.answer = answer;
	}

	public ArrowheadCloud getProviderCloud() {
		return providerCloud;
	}

	public void setProviderCloud(ArrowheadCloud providerCloud) {
		this.providerCloud = providerCloud;
	}
	
}
