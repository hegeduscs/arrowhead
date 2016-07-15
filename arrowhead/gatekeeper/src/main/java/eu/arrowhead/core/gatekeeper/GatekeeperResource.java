package eu.arrowhead.core.gatekeeper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.configuration.SysConfig;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.messages.GSDAnswer;
import eu.arrowhead.common.model.messages.GSDPoll;
import eu.arrowhead.common.model.messages.GSDRequestForm;
import eu.arrowhead.common.model.messages.GSDResult;
import eu.arrowhead.common.model.messages.ICNEnd;
import eu.arrowhead.common.model.messages.ICNProposal;
import eu.arrowhead.common.model.messages.ICNRequestForm;
import eu.arrowhead.common.model.messages.ICNResult;
import eu.arrowhead.common.model.messages.InterCloudAuthRequest;
import eu.arrowhead.common.model.messages.InterCloudAuthResponse;
import eu.arrowhead.common.model.messages.OrchestrationResponse;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;
import eu.arrowhead.common.model.messages.ServiceRequestForm;

/**
 * @author umlaufz
 */
@Path("gatekeeper")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GatekeeperResource {
	
	private static Logger log = Logger.getLogger(GatekeeperResource.class.getName());
	
	@GET
    public String getIt() {
	    return "This is the Gatekeeper Resource stub.";
    }
	
	/**
	 * This function represents the consumer-side of GlobalServiceDiscovery, 
	 * where the GateKeeper of the consumer System tries to find a provider Cloud
	 * for the requested Service.
	 * 
	 * @param GSDRequestForm
	 * @return GSDResult
	 * @throws BadPayloadException
	 */
	@PUT
	@Path("init_gsd")
	public Response GSDRequest(GSDRequestForm requestForm) {
		log.info("Entered the GateKeeperResource for GSD Polling.");
		
		if(!requestForm.isPayloadUsable()){
			log.info("Payload is not usable.");
			throw new BadPayloadException("Bad payload: missing/incomplete requestedService."
					+ "Mandatory fields: serviceGroup, serviceDefinition, interfaces.");
		}
		
		ArrowheadCloud ownCloud = SysConfig.getOwnCloud();
		log.info("Own cloud info acquired");
		GSDPoll gsdPoll = new GSDPoll(requestForm.getRequestedService(), ownCloud);
		
		List<String> cloudURIs = new ArrayList<String>();
		if(requestForm.getSearchPerimeter() == null || requestForm.getSearchPerimeter().isEmpty()){
			cloudURIs = SysConfig.getNeighborCloudURIs();
			log.info(cloudURIs.size() + "NeighborCloud URIs acquired.");
		}
		else{
			/*
			 * Using a Set removes duplicate entries (which are needed for the Orchestrator) 
			 * from the Cloud list.
			 */
			Set<ArrowheadCloud> preferredClouds = new LinkedHashSet<>(requestForm.getSearchPerimeter());
			String URI = null;
			for(ArrowheadCloud cloud : preferredClouds){
				URI = SysConfig.getURI(cloud.getAddress(), cloud.getPort(), 
						cloud.getGatekeeperServiceURI());
				cloudURIs.add(URI);
				log.info(cloudURIs.size() + "preferred cloud URIs acquired.");
			}
		}
		
		List<GSDAnswer> gsdAnswerList = new ArrayList<GSDAnswer>();
		for(String URI : cloudURIs){
			URI = UriBuilder.fromPath(URI).path("gsd_poll").toString();
			Response response = Utility.sendRequest(URI, "PUT", gsdPoll);
			log.info("Sent GSD Poll request to: " + URI);
			GSDAnswer gsdAnswer = response.readEntity(GSDAnswer.class);
			if(gsdAnswer != null){
				log.info("NeighborCloud " + gsdAnswer.getProviderCloud() + " responded to GSD Poll");
				gsdAnswerList.add(gsdAnswer);
			}
		}
		
		log.info("Sending GSD Poll results to Orchestrator.");
		GSDResult gsdResult = new GSDResult(gsdAnswerList);
		return Response.ok().entity(gsdResult).build();
	}
	
    /**
     * This function represents the provider-side of GlobalServiceDiscovery, 
     * where the GateKeeper of the provider Cloud sends back its information
     * if the Authorization and Service Registry polling yields positive results.
     * 
     * @param GSDPoll
     * @return GSDAnswer
     */
	@PUT
    @Path("gsd_poll")
    public Response GSDPoll(GSDPoll gsdPoll) {
		log.info("Gatekeeper received a GSD Poll request");
    	
		//Polling the Authorization System about the consumer Cloud
		ArrowheadCloud cloud = gsdPoll.getRequesterCloud();
		ArrowheadService service = gsdPoll.getRequestedService();
		InterCloudAuthRequest authRequest = new InterCloudAuthRequest(cloud, service, false);
		
		String authURI = SysConfig.getAuthorizationURI();
		authURI = UriBuilder.fromPath(authURI).path("intercloud").toString();
		Response authResponse = Utility.sendRequest(authURI, "PUT", authRequest);
		log.info("Authorization System queried for requester Cloud: " + 
				gsdPoll.getRequesterCloud().getCloudName());
		
		//If the consumer Cloud is not authorized null is returned
		if(!authResponse.readEntity(InterCloudAuthResponse.class).isAuthorized()){
			log.info("Requester Cloud is UNAUTHORIZED");
			return Response.status(Status.UNAUTHORIZED).entity(null).build();
		}
		
		//If it is authorized, poll the Service Registry for the requested Service
		else{
			log.info("Requester Cloud is AUTHORIZED");
			
			//Compiling the URI and the request payload
			String srURI = SysConfig.getServiceRegistryURI();
			srURI = UriBuilder.fromPath(srURI).path(service.getServiceGroup())
					.path(service.getServiceDefinition()).toString();
			String tsig_key = SysConfig.getCoreSystem("serviceregistry").getAuthenticationInfo();
			ServiceQueryForm queryForm = new ServiceQueryForm(service.getServiceMetadata(), service.getInterfaces(),
					false, false, tsig_key);
			
			//Sending back provider Cloud information if the SR poll has results
			Response srResponse = Utility.sendRequest(srURI, "PUT", queryForm);
			log.info("ServiceRegistry queried for requested Service: " + service.getServiceDefinition());
			ServiceQueryResult result = srResponse.readEntity(ServiceQueryResult.class);
			if(result.isPayloadEmpty()){
				log.info("ServiceRegistry query came back empty.");
				return Response.noContent().entity(null).build();
			}
			
			log.info("Sending back GSD answer to requester Cloud.");
			GSDAnswer answer = new GSDAnswer(service, SysConfig.getOwnCloud());
			return Response.ok().entity(answer).build();
		}
    }
	
	/**
	 * This function represents the consumer-side of InterCloudNegotiations, 
	 * where the Gatekeeper sends information about the requester System.
	 * (SSL secured)
	 * 
	 * @param ICNRequestForm
	 * @return ICNResult
	 * @throws BadPayloadException
	 */
    @PUT
    @Path("init_icn")
    public Response ICNRequest(ICNRequestForm requestForm) {
    	log.info("Entered the GateKeeperResource for ICN Proposal.");
    	
    	if(!requestForm.isPayloadUsable()){
    		log.info("Payload is not usable.");
    		throw new BadPayloadException("Bad payload: missing/incomplete ICNRequestForm.");
    	}
    	
    	log.info("Compiling ICN proposal");
    	ICNProposal icnProposal = new ICNProposal(requestForm.getRequestedService(), 
    			requestForm.getAuthenticationInfo(), SysConfig.getOwnCloud(), 
    			requestForm.getRequesterSystem(), requestForm.getPreferredProviders());
    	
    	String icnURI = SysConfig.getURI(requestForm.getTargetCloud().getAddress(),
    			requestForm.getTargetCloud().getPort(), 
    			requestForm.getTargetCloud().getGatekeeperServiceURI());
    	icnURI = UriBuilder.fromPath(icnURI).path("icn_proposal").toString();
    	
    	log.info("Sending ICN proposal to provider Cloud: " + icnURI);
    	Response response = Utility.sendRequest(icnURI, "PUT", icnProposal);
    	ICNResult result = new ICNResult(response.readEntity(ICNEnd.class));

    	log.info("Returning ICN result to Orchestrator.");
    	return Response.status(response.getStatus()).entity(result).build();
    }
    
    /**
     * This function represents the provider-side of InterCloudNegotiations, 
	 * where the Gatekeeper (after an Orchestration process) sends information 
	 * about the provider System.
	 * (SSL secured)
	 * 
     * @param ICNProposal
     * @return ICNEnd
     */
    @PUT
    @Path("icn_proposal")
    public Response ICNProposal (ICNProposal icnProposal) {
    	log.info("Gatekeeper received an ICN proposal.");
    	
    	//Polling the Authorization System about the consumer Cloud
		ArrowheadCloud cloud = icnProposal.getRequesterCloud();
		ArrowheadService service = icnProposal.getRequestedService();
		InterCloudAuthRequest authRequest = new InterCloudAuthRequest(cloud, service, false);
		
		String authURI = SysConfig.getAuthorizationURI();
		authURI = UriBuilder.fromPath(authURI).path("intercloud").toString();
		Response authResponse = Utility.sendRequest(authURI, "PUT", authRequest);
		log.info("Authorization System queried for requester Cloud: " + cloud.getCloudName());
		
		//If the consumer Cloud is not authorized null is returned
		if(!authResponse.readEntity(InterCloudAuthResponse.class).isAuthorized()){
			log.info("Requester Cloud is UNAUTHORIZED");
			return Response.status(Status.UNAUTHORIZED).entity(null).build();
		}
		
		/*
		 * If it is authorized, send a ServiceRequestForm to the Orchestrator 
		 * and return the OrchestrationResponse
		 */
		else{
			log.info("Requester Cloud is AUTHORIZED");
			
			//TODO review the flag values here
			Map<String, Boolean> orchestrationFlags = new HashMap<String, Boolean>();
			orchestrationFlags.put("triggerInterCloud", false);
			orchestrationFlags.put("externalServiceRequest", true);
			orchestrationFlags.put("enableInterCloud", false);
			orchestrationFlags.put("metadataSearch", false);
			orchestrationFlags.put("pingProviders", false);
			orchestrationFlags.put("overrideStore", false);
			orchestrationFlags.put("storeOnlyActive", false);
			orchestrationFlags.put("matchmaking", false);
			orchestrationFlags.put("hasPreferences", false);
			orchestrationFlags.put("onlyPreferred", false);
			orchestrationFlags.put("generateToken", false);
			
			ServiceRequestForm serviceRequestForm = 
					new ServiceRequestForm(icnProposal.getRequesterSystem(), service, null,
							orchestrationFlags, null, icnProposal.getPreferredProviders());
			String orchestratorURI = SysConfig.getOrchestratorURI();
			
			log.info("Sending ServiceRequestForm to the Orchestrator.");
			Response response = Utility.sendRequest(orchestratorURI, "POST", serviceRequestForm);
			OrchestrationResponse orchResponse = response.readEntity(OrchestrationResponse.class);
			
			log.info("Returning the OrchestrationResponse to the requester Cloud.");
			return Response.status(response.getStatus()).entity(new ICNEnd(orchResponse)).build();	
		}
    }

    
}
