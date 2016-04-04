package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ServiceQueryForm {

	private List<ServiceMetadata> serviceMetadata;
	private List<String> serviceInterfaces = new ArrayList<String>();
	private boolean pingProviders;
	private boolean metadataSearch;
	private String tsig_key;

	public ServiceQueryForm() {
		super();
	}

	public ServiceQueryForm(List<ServiceMetadata> serviceMetadata, List<String> serviceInterfaces, boolean pingProviders,
			boolean metadataSearch, String tsig_key) {
		this.serviceMetadata = serviceMetadata;
		this.serviceInterfaces = serviceInterfaces;
		this.pingProviders = pingProviders;
		this.metadataSearch = metadataSearch;
		this.tsig_key = tsig_key;
	}

	public ServiceQueryForm(ServiceRequestForm srf) {
		this.serviceMetadata = srf.getRequestedService().getMetaData();
		this.serviceInterfaces = srf.getRequestedService().getInterfaces();
		this.pingProviders = srf.getOrchestrationFlags().get("pingProvider");
		this.metadataSearch = srf.getOrchestrationFlags().get("metadataSearch");
		this.tsig_key = "DUMMY"; // FROM CONFIGURATION
	}

	public List<ServiceMetadata> getServiceMetadata() {
		return serviceMetadata;
	}

	public void setServiceMetadata(List<ServiceMetadata> serviceMetadata) {
		this.serviceMetadata = serviceMetadata;
	}

	public boolean isPingProviders() {
		return pingProviders;
	}

	public void setPingProviders(boolean pingProviders) {
		this.pingProviders = pingProviders;
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

	public String getTsig_key() {
		return tsig_key;
	}

	public void setTsig_key(String tsig_key) {
		this.tsig_key = tsig_key;
	}

}
