package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServiceQueryResult {
	
	private List<ProvidedService> result = new ArrayList<ProvidedService>();

	public ServiceQueryResult() {
		super();
	}

	public ServiceQueryResult(List<ProvidedService> result) {
		super();
		this.result = result;
	}

	public List<ProvidedService> getServiceQueryData() {
		return result;
	}

	public void setServiceQueryData(List<ProvidedService> result) {
		this.result = result;
	}

}
