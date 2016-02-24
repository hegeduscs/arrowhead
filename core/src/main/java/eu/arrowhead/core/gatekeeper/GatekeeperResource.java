package eu.arrowhead.core.gatekeeper;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.arrowhead.common.configuration.SysConfig;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.GSDAnswer;
import eu.arrowhead.common.model.messages.GSDPoll;
import eu.arrowhead.common.model.messages.GSDRequestForm;
import eu.arrowhead.common.model.messages.GSDResult;
import eu.arrowhead.common.model.messages.ICNProposal;
import eu.arrowhead.common.model.messages.InterCloudAuthRequest;
import eu.arrowhead.common.model.messages.OrchestrationForm;
import eu.arrowhead.common.model.messages.OrchestrationResponse;
import eu.arrowhead.common.model.messages.ProvidedService;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;
import eu.arrowhead.common.model.messages.ServiceRequestForm;

/**
 * 
 * @author blevente92
 *
 */
@Path("gatekeeper")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GatekeeperResource {
	public ArrowheadService requestedService;
	public ArrowheadCloud requesterCloud;
	List<ProvidedService> testservices = new ArrayList<ProvidedService>();
	ProvidedService providedService;
	private ServiceRequestForm serviceRequestForm;
//	private SysConfig sysConfig = SysConfig.getInstance();



	
	@GET
    public String getIt() {
        return "This is the Gatekeeper Resource stub.";
    }
    
	/**
	 * This function represents the Consumer-side GSD Poll, where the GateKeeper of the consumer service try to find 
	 * a provider service from another Cloud
	 * 
	 * @param gsdPollRequest
	 * @return GSDAnswer
	 */
    @PUT
    @Path("/init_gsd/")
    public GSDAnswer sendRequest(GSDPoll gsdPollRequest){
//    	GSDPoll gsdPoll = gsdPollRequest;
    	
    	//Test GSDPoll
    	GSDPoll gsdPoll = testGSDPoll();    	
    	
    	// HTTP PUT to the provider GateKeeper
    	Client client = ClientBuilder.newClient();
    	String uri = "http://localhost:8080/core/gatekeeper/gsd_poll/";
		WebTarget target = client.target(uri);
	    Response response = target
	    		.request()
	    		.header("Content-type", "application/json")
				.put(Entity.json(gsdPoll));
	    return response.readEntity(GSDAnswer.class);
	    
    }
    
    /**
     * This function represents the Provider-side GSDPoll, where the GateKeeper send back 
     * a list of provider services, which match the demands of the consumer service     * 
     * 
     * @param gsdPollRequest
     * @return GSDAnswer
     */
    @PUT
    @Path("/gsd_poll/")
    public GSDAnswer getRequest(GSDPoll gsdPollRequest){

    	GSDPoll gsdPoll = gsdPollRequest;    	
    	ArrowheadCloud requesterCloud = gsdPoll.getRequesterCloud();
    	ArrowheadService requestedService = gsdPoll.getRequestedService();
    	ArrowheadSystem requesterSystem = new ArrowheadSystem("systemGroup", "systemName", "iPAddress", "port", "authenticationInfo");
    	
    	
    	String cloudOperator = gsdPoll.getRequesterCloud().getOperator();
    	String cloudName = gsdPoll.getRequesterCloud().getName();
//    	String testUri = sysConfig.getAuthorizationURI();
//    	System.out.println(testUri);
    	String uri = "http://localhost:8080/core/authorization/operator/"+cloudOperator+"/cloud/"+cloudName; //TODO: SysConfig URI
    	
    	// Sending an InterCloudAuthRequest to the Authorization System (generateToken=false)
    	InterCloudAuthRequest interAuthRequest = new InterCloudAuthRequest (requesterCloud.getAuthenticationInfo(), requestedService, false);
    	Client client = ClientBuilder.newClient();
		WebTarget target = client.target(uri); 
	    Response response = target
	    		.request()
	    		.header("Content-type", "application/json")
	    		.put(Entity.json(interAuthRequest)); 
//	    if(response.readEntity(Boolean.class)){
    	
	    	// Generate a ServiceQueryForm from GSDPoll to send it to the Service Registry
	    	ServiceRequestForm srf = new ServiceRequestForm(requestedService, "requestedQoS", requesterSystem, 1000);
//	    	ServiceQueryForm sqf = new ServiceQueryForm(srf);
	    	ServiceQueryForm sqf = new ServiceQueryForm(requestedService.getMetaData(), requestedService.getInterfaces(), false, "tSIG_key");
	    	ServiceQueryResult sqr = getServiceQueryResultGateKeeper(sqf);
	    	GSDAnswer answer = new GSDAnswer(sqr.getServiceQueryData(), requesterCloud);
	    	return answer;
	    	
	    	
	    	
//	    }else return null; //TODO: Error handling
    }

    /**
     * This function represents the consumer-side ICN Proposal, where the consumer service matches
     * the provide service.
     * 
     * @param icnProposal
     * @return OrchestrationForm
     */
    @PUT
    @Path("/init_icn/")
    public OrchestrationResponse sendProposal(ICNProposal icnProposal){
//    	ICNProposal proposal = icnProposal;
//    	ArrowheadService requestedService = icnProposal.getRequestedService();
    	
    	//Test ICNProposal
    	ICNProposal proposal = testProposal();
    	
    	// HTTP PUT to the provider GateKeeper
    	Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:8080/core/gatekeeper/icn_proposal/");
	    Response response = target
	    		.request()
	    		.accept(MediaType.APPLICATION_JSON)
	    		.header("Content-type", "application/json")
	    		.put(Entity.json(proposal)); 
//	    System.out.println("Megkapva");
	    
	    return response.readEntity(OrchestrationResponse.class);
    }
    
	/**
     * This function represents the provider-side ICN Proposal, where the consumer service matches
     * the provide service.
     * 
     * @param icnProposal
     * @return OrchestrationForm
     */
    @PUT
    @Path("/icn_proposal/")
    public OrchestrationResponse ICNEnd (ICNProposal icnProposalRequest){
    	
    	ICNProposal icnProposal = icnProposalRequest;
    	ArrowheadCloud consumerCloud = new ArrowheadCloud(
    			"operator2", "cloudName2", "gatekeeperIP2", "gatekeeperPort2",
    			"gatekeeperURI", "authenticationInfo");
    	ArrowheadService requestedService = icnProposal.getRequestedService();
    	ArrowheadSystem requesterSystem = new ArrowheadSystem(
    			"ReqSystemGroup", "ReqSystemName", "ReqIPAddress", "ReqPort", "ReqAuthenticationInfo");
    	
    	String cloudOperator = "operator";
    	String cloudName = "cloudName";
    	String uri = "http://localhost:8080/core/authorization/operator/"+cloudOperator+"/cloud/"+cloudName;
    	
    	// Sending an InterCloudAuthRequest to the Authorization System (generateToken=true)
    	InterCloudAuthRequest interAuthRequest = new InterCloudAuthRequest(consumerCloud.getAuthenticationInfo(), requestedService, true);
    	Client client = ClientBuilder.newClient();
		WebTarget target = client.target(uri); 
	    Response response = target
	    		.request()
	    		.header("Content-type", "application/json")
	    		.put(Entity.json(interAuthRequest)); 
    	
//	    if(response.readEntity(Boolean.class)){
	    	
	    	// Send a HTTP POST to Orchestrator
    		serviceRequestForm = new ServiceRequestForm(requestedService, "requestedQoS", requesterSystem, 5);
//	    	Map<String, Boolean> orchestrationFlags;
//			serviceRequestForm = new ServiceRequestForm(requestedService, "requestedQoS", requesterSystem, 5, orchestrationFlags);
			
			Client client2 = ClientBuilder.newClient();
	    	String uri2 = "http://localhost:8080/core/orchestrator/orchestration";
			WebTarget target2 = client2.target(uri2); 
		    Response response2 = target2
		    		.request()
		    		.header("Content-type", "application/json")
		    		.post(Entity.json(serviceRequestForm));
//		    System.out.println("Elküldve");
//		    System.out.println(response2.getStatus());
		    return response2.readEntity(OrchestrationResponse.class);

		    
//		    //Test OrchestrationForm
//		    OrchestrationForm orchForm = testOrchestrationForm();
//		    return orchForm;
    }
    


	/**
	 * Sends the Service Query Form to the Service Registry and asks for the
	 * Service Query Result.
	 * 
	 * @param sqf
	 * @return ServiceQueryResult
	 */
  //Copy from eu.arrowhead.core.orchestrator.OrchestrationService
  	private ServiceQueryResult getServiceQueryResultGateKeeper(ServiceQueryForm sqfARG/*, URI uri*/) {
  		ServiceQueryForm sqf = sqfARG;
  		System.out.println("GK: inside the getServiceQueryResult function");

  		// Send a HTTP PUT to ServiceRegistry for the provider service(s)
  		Client client = ClientBuilder.newClient();
  		String uri = "http://localhost:8080/core/serviceregistry/serviceGroup/service";

  		WebTarget target = client.target(uri);
  		Response response = target.request().header("Content-type", "application/json").put(Entity.json(sqf));
  		System.out.println("GK: gSQR received the response");
  		
//  		ServiceQueryResult sqr = new ServiceQueryResult(response.readEntity(ServiceQueryResult.class).getServiceQueryData());
		
  		//Test ServiceQueryResult:
  		ServiceQueryResult sqr = testServiceQueryResult();
  		
  		System.out.println("GK received the following serviceURIs:");
  		for (ProvidedService providedService : sqr.getServiceQueryData()) {
  			System.out.println(providedService.getServiceURI() + providedService.getProvider().getIPAddress());
  		}
  		return sqr;
  	}

  	
  	/**
  	 * Create a GSDPoll for testing
  	 * 
  	 * @return GSDPoll
  	 */
    private GSDPoll testGSDPoll(){
    	List<String> interfaces = new ArrayList<String>();
		interfaces.add("test1");
		interfaces.add("test2");
    	requestedService = new ArrowheadService("serviceGroup", "serviceDefinition", interfaces, "metaData");
    	requesterCloud = new ArrowheadCloud("operator", "cloudName", "gatekeeperIP", "gatekeeperPort", "gatekeeperURI", "authenticationInfo");
    	GSDPoll gsdPoll = new GSDPoll(requestedService, requesterCloud);
    	return gsdPoll;    	
    }
    
    /**
     * Create an ICNProposal for testing
     * @return ICNProposal
     */
    private ICNProposal testProposal() {
    	List<String> interfaces = new ArrayList<String>();
		interfaces.add("test1");
		interfaces.add("test2");
    	ArrowheadService requestedService = new ArrowheadService("serviceGroup", "serviceDefinition", interfaces, "metaData");
    	ICNProposal proposal = new ICNProposal(requestedService, "authInfo");
    	return proposal;
	}
    
    /**
     * Create an OrchestrationForm for testing
     * @return OrchestrationForm
     */
	private OrchestrationForm testOrchestrationForm() {
		List<String> interfaces = new ArrayList<String>();
    	interfaces.add("test111");
    	interfaces.add("test222");
	    ArrowheadService providerService = new ArrowheadService("serviceGroup", "serviceDefinition",
	    		interfaces, "metaData");
	    ArrowheadSystem providerSystem = new ArrowheadSystem("systemGroup", "systemName", 
	    		"iPAddress", "port", "authenticationInfo");
	    OrchestrationForm orchForm = new OrchestrationForm(
	    		providerService, providerSystem, "serviceURI", "authorizationInfo");
		return orchForm;
	}
	
	/**
	 * Create a ServiceQueryResult for testing
	 * @return ServiceQueryResult
	 */
	private ServiceQueryResult testServiceQueryResult(){
		ArrowheadSystem provider = new ArrowheadSystem("a", "g", "f", "fd", "dd");
	providedService = new ProvidedService(provider , "serviceURI", "serviceInterface");
		testservices.add(providedService);
		ServiceQueryResult sqr = new ServiceQueryResult(testservices);
		return sqr;
	}
    
}