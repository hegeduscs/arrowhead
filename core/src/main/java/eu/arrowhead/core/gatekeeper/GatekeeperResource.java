package eu.arrowhead.core.gatekeeper;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import eu.arrowhead.common.model.messages.GSDEntry;
import eu.arrowhead.common.model.messages.GSDPoll;
import eu.arrowhead.common.model.messages.GSDRequestForm;
import eu.arrowhead.common.model.messages.GSDResult;
import eu.arrowhead.common.model.messages.ICNEnd;
import eu.arrowhead.common.model.messages.ICNProposal;
import eu.arrowhead.common.model.messages.ICNRequestForm;
import eu.arrowhead.common.model.messages.ICNResultForm;
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
//		ArrowheadCloud cloud = sysConfig.getInternalCloud();
//		System.out.println(cloud.getGatekeeperIP());
	    return "This is the Gatekeeper Resource stub.";
    }
	
    
	/**
	 * This function represents the Consumer-side GSD Poll, where the GateKeeper of the consumer service try to find 
	 * a provider service from another Cloud
	 * 
	 * @param GSDRequestForm
	 * @return GSDResult
	 */
    @PUT
    @Path("/init_gsd/")
    public GSDResult sendRequest(GSDRequestForm gsdRequest){
    	log.info("Inside the GateKeeper");
    	String random = sysConfig.getAuthorizationURI();
    	log.info("Sysconfig");
    	
    	ArrowheadCloud cloud = sysConfig.getOwnCloud();
    	
    	log.info("Got the cloud info");
    	GSDPoll gsdPoll = new GSDPoll();
    	log.info("Created gsdPoll");
    	
    	//Test GSDPoll
    	if(gsdRequest==null) {
    		log.info("No GSDRequest, creating a test request.");
    		System.out.println("No GSDPoll, creating a test request.");
        	gsdPoll = testGSDPoll();    	
    	}
    	
    	else {
    		log.info("Existing GSDPoll, starting to find provider service");
    		gsdPoll = new GSDPoll(gsdRequest.getRequestedService(),cloud);}
    	
    	
    	// HTTP PUT to the provider GateKeeper
   		log.info("Starting to find provider service for: "+ gsdPoll.getRequestedService().getServiceDefinition());
   		System.out.println("Starting to find provider service for: "+ gsdPoll.getRequestedService().getServiceDefinition());
   		Client client = ClientBuilder.newClient();
   		
    	String uri = "http://"+sysConfig.getCloudURIs().get(0).substring(0, 25)+
    			"gatekeeper/gsd_poll/";
    	
//    	String uri = "http://"+sysConfig.getGatekeeperURI()+"/gsd_poll/";
//   	String uri = "http://localhost:8080/core/gatekeeper/gsd_poll/";
    	   		
    	System.out.println(uri);
    	
		WebTarget target = client.target(uri);
	    Response response = target
	    		.request()
	    		.header("Content-type", "application/json")
				.put(Entity.json(gsdPoll));
	    GSDAnswer answer = response.readEntity(GSDAnswer.class);
	    
	    
	    if (answer == null) 
	    	{
	    	System.out.println("answer == null");
	    	GSDResult result = new GSDResult();
	    	return result;  	
	    	}
	    
	    else{
	    System.out.println("Van answer, metadata: "
	    + answer.getRequestedService().getMetaData());

	    log.info("Creating GSDResult from GSDAnswer");
	    List<GSDEntry> gsdEntryList = new ArrayList<GSDEntry>();
	    GSDEntry gsdEntry = new GSDEntry(answer.getProviderCloud(), answer.getRequestedService());
	    gsdEntryList.add(gsdEntry);
	    
	    GSDResult result = new GSDResult(gsdEntryList);
	    log.info("result: " + result.getResponse().get(0));
	    	
	    return result;
	    }
    }
    
    /**
     * This function represents the Provider-side GSDPoll, where the GateKeeper send back 
     * a list of provider services, which match the demands of the consumer service     * 
     * 
     * @param GSDPoll
     * @return GSDAnswer
     */
    @PUT
    @Path("/gsd_poll/")
    public GSDAnswer getRequest(GSDPoll gsdPollRequest){
    	
    	log.info("GK gets a GSDPoll");
    	
    	GSDPoll gsdPoll = new GSDPoll();    
    	
    	gsdPoll = gsdPollRequest;
    	
    	ArrowheadCloud requesterCloud = gsdPoll.getRequesterCloud();
    	ArrowheadService requestedService = gsdPoll.getRequestedService();
    	ArrowheadCloud providerCloud = sysConfig.getOwnCloud();

    	String cloudOperator = requesterCloud.getOperator();
    	String cloudName = requesterCloud.getName();

    	String uri = "http://" + sysConfig.getAuthorizationURI() + "/operator/" + cloudOperator+"/cloud/"+cloudName;
//    	String uri = "http://localhost:8080/core/authorization/operator/"+cloudOperator+"/cloud/"+cloudName;
    	System.out.println(uri);

    	 //Sending an InterCloudAuthRequest to the Authorization System (generateToken=false)
    	log.info("Sending an interAuthRequest to Authorization: " + uri);
    	InterCloudAuthRequest interAuthRequest = new InterCloudAuthRequest(requesterCloud.getAuthenticationInfo(), requestedService, false);
    	Client client = ClientBuilder.newClient();
		WebTarget target = client.target(uri); 
	    Response response = target
	    		.request()
	    		.header("Content-type", "application/json")
	    		.put(Entity.json(interAuthRequest)); 

	    String respAuth;
	    log.info("Response from the Auth");
	    respAuth = response.readEntity(String.class);
	    log.info("Response from the Authorization in GSD: " + respAuth);
	    
    	
	    	// Generate a ServiceQueryForm from GSDPoll to send it to the Service Registry

    		log.info("Sending service to SR");
	    	ServiceQueryResult srvQueryResult = getServiceQueryResultGateKeeper(requestedService);

	    	if (srvQueryResult != null)
	    	{	

	    		log.info("Service found.");
	    		GSDAnswer answer = new GSDAnswer(gsdPollRequest.getRequestedService(),
	    				providerCloud);
	    		log.info("Service found, provider cloud: " + answer.getProviderCloud().getName());
	    		return answer;
	    	}
	    	else{
	    		log.info("No ServiceQueryResult");
	    		return null;
	    		}
    }
//	    }else {
//	    	log.info("Not authorized cloud in GSD");
//	    	return null; 
//	    }
//    }

    /**
     * This function represents the consumer-side ICN Proposal, where the consumer service matches
     * the provide service.
     * 
     * @param ICNRequestForm
     * @return ICNResultForm
     */
    @PUT
    @Path("/init_icn/")
    public ICNResultForm sendProposal(ICNRequestForm icnRequestForm){
    	
    	ICNProposal proposal = new ICNProposal();
    	
    	//Test ICNProposal
    	if (icnRequestForm == null){
    		log.info("No ICNRequestForm, creating a test request.");
    		proposal = testProposal();    	
    	}
    	else {
    		proposal = new ICNProposal(icnRequestForm.getRequestedService(), 
        			icnRequestForm.getAuthenticationInfo(), sysConfig.getOwnCloud(), icnRequestForm.getRequesterSystem());
    	}
    	
    	
    	// HTTP PUT to the provider GateKeeper
    	log.info("ICN Proposal for: " + proposal.getRequestedService().getServiceDefinition());
    	
    	String uri = "http://"+sysConfig.getCloudURIs().get(0).substring(0, 25)+
    			"gatekeeper/icn_proposal/";
    	
    	log.info("ICN Proposal to: " + uri);
    	
//    	String uri = "http://"+sysConfig.getGatekeeperURI()+"/icn_proposal/";

//    	String uri = "http://localhost:8080/core/gatekeeper/icn_proposal/";
    	
    	Client client = ClientBuilder.newClient();
		WebTarget target = client.target(uri);
	    Response response = target
	    		.request()
	    		.accept(MediaType.APPLICATION_JSON)
	    		.header("Content-type", "application/json")
	    		.put(Entity.json(proposal)); 
	    ICNResultForm icnResult = new ICNResultForm(response.readEntity(ICNEnd.class));
	    
	    log.info("GK gets an ICNResultForm");
	    
	    return icnResult;
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
    public ICNEnd getICNEnd (ICNProposal icnProposalRequest){

    	log.info("GK gets an ICNProposal");
    	
    	ICNProposal icnProposal = icnProposalRequest;
    	
    	String cloudOperator = icnProposal.getRequestedCloud().getOperator();
    	String cloudName = icnProposal.getRequestedCloud().getName();
    	
    	ArrowheadService requestedService = icnProposal.getRequestedService();
    	ArrowheadSystem requesterSystem = icnProposal.getRequesterSystem();
    	
    	
//    	String uri = "http://" + sysConfig.getAuthorizationURI() + "/operator/" + cloudOperator+"/cloud/"+cloudName;
    	String uri = "http://localhost:8080/core/authorization/operator/"+cloudOperator+"/cloud/"+cloudName;
    	System.out.println(uri);

    	// Sending an InterCloudAuthRequest to the Authorization System (generateToken=true)
    	log.info("Sending an InterCloudAuthRequest to the Authorization System");
    	InterCloudAuthRequest interAuthRequest = new InterCloudAuthRequest(icnProposal.getRequestedCloud().getAuthenticationInfo(), requestedService, true);
    	Client client = ClientBuilder.newClient();
		WebTarget target = client.target(uri); 
	    Response response = target
	    		.request()
	    		.header("Content-type", "application/json")
	    		.put(Entity.json(interAuthRequest));

	    String respAuth;
	    log.info("Response from the Auth");
	    respAuth = response.readEntity(String.class);
	    log.info("Response from the Authorization in ICN: " + respAuth);
	    
	    	
	    	// Send a HTTP POST to Orchestrator
    		log.info("Sending SRF to Orchestrator");
    		Map<String, Boolean> orchestrationFlags = new HashMap<>();
    		orchestrationFlags.put("matchmaking", false);
    		orchestrationFlags.put("externalServiceRequest", true);
    		orchestrationFlags.put("triggerInterCloud", false);
    		orchestrationFlags.put("metadataSearch", false);
    		orchestrationFlags.put("pingProvider", false);
    		ServiceRequestForm serviceRequestForm = new ServiceRequestForm(requestedService, "requestedQoS", requesterSystem, orchestrationFlags);
			
			Client client2 = ClientBuilder.newClient();

			String uri2 = "http://" + sysConfig.getOrchestratorURI();
//	    	String uri2 = "http://localhost:8080/core/orchestrator/orchestration";
			System.out.println(uri2);
			
			WebTarget target2 = client2.target(uri2); 
		    Response response2 = target2
		    		.request()
		    		.header("Content-type", "application/json")
		    		.post(Entity.json(serviceRequestForm));
		    ICNEnd  icnEND = new ICNEnd(response2.readEntity(OrchestrationResponse.class));
    		log.info("Sending ICNEnd back");

		    return icnEND;
    }
//	    else {
//	    log.info("Not authorized cloud in ICN.");
//	    return null;  }  
	    
//    }

    


	/**
	 * Sends the Service Query Form to the Service Registry and asks for the
	 * Service Query Result.
	 * 
	 * @param sqf
	 * @return ServiceQueryResult
	 */
  //Copy from eu.arrowhead.core.orchestrator.OrchestrationService
  	private ServiceQueryResult getServiceQueryResultGateKeeper(ArrowheadService arrService) {
  		System.out.println("GK: inside the getServiceQueryResult function");
  		log.info("GK: inside the getServiceQueryResult function");
		ArrowheadService as = arrService;
		String strtarget = "http://" + sysConfig.getServiceRegistryURI()+ "/" + as.getServiceGroup() + "/" + as.getServiceDefinition();
		System.out.println("GK: sending the ServiceQueryForm to this address:" + strtarget);
		log.info("GK: sending the ServiceQueryForm to this address:" + strtarget);
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(strtarget);
		
		ServiceQueryForm sqf = new ServiceQueryForm
				(as.getMetaData(), as.getInterfaces(), false, false, 
						"RIuxP+vb5GjLXJo686NvKQ==");
		
		Response response = target.request().header("Content-type", "application/json").put(Entity.json(sqf));
		ServiceQueryResult sqr = response.readEntity(ServiceQueryResult.class);
		System.out.println("GK received the following services from the SR:");
		log.info("GK received something");
		for (ProvidedService providedService : sqr.getServiceQueryData()) {
			log.info("GK received the following services from the SR: " + providedService.getProvider().getSystemGroup() + providedService.getProvider().getSystemName());
		}
		return sqr;
  	}

  	
  	/**
  	 * Create a GSDPoll for testing
  	 * 
  	 * @return GSDPoll
  	 */
    protected GSDPoll testGSDPoll(){
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
    protected ICNProposal testProposal() {
    	List<String> interfaces = new ArrayList<String>();
		interfaces.add("inf2");
		interfaces.add("inf4");
		ArrowheadService requestedService = new ArrowheadService("sg4", "sd4", interfaces, "md4");
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
	protected ServiceQueryResult testServiceQueryResult(){
		ArrowheadSystem provider = new ArrowheadSystem("a", "g", "f", "fd", "dd");
		ProvidedService providedService = new ProvidedService(provider , "serviceURI", "serviceInterface");
		List<ProvidedService> testservices = new ArrayList<ProvidedService>();
		testservices.add(providedService);
		ServiceQueryResult sqr = new ServiceQueryResult(testservices);
		return sqr;
	}
    
}