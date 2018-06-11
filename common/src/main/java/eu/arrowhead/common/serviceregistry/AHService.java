package eu.arrowhead.common.serviceregistry;

import eu.arrowhead.common.serviceregistry.ServiceInformation;

public class AHService {
	private ServiceInformation info;
	
	public AHService(ServiceInformation info) {
		this.info = info;
	}
	
	public ServiceInformation getInfo() {
		return this.info;
	}
	
	public void setInfo(ServiceInformation info) {
		this.info = info;
	}
	
	public String toString() {
		return info.toString();
	}
}