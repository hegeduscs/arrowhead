package eu.arrowhead.core.gatekeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import eu.arrowhead.common.configuration.SysConfig;
import eu.arrowhead.common.exception.BadPayloadException;
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
	private SysConfig sysConfig = SysConfig.getInstance();
	private static Logger log = Logger.getLogger(GatekeeperResource.class.getName());

	
	@GET
    public String getIt() {
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
    	ArrowheadCloud requesterCloud = sysConfig.getOwnCloud();
    	//XXX: INTERNAL / OWN
    	
    	log.info("Got the cloud info");
    	GSDPoll gsdPoll = new GSDPoll();
    	
    	if (gsdRequest==null) {
			throw new BadPayloadException(
					"GateKeeper: Bad payload from Orchestrator: Missing/wrong parameters in GSDRequestForm.");
		}
    	
    	else {
    		log.info("Creating GSDPoll for requester service");
    		gsdPoll = new GSDPoll(gsdRequest.getRequestedService(),requesterCloud);
    		}
    	
    	
    	// HTTP PUT to the provider GateKeeper
   		log.info("Starting to find provider service for: "+ gsdPoll.getRequestedService().getServiceDefinition());
   		Client client = ClientBuilder.newClient();
   		
		//TODO: "http://"
    	String uri = "http://"+sysConfig.getCloudURIs().get(0).substring(0, 25)+"gatekeeper/gsd_poll/";    	
    	System.out.println(uri);
    	
		WebTarget target = client.target(uri);
		log.info("Sending GSDPoll to other GateKeeper");
	    Response response = target
	    		.request()
	    		.header("Content-type", "application/json")
				.put(Entity.json(gsdPoll));
	    GSDAnswer answer = response.readEntity(GSDAnswer.class);
	    
	    //Checking the GSDAnswer
	    if (answer.getRequestedService() == null){
	    	log.info("Provider service not found.");
	    	GSDResult result = new GSDResult();
	    	return result;  	
	    	}
	    
	    else{
	    	
	    //Creating GSDResult for the Orchestrator
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
    	GSDAnswer emptyAnswer = new GSDAnswer();
    	GSDPoll gsdPoll = new GSDPoll();    
    	
    	if (gsdPollRequest==null) {
			throw new BadPayloadException(
					"GateKeeper: Bad payload from GateKeeper: Missing/wrong parameters in GSDPoll.");
		}    	
    	else {
    		gsdPoll = gsdPollRequest;
    	}
    	
    	
    	ArrowheadService requestedService = gsdPoll.getRequestedService();
    	ArrowheadCloud providerCloud = sysConfig.getOwnCloud();
    	//XXX: INTERNAL / OWN
    	
    	 //Sending an InterCloudAuthRequest to the Authorization System (generateToken=false)
    	log.info("Creating an InterCloudAuthRequest to the Authorization System");
    	InterCloudAuthRequest interAuthRequest = new InterCloudAuthRequest(
    			requestedService, gsdPoll.getRequesterCloud().getAuthenticationInfo(), false);
    	
    	String AuthorizationResponse = getAuthorizationResponse(interAuthRequest,
    			gsdPoll.getRequesterCloud());

    	if(AuthorizationResponse=="false")
    	{
    		log.info("Cloud is not authorized. Sending back an empty GSDAnswer.");
    		System.out.println("Cloud is not authorized. Sending back an empty GSDAnswer.");
    		return emptyAnswer;
    	}
    	else{
	    
	    	// Generate a ServiceQueryForm from GSDPoll to send it to the Service Registry
    		log.info("Cloud is authorized, sending service to SR");
	    	ServiceQueryResult srvQueryResult = getServiceQueryResultGateKeeper(requestedService);

	    	if (srvQueryResult != null)
	    	{	
	    		log.info("Service found.");
	    		GSDAnswer answer = new GSDAnswer(gsdPollRequest.getRequestedService(),providerCloud);
	    		log.info("Service found, provider cloud: " + answer.getProviderCloud().getName());
	    		return answer;
	    	}
	    	else{
	    		log.info("No service found. Sending back an empty GSDAnswer.");
	    		return emptyAnswer;
	    		}
    }
    }

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
    	
    	if (icnRequestForm == null){
    		throw new BadPayloadException(
					"GateKeeper: Bad payload from Orchestrator: Missing/wrong parameters in ICNRequestForm.");
    		}
    	else {
    		proposal = new ICNProposal(icnRequestForm.getRequestedService(), 
        			icnRequestForm.getAuthenticationInfo(), sysConfig.getOwnCloud(), icnRequestForm.getRequesterSystem());
    		//XXX: INTERNAL / OWN
    	}
    	
    	// HTTP PUT to the provider GateKeeper
    	log.info("ICN Proposal for: " + proposal.getRequestedService().getServiceDefinition());
    	
		//TODO: "http://"
    	String uri = "http://"+sysConfig.getCloudURIs().get(0).substring(0, 25)+"gatekeeper/icn_proposal/";
    	
    	log.info("ICN Proposal to the chosen GateKeeper.");
    	System.out.println("ICN Proposal to: " + uri);
    	
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
	    
	    log.info("GK gets an ICNResultForm, GateKeeper ends succesfully.");
	    
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
    	
    	ICNProposal icnProposal = new ICNProposal();
    	ICNEnd emptyAnswer = new ICNEnd();
    	
    	if (icnProposalRequest == null){
    		throw new BadPayloadException(
					"GateKeeper: Bad payload from GateKeeper: Missing/wrong parameters in ICNProposal.");
    		}
    	else {
        	icnProposal = icnProposalRequest;
    	}
    	
    	ArrowheadService requestedService = icnProposal.getRequestedService();
    	ArrowheadSystem requesterSystem = icnProposal.getRequesterSystem();
    	
    	// Sending an InterCloudAuthRequest to the Authorization System (generateToken=true)
    	log.info("Creating an InterCloudAuthRequest to the Authorization System");
    	InterCloudAuthRequest interAuthRequest = new InterCloudAuthRequest(requestedService, 
    			icnProposal.getRequestedCloud().getAuthenticationInfo(), true);
    	
    	String AuthorizationResponse = getAuthorizationResponse(interAuthRequest,
    			icnProposal.getRequestedCloud());
	    
    	if(AuthorizationResponse=="false")
    	{
    		log.info("Cloud is not authorized. Sending back an empty GSDAnswer.");
    		System.out.println("Cloud is not authorized. Sending back an empty GSDAnswer.");
    		return emptyAnswer;
    	}
    	else{
	    	// Send a HTTP POST to Orchestrator
    		log.info("Cloud is authorized, sending SRF to Orchestrator");
    		OrchestrationResponse orchResponse = getOrchestrationResponse(
    				requestedService,"requestedQoS", requesterSystem);

    		log.info("Sending ICNEnd back");
    		ICNEnd  icnEND = new ICNEnd(orchResponse);
		    return icnEND;
    	}
    }
    
    /**
     * Sends an InterCloudAuthRequest to the Authorization.
     * It checks whether the cloud is authorized.
     * 
     * @param interAuthRequest
     * @param requesterCloud
     * @return "true" or "false"
     */
    private String getAuthorizationResponse(
			InterCloudAuthRequest interAuthRequest, ArrowheadCloud requesterCloud) {

    	//TODO: "http://"
    	String uri = "http://" + sysConfig.getAuthorizationURI() + 
    			"/operator/" + requesterCloud.getOperator()+
    			"/cloud/"+requesterCloud.getName();
//    	String uri = "http://localhost:8080/core/authorization/operator/"+cloudOperator+"/cloud/"+cloudName;
    	System.out.println(uri);
    	
    	Client client = ClientBuilder.newClient();
		WebTarget target = client.target(uri); 

		log.info("Sending an InterCloudAuthRequest to the Authorization System");
	    Response response = target
	    		.request()
	    		.header("Content-type", "application/json")
	    		.put(Entity.json(interAuthRequest));
	    log.info("Response from the Auth");
	    String respAuth = response.readEntity(String.class);
		return respAuth;
	}


	/**
     * Create a ServiceRequestForm, then sends it to the Orchestrator, 
     * and returns back with an OrchestrationResponse.
     * 
     * @param requestedService
     * @param string
     * @param requesterSystem
     * @return OrchestrationResponse
     */
	private OrchestrationResponse getOrchestrationResponse(
			ArrowheadService requestedService, String string,
			ArrowheadSystem requesterSystem) {
		
		Map<String, Boolean> orchestrationFlags = new HashMap<>();
		orchestrationFlags.put("matchmaking", false);
		orchestrationFlags.put("externalServiceRequest", true);
		orchestrationFlags.put("triggerInterCloud", false);
		orchestrationFlags.put("metadataSearch", false);
		orchestrationFlags.put("pingProvider", false);
		
		log.info("Creating a ServiceRequestForm to the Orchestrator");
		ServiceRequestForm serviceRequestForm = new ServiceRequestForm(requestedService, "requestedQoS", requesterSystem, orchestrationFlags);
		
		Client client2 = ClientBuilder.newClient();

		//TODO: "http://"
		String uri2 = "http://" + sysConfig.getOrchestratorURI();
//    	String uri2 = "http://localhost:8080/core/orchestrator/orchestration";
		System.out.println(uri2);
		
		log.info("Sending the ServiceRequestForm to the Orchestrator");
		WebTarget target2 = client2.target(uri2); 
	    Response response2 = target2
	    		.request()
	    		.header("Content-type", "application/json")
	    		.post(Entity.json(serviceRequestForm));
	    OrchestrationResponse orchResponse = response2.readEntity(OrchestrationResponse.class);
	    
		return orchResponse;
	}


	/**
	 * Sends the Service Query Form to the Service Registry and asks for the
	 * Service Query Result.
	 * 
	 * @param sqf
	 * @return ServiceQueryResult
	 */
  //Copy from eu.arrowhead.core.orchestrator.OrchestrationService
  	private ServiceQueryResult getServiceQueryResultGateKeeper(ArrowheadService arrService) {
  		log.info("GK: inside the getServiceQueryResult function");
		ArrowheadService as = arrService;
		String strtarget = "http://" + sysConfig.getServiceRegistryURI()+ "/" + as.getServiceGroup() + "/" + as.getServiceDefinition();
		System.out.println("GK: sending the ServiceQueryForm to this address:" + strtarget);
		log.info("GK: sending the ServiceQueryForm to this address:" + strtarget);
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(strtarget);
		
		ServiceQueryForm sqf = new ServiceQueryForm
				(as.getMetaData(), as.getInterfaces(), false, false,"RIuxP+vb5GjLXJo686NvKQ==");
		
		Response response = target.request().header("Content-type", "application/json").put(Entity.json(sqf));
		ServiceQueryResult sqr = response.readEntity(ServiceQueryResult.class);
		log.info("GK received something");
		for (ProvidedService providedService : sqr.getServiceQueryData()) {
			log.info("GK received the following services from the SR: " + providedService.getProvider().getSystemGroup() + providedService.getProvider().getSystemName());
		}
		return sqr;
  	}
}