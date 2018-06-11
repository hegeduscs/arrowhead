package eu.arrowhead.common.model;

import eu.arrowhead.common.model.Endpoint;
import eu.arrowhead.common.model.Identity;
import eu.arrowhead.common.model.Metadata;

public class Information {
	protected Endpoint endpoint;
	protected Metadata metadata;
	protected Identity identity;
	
	public Information(Identity identity, Endpoint endpoint, Metadata metadata) {
		this.identity = identity;
		this.endpoint = endpoint;
		this.metadata = metadata;
	}
	
	public Information() {
		
	}
	
	public Identity getIdentity() {
		return this.identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	public Endpoint getEndpoint() {
		return this.endpoint;
	}
	
	public void setEndpoint(Endpoint endpoint) {
		this.endpoint = endpoint;
	}
	
	public Metadata getMetadata() {
		return this.metadata;
	}
	
	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}
}