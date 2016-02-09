package eu.arrowhead.common.model.messages;

import java.util.ArrayList;
import java.util.List;

import eu.arrowhead.common.model.ArrowheadService;

public class GSDResult {

	List<ArrowheadService> Response = new ArrayList<ArrowheadService>();
	int OfferValidity;

	public GSDResult() {

	}

	public GSDResult(List<ArrowheadService> response, int offerValidity) {
		Response = response;
		OfferValidity = offerValidity;
	}

	public List<ArrowheadService> getResponse() {
		return Response;
	}

	public void setResponse(List<ArrowheadService> response) {
		Response = response;
	}

	public int getOfferValidity() {
		return OfferValidity;
	}

	public void setOfferValidity(int offerValidity) {
		OfferValidity = offerValidity;
	}

}
