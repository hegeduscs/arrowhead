package eu.arrowhead.core.gatekeeper;

import java.util.ArrayList;
import java.util.List;

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
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;

/**
 * @author umlaufz
 */
@Path("gatekeeper")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GatekeeperResource {
	
	//private static Logger log = Logger.getLogger(GatekeeperResource.class.getName());
	
	@GET
    public String getIt() {
	    return "This is the Gatekeeper Resource stub.";
    }
	
	/**
	 * This function represents the Consumer-side of GlobalServiceDiscovery, 
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
		
		if(!requestForm.isPayloadUsable()){
			throw new BadPayloadException("Bad payload: missing/incomplete requestedService."
					+ "Mandatory fields: serviceGroup, serviceDefinition, interfaces.");
		}
		
		ArrowheadCloud ownCloud = SysConfig.getOwnCloud();
		GSDPoll gsdPoll = new GSDPoll(requestForm.getRequestedService(), ownCloud);
		
		List<String> cloudURIs = new ArrayList<String>();
		cloudURIs = SysConfig.getCloudURIs();
		List<GSDAnswer> gsdAnswerList = new ArrayList<GSDAnswer>();
		for(String URI : cloudURIs){
			URI = UriBuilder.fromPath(URI).path("gsd_poll").toString();
			Response response = Utility.sendRequest(URI, "PUT", gsdPoll);
			GSDAnswer gsdAnswer = response.readEntity(GSDAnswer.class);
			if(gsdAnswer != null){
				gsdAnswerList.add(gsdAnswer);
			}
		}
		
		GSDResult gsdResult = new GSDResult(gsdAnswerList);
		
		return Response.ok().entity(gsdResult).build();
	}
	
    /**
     * This function represents the Provider-side of GlobalServiceDiscovery, 
     * where the GateKeeper of the provider Cloud sends back its information
     * if the Authorization and Service Registry polling yields positive results.
     * 
     * @param GSDPoll
     * @return GSDAnswer
     */
	@PUT
    @Path("gsd_poll")
    public Response GSDPoll(GSDPoll gsdPoll) {
    	
		//Polling the Authorization System about the consumer Cloud
		ArrowheadCloud cloud = gsdPoll.getRequesterCloud();
		ArrowheadService service = gsdPoll.getRequestedService();
		InterCloudAuthRequest authRequest = new InterCloudAuthRequest(cloud, service, false);
		
		String authURI = SysConfig.getAuthorizationURI();
		authURI = UriBuilder.fromPath(authURI).path("intercloud").toString();
		Response authResponse = Utility.sendRequest(authURI, "PUT", authRequest);
		
		//If the consumer Cloud is not authorized null is returned
		if(!authResponse.readEntity(InterCloudAuthResponse.class).isAuthorized()){
			return Response.status(Status.UNAUTHORIZED).entity(null).build();
		}
		
		//If it is authorized, poll the Service Registry for the requested Service
		else{
			//Compiling the URI and the request payload
			String srURI = SysConfig.getServiceRegistryURI();
			srURI = UriBuilder.fromPath(srURI).path(service.getServiceGroup())
					.path(service.getServiceDefinition()).toString();
			String tsig_key = SysConfig.getCoreSystem("serviceregistry").getAuthenticationInfo();
			ServiceQueryForm queryForm = new ServiceQueryForm(service.getMetaData(), service.getInterfaces(),
					false, false, tsig_key);
			
			//Sending back provider Cloud information if the SR poll has results
			Response srResponse = Utility.sendRequest(srURI, "PUT", queryForm);
			ServiceQueryResult result = srResponse.readEntity(ServiceQueryResult.class);
			if(result.isPayloadEmpty()){
				return Response.noContent().entity(null).build();
			}
			
			GSDAnswer answer = new GSDAnswer(service, SysConfig.getOwnCloud());
			return Response.ok().entity(answer).build();
		}
    }
	
	/**
	 * 
	 * @param ICNRequestForm
	 * @return ICNResult
	 */
    @PUT
    @Path("init_icn")
    public Response ICNRequest(ICNRequestForm requestForm) {
    	
    	return null;
    }
    
    /**
     * 
     * @param ICNProposal
     * @return ICNEnd
     */
    @PUT
    @Path("icn_proposal")
    public Response ICNProposal (ICNProposal icnProposal) {
    	
    	return null;
    }

    
}
