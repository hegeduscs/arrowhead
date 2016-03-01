package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServiceQueryForm {

	private String serviceMetaData;
	private List<String> serviceInterfaces = new ArrayList<String>();
	private boolean pingProviders;
	private boolean metadataSearch;
	private String tsig_key;

	public ServiceQueryForm() {
		super();
	}

	public ServiceQueryForm(String serviceMetaData, List<String> serviceInterfaces, boolean pingProviders,
			String tSIG_key) {
		super();
		this.serviceMetaData = serviceMetaData;
		this.serviceInterfaces = serviceInterfaces;
		this.pingProviders = pingProviders;
		this.tsig_key = tSIG_key;
	}
	
	public ServiceQueryForm(ServiceRequestForm srf){
		this.serviceMetaData = srf.getRequestedService().getMetaData();
		this.serviceInterfaces = srf.getRequestedService().getInterfaces();
		this.pingProviders = srf.getOrchestrationFlags().get("PingProvider");
		this.tsig_key ="DUMMY"; // FROM CONFIGURATION
	}

	public String getServiceMetaData() {
		return serviceMetaData;
	}

	public void setServiceMetaData(String serviceMetaData) {
		this.serviceMetaData = serviceMetaData;
	}

	public boolean isPingProviders() {
		return pingProviders;
	}

	public void setPingProviders(boolean pingProviders) {
		this.pingProviders = pingProviders;
	}

	public String getTSIG_key() {
		return tsig_key;
	}

	public void setTSIG_key(String tsig_key) {
		this.tsig_key = tsig_key;
	}

	public List<String> getServiceInterfaces() {
		return serviceInterfaces;
	}

	public void setServiceInterfaces(List<String> serviceInterfaces) {
		this.serviceInterfaces = serviceInterfaces;
	}

	public boolean isMetadataSearch() {
		return metadataSearch;
	}

	public void setMetadataSearch(boolean metadataSearch) {
		this.metadataSearch = metadataSearch;
	}

}
