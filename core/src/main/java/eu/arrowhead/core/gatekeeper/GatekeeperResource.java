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
import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;

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
import eu.arrowhead.core.orchestrator.OrchestratorService;

/**
 * 
 * @author blevente92
 *
 */
@Path("gatekeeper")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GatekeeperResource {
	private SysConfig sysConfig = SysConfig.getInstance();
	private static Logger log = Logger.getLogger(GatekeeperResource.class.getName());

	
	@GET
    public String getIt() {
//		List<String> test = sysConfig.getCloudURIs();
//		String testURI = test.get(0);
//		String conc = testURI.substring(0, 25) + "gatekeeper";
//		System.out.println(testURI);
//		System.out.println(conc);
//    	String uri = sysConfig.getCloudURIs().get(0).substring(0, 25)+
//    			"gatekeeper/init_gsd/";
//		System.out.println(uri);

	    return "This is the Gatekeeper Resource stub.";
    }
	
	
	@GET
    @Path("/test_gsd/")
    public GSDAnswer testGSD() {
		GSDPoll gsdPoll = new GSDPoll();
		gsdPoll = testGSDPoll();
		
    	Client client = ClientBuilder.newClient();
    	String uri = "http://localhost:8080/core/gatekeeper/init_gsd/";
		WebTarget target = client.target(uri);
	    Response response = target
	    		.request()
	    		.header("Content-type", "application/json")
				.put(Entity.json(gsdPoll));
	    return response.readEntity(GSDAnswer.class);
	}
	
	
	@GET
    @Path("/test_icn/")
    public OrchestrationResponse testICN() {
    	ICNProposal proposal = new ICNProposal();
    	proposal = testProposal();
    	
    	Client client = ClientBuilder.newClient();
    	String uri = "http://localhost:8080/core/gatekeeper/init_icn/";
		WebTarget target = client.target(uri);
	    Response response = target
	    		.request()
	    		.header("Content-type", "application/json")
				.put(Entity.json(proposal));
	    return response.readEntity(OrchestrationResponse.class);

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
    	GSDPoll gsdPoll = new GSDPoll();
    	
    	//Test GSDPoll
    	if(gsdPollRequest==null) {
    		log.info("No GSDPoll, creating a test request.");
    		System.out.println("No GSDPoll, creating a test request.");
        	gsdPoll = testGSDPoll();    	
    	}
    	
    	else {
    		System.out.println("Existing GSDPoll, starting to find provider service");
    		gsdPoll = gsdPollRequest;}
    	
    	
    	// HTTP PUT to the provider GateKeeper
//   		log.info("Starting to find provider service for: "+ gsdPoll.getRequestedService().getServiceDefinition());
   		System.out.println("Starting to find provider service for: "+ gsdPoll.getRequestedService().getServiceDefinition());
   		Client client = ClientBuilder.newClient();
    	//TODO: URI to neighbour cloud (GSD)
   		
   		//Majd jó lesz:
//    	String uri = "http://"+sysConfig.getCloudURIs().get(0).substring(0, 25)+
//    			"gatekeeper/gsd_poll/";
    	
    	String uri = "http://"+sysConfig.getGatekeeperURI()+"/gsd_poll/";
    	   		
//   		String uri = "http://localhost:8080/core/gatekeeper/gsd_poll/";
    	System.out.println(uri);
    	
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

    	GSDPoll gsdPoll = new GSDPoll();    
    	
    	gsdPoll = gsdPollRequest;
    	
    	ArrowheadCloud requesterCloud = gsdPoll.getRequesterCloud();
    	ArrowheadService requestedService = gsdPoll.getRequestedService();
    	ArrowheadSystem requesterSystem = new ArrowheadSystem("systemGroup", "systemName", "iPAddress", "port", "authenticationInfo");

    	String cloudOperator = requesterCloud.getOperator();
    	String cloudName = requesterCloud.getName();

//    	String uri = "http://" + sysConfig.getAuthorizationURI() + "/operator/" + cloudOperator+"/cloud/"+cloudName;
    	String uri = "http://localhost:8080/core/authorization/operator/"+cloudOperator+"/cloud/"+cloudName;
    	System.out.println(uri);

    	// Sending an InterCloudAuthRequest to the Authorization System (generateToken=false)
    	InterCloudAuthRequest interAuthRequest = new InterCloudAuthRequest (requesterCloud.getAuthenticationInfo(), requestedService, false);
    	Client client = ClientBuilder.newClient();
		WebTarget target = client.target(uri); 
	    Response response = target
	    		.request()
	    		.header("Content-type", "application/json")
	    		.put(Entity.json(interAuthRequest)); 
	    
	    //FIXME: Comment for test
//	    if(response.readEntity(Boolean.class)){
    	
	    	// Generate a ServiceQueryForm from GSDPoll to send it to the Service Registry
	    
//		    ServiceRequestForm serviceRequestForm= new ServiceRequestForm(
//		    		requestedService, "requestedQoS", requesterSystem, 150);
//	    	ServiceQueryForm srvQueryForm = new ServiceQueryForm(serviceRequestForm);
//	    	ServiceQueryResult srvQueryResult = getServiceQueryResultGateKeeper(srvQueryForm, this.serviceRequestForm);

	    	//TEST ServiceRequest
	    	ServiceQueryResult srvQueryResult = testServiceQueryResult();

	    	GSDAnswer answer = new GSDAnswer(srvQueryResult.getServiceQueryData());
	    	return answer;

//	    }else return null; //TODO: Error handling: return Response.status(Status.UNAUTHORIZED).build();
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
    	
    	ICNProposal proposal = new ICNProposal();
    	
    	//Test ICNProposal
    	if (icnProposal == null){
    		log.info("No ICNProposal, creating a test request.");
    		proposal = testProposal();    	
    	}
    	else {
    		proposal = icnProposal;
    	}
    	
    	
    	// HTTP PUT to the provider GateKeeper
    	log.info("ICN Proposal for: " + proposal.getRequestedService().getServiceDefinition());
    	
   		//Majd jó lesz:
//    	String uri = "http://"+sysConfig.getCloudURIs().get(0).substring(0, 25)+
//    			"gatekeeper/icn_proposal/";
    	
    	String uri = "http://"+sysConfig.getGatekeeperURI()+"/icn_proposal/";

//    	String uri = "http://localhost:8080/core/gatekeeper/icn_proposal/";
    	
    	Client client = ClientBuilder.newClient();
		WebTarget target = client.target(uri);
	    Response response = target
	    		.request()
	    		.accept(MediaType.APPLICATION_JSON)
	    		.header("Content-type", "application/json")
	    		.put(Entity.json(proposal)); 
	    
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
    	String cloudOperator = "BME";
    	String cloudName = "B";
    	ArrowheadCloud consumerCloud = new ArrowheadCloud(
    			cloudOperator, cloudName, "gatekeeperIP2", "gatekeeperPort2",
    			"gatekeeperURI", "test");
    	ArrowheadService requestedService = icnProposal.getRequestedService();
    	ArrowheadSystem requesterSystem = new ArrowheadSystem(
    			"ReqSystemGroup", "ReqSystemName", "ReqIPAddress", "ReqPort", "ReqAuthenticationInfo");
    	
    	
//    	String uri = "http://" + sysConfig.getAuthorizationURI() + "/operator/" + cloudOperator+"/cloud/"+cloudName;
    	String uri = "http://localhost:8080/core/authorization/operator/"+cloudOperator+"/cloud/"+cloudName;
    	System.out.println(uri);

    	// Sending an InterCloudAuthRequest to the Authorization System (generateToken=true)
    	InterCloudAuthRequest interAuthRequest = new InterCloudAuthRequest(consumerCloud.getAuthenticationInfo(), requestedService, true);
    	Client client = ClientBuilder.newClient();
		WebTarget target = client.target(uri); 
	    Response response = target
	    		.request()
	    		.header("Content-type", "application/json")
	    		.put(Entity.json(interAuthRequest)); 
	    
    	//FIXME: most nincs author
//	    if(response.readEntity(Boolean.class)){
	    	
	    	// Send a HTTP POST to Orchestrator
    		ServiceRequestForm serviceRequestForm = new ServiceRequestForm(requestedService, "requestedQoS", requesterSystem, 5);
			
			Client client2 = ClientBuilder.newClient();

			//TODO: URI to Orchestrator (ICN)
	    	String uri2 = "http://localhost:8080/core/orchestrator/orchestration";
			WebTarget target2 = client2.target(uri2); 
		    Response response2 = target2
		    		.request()
		    		.header("Content-type", "application/json")
		    		.post(Entity.json(serviceRequestForm));
		    return response2.readEntity(OrchestrationResponse.class);
//    }
//	    else return null;    
	    
    }

    


	/**
	 * Sends the Service Query Form to the Service Registry and asks for the
	 * Service Query Result.
	 * 
	 * @param sqf
	 * @return ServiceQueryResult
	 */
  //Copy from eu.arrowhead.core.orchestrator.OrchestrationService
  	private ServiceQueryResult getServiceQueryResultGateKeeper(ServiceQueryForm sqf, ServiceRequestForm srf) {
  		System.out.println("orchestator: inside the getServiceQueryResult function");
		ArrowheadService as = srf.getRequestedService();
		String strtarget = "http://" + sysConfig.getServiceRegistryURI()+ "/" + as.getServiceGroup() + "/" + as.getServiceDefinition();
		System.out.println("orchestrator: sending the ServiceQueryForm to this address:" + strtarget);
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(strtarget);
		Response response = target.request().header("Content-type", "application/json").put(Entity.json(sqf));
		ServiceQueryResult sqr = response.readEntity(ServiceQueryResult.class);
		System.out.println("orchestrator received the following services from the SR:");
		for (ProvidedService providedService : sqr.getServiceQueryData()) {
			System.out.println(providedService.getProvider().getSystemGroup() + providedService.getProvider().getSystemName());
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
		interfaces.add("inf2");
		interfaces.add("inf4");
		ArrowheadService requestedService = new ArrowheadService("sg4", "sd4", interfaces, "md4");
		ArrowheadCloud requesterCloud = new ArrowheadCloud("BME", "B", "gatekeeperIP", "gatekeeperPort", "gatekeeperURI", "test");
    	GSDPoll gsdPoll = new GSDPoll(requestedService, requesterCloud);
    	return gsdPoll;    	
    }
    
    /**
     * Create an ICNProposal for testing
     * @return ICNProposal
     */
    private ICNProposal testProposal() {
    	List<String> interfaces = new ArrayList<String>();
		interfaces.add("inf2");
		interfaces.add("inf4");
		ArrowheadService requestedService = new ArrowheadService("sg4", "sd4", interfaces, "md4");
    	ICNProposal proposal = new ICNProposal(requestedService, "test");
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
		ProvidedService providedService = new ProvidedService(provider , "serviceURI", "serviceInterface");
		List<ProvidedService> testservices = new ArrayList<ProvidedService>();
		testservices.add(providedService);
		ServiceQueryResult sqr = new ServiceQueryResult(testservices);
		return sqr;
	}
    
}