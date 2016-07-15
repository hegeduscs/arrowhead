package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;

@XmlRootElement
public class GSDRequestForm {

	private ArrowheadService requestedService;
	private List<ArrowheadCloud> searchPerimeter = new ArrayList<ArrowheadCloud>();

	public GSDRequestForm() {
	}

	public GSDRequestForm(ArrowheadService requestedService, List<ArrowheadCloud> searchPerimeter) {
		this.requestedService = requestedService;
		this.searchPerimeter = searchPerimeter;
	}

	public ArrowheadService getRequestedService() {
		return requestedService;
	}

	public void setRequestedService(ArrowheadService requestedService) {
		this.requestedService = requestedService;
	}

	public List<ArrowheadCloud> getSearchPerimeter() {
		return searchPerimeter;
	}

	public void setSearchPerimeter(List<ArrowheadCloud> searchPerimeter) {
		this.searchPerimeter = searchPerimeter;
	}

	public boolean isPayloadUsable(){
		if(requestedService == null)
			return false;
		if(!requestedService.isValid())
			return false;
		if(requestedService.getInterfaces() == null || requestedService.getInterfaces().isEmpty())
			return false;
		return true;
	}
	
	
}
