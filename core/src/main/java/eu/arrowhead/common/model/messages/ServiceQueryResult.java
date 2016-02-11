package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServiceQueryResult {
	
	private List<ProvidedService> serviceQueryData = new ArrayList<ProvidedService>();

	public List<ProvidedService> getServiceQueryData() {
		return serviceQueryData;
	}

	public void setServiceQueryData(List<ProvidedService> serviceQueryData) {
		this.serviceQueryData = serviceQueryData;
	}

}
