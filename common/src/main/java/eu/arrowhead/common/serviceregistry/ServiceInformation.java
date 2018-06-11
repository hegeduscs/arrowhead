package eu.arrowhead.common.serviceregistry;

import eu.arrowhead.common.model.Information;
import eu.arrowhead.common.model.Endpoint;
import eu.arrowhead.common.serviceregistry.ServiceIdentity;
import eu.arrowhead.common.serviceregistry.ServiceMetadata;

public class ServiceInformation extends Information{
	private ServiceIdentity identity;
	private ServiceMetadata metadata;
	
	public ServiceInformation(ServiceIdentity identity, Endpoint endpoint, ServiceMetadata metadata) {
		super(identity, endpoint, metadata);
		this.identity = identity;
		this.metadata = metadata;
	}
	
	public ServiceIdentity getIdentity() {
		return this.identity;
	}
	
	public void setIdentity(ServiceIdentity identity) {
		this.identity = identity;
	}
	
	public ServiceMetadata getMetadata() {
		return this.metadata;
	}
	
	public void setMetadata(ServiceMetadata metadata) {
		this.metadata = metadata;
	}
	
	@Override
    public String toString() {
		return "Service: \n\t" + identity + "\n\t" + endpoint + "\n\t" + "Metadata [data=" + metadata + "]";
    }
}