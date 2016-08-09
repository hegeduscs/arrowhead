package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;

/**
 * @author umlaufz
 */
@XmlRootElement
public class InterCloudAuthEntry {
	
	private ArrowheadCloud cloud;
    private Collection<ArrowheadService> serviceList = new ArrayList<ArrowheadService>();
	
    public InterCloudAuthEntry(){
    }
    
	public InterCloudAuthEntry(ArrowheadCloud cloud, Collection<ArrowheadService> serviceList) {
		this.cloud = cloud;
		this.serviceList = serviceList;
	}

	public ArrowheadCloud getCloud() {
		return cloud;
	}

	public void setCloud(ArrowheadCloud cloud) {
		this.cloud = cloud;
	}

	public Collection<ArrowheadService> getServiceList() {
		return serviceList;
	}

	public void setServiceList(Collection<ArrowheadService> serviceList) {
		this.serviceList = serviceList;
	}

	public boolean isPayloadUsable(){
		if(cloud == null || serviceList.isEmpty() || !cloud.isValidForDatabase())
			return false;
		for(ArrowheadService service : serviceList)
			if(!service.isValidSoft())
				return false;
		return true;
	}
	
	
}
