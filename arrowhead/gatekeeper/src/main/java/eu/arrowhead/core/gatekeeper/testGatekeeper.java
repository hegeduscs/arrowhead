package eu.arrowhead.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.GSDPoll;
import eu.arrowhead.common.model.messages.ICNProposal;
import eu.arrowhead.common.model.messages.OrchestrationForm;
import eu.arrowhead.common.model.messages.ProvidedService;
import eu.arrowhead.common.model.messages.ServiceMetadata;
import eu.arrowhead.common.model.messages.ServiceQueryResult;

/**
 * 
 *  Test class for test methods
 * @author blevente92
 *
 */
public class testGatekeeper {
	
	/**
	 * Create a GSDPoll for testing
	 * 
	 * @return GSDPoll
	 */
protected GSDPoll testGSDPoll(){
	List<String> interfaces = new ArrayList<String>();
	interfaces.add("inf2");
	interfaces.add("inf4");
	List<ServiceMetadata> data =  new ArrayList<ServiceMetadata>();
	data.add(new ServiceMetadata("md4", "md4"));
	ArrowheadService requestedService = new ArrowheadService("sg4", "sd4", interfaces, data);
	ArrowheadCloud requesterCloud = new ArrowheadCloud("BME", "B", "gatekeeperIP", "gatekeeperPort", "gatekeeperURI", "test");
	GSDPoll gsdPoll = new GSDPoll(requestedService, requesterCloud);
	return gsdPoll;    	
}

/**
* Create an ICNProposal for testing
* @return ICNProposal
*/
protected ICNProposal testProposal() {
	List<String> interfaces = new ArrayList<String>();
	interfaces.add("inf2");
	interfaces.add("inf4");
	List<ServiceMetadata> data =  new ArrayList<ServiceMetadata>();
	data.add(new ServiceMetadata("md4", "md4"));
	ArrowheadService requestedService = new ArrowheadService("sg4", "sd4", interfaces, data);
	ICNProposal proposal = new ICNProposal(requestedService, "test", null, null);
	return proposal;
}

/**
* Create an OrchestrationForm for testing
* @return OrchestrationForm
*/
protected OrchestrationForm testOrchestrationForm() {
	List<String> interfaces = new ArrayList<String>();
	interfaces.add("test111");
	interfaces.add("test222");
	List<ServiceMetadata> data =  new ArrayList<ServiceMetadata>();
	data.add(new ServiceMetadata("md4", "md4"));
    ArrowheadService providerService = new ArrowheadService("serviceGroup", "serviceDefinition", interfaces, data);
    ArrowheadSystem providerSystem = new ArrowheadSystem("systemGroup", "systemName", "iPAddress", "port", "authenticationInfo");
    OrchestrationForm orchForm = new OrchestrationForm(providerService, providerSystem, "serviceURI", "authorizationInfo");
	return orchForm;
}

/**
* Create a ServiceQueryResult for testing
* @return ServiceQueryResult
*/
protected ServiceQueryResult testServiceQueryResult(){
	ArrowheadSystem provider = new ArrowheadSystem("a", "g", "f", "fd", "dd");
	ProvidedService providedService = new ProvidedService(provider ,null, "serviceURI", "serviceInterface");
	List<ProvidedService> testservices = new ArrayList<ProvidedService>();
	testservices.add(providedService);
	ServiceQueryResult sqr = new ServiceQueryResult(testservices);
	return sqr;
}


}
