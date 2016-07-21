package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServiceQueryResult {
	
	private List<ProvidedService> serviceQueryData = new ArrayList<ProvidedService>();

	public ServiceQueryResult() {
		super();
	}

	public ServiceQueryResult(List<ProvidedService> serviceQueryData) {
		super();
		this.serviceQueryData = serviceQueryData;
	}

	public List<ProvidedService> getServiceQueryData() {
		return serviceQueryData;
	}

	public void setServiceQueryData(List<ProvidedService> serviceQueryData) {
		this.serviceQueryData = serviceQueryData;
	}
	
	public boolean isPayloadEmpty(){
		if(serviceQueryData == null || serviceQueryData.isEmpty())
			return true;
		return false;
	}
	

}
