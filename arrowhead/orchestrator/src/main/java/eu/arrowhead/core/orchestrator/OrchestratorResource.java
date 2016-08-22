package eu.arrowhead.core.orchestrator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.model.messages.OrchestrationResponse;
import eu.arrowhead.common.model.messages.ServiceRequestForm;
import eu.arrowhead.common.ssl.SecurityUtils;

/**
 * This is the REST resource for the Orchestrator Core System.
 */
@Path("orchestration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrchestratorResource {
	
	@Context
	Configuration configuration;
	private static Logger log = Logger.getLogger(OrchestratorResource.class.getName());
	
	/*
	 * Simple test method to see if the http server where this resource is registered works or not.
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt(@Context SecurityContext sc) {
		if (sc.isSecure()) {
			System.out.println("Channel is secure.");
			if(!isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
		}
		return "Got it!";
	}
	
	/**
	 * This method initiates the correct orchestration process determined by orchestration flags
	 * in the service request form. The returned response (can) consists a list of endpoints
	 * where the requester System can consume the requested Service.
	 * 
	 * @param ServiceRequestForm srf
	 * @return OrchestrationResponse
	 * @throws BadPayloadException
	 */
	@POST
	public Response orchestrationProcess(@Context SecurityContext sc, ServiceRequestForm srf){
		log.info("Entered the orchestrationProcess method.");
		
		if (sc.isSecure()) {
			log.info("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		if(!srf.isPayloadUsable()){
			log.info("OrchestratorResource:orchestrationProcess throws BadPayloadException");
			throw new BadPayloadException("Bad payload: service request form has missing/incomplete "
					+ "mandatory fields.");
		}

		OrchestrationResponse orchResponse = new OrchestrationResponse();
		if (srf.getOrchestrationFlags().get("externalServiceRequest")){
			log.info("Received an externalServiceRequest.");
			orchResponse = OrchestratorService.externalServiceRequest(srf);
			log.info("externalServiceRequest orchestration returned with " 
					+ orchResponse.getResponse().size() + " orchestration forms.");
		}
		else if(srf.getOrchestrationFlags().get("triggerInterCloud")){
			log.info("Received a triggerInterCloud request.");
			orchResponse = OrchestratorService.triggerInterCloud(srf);
			log.info("triggerInterCloud orchestration returned with " 
					+ orchResponse.getResponse().size() + " orchestration forms.");
		}
		else if(!srf.getOrchestrationFlags().get("overrideStore")){ //overrideStore == false
			log.info("Received an orchestrationFromStore request.");
			orchResponse = OrchestratorService.orchestrationFromStore(srf);
			log.info("orchestrationFromStore returned with " 
					+ orchResponse.getResponse().size() + " orchestration forms.");
		}
		else{
			log.info("Received a regularOrchestration request.");
			orchResponse = OrchestratorService.dynamicOrchestration(srf);
			log.info("regularOrchestration returned with " 
					+ orchResponse.getResponse().size() + " orchestration forms.");
		}
		
		return Response.status(Status.OK).entity(orchResponse).build();
	}
	
	private static boolean isClientAuthorized(SecurityContext sc, Configuration configuration){
		String subjectname = sc.getUserPrincipal().getName();
		String clientCN = SecurityUtils.getCertCNFromSubject(subjectname);
		log.info("The client common name for the request: " + clientCN);
		String serverCN = (String) configuration.getProperty("server_common_name");
		
		String[] serverFields = serverCN.split("\\.", -1);
		String[] clientFields = clientCN.split("\\.", -1);
		String serverCNend = "";
		String clientCNend = "";
		if(serverFields.length < 3 || clientFields.length < 3){
			log.info("SSL error: one of the CNs have less than 3 fields!");
			return false;
		}
		else{
			for(int i = 2; i < serverFields.length; i++){
				serverCNend = serverCNend.concat(serverFields[i]);
			}
			
			for(int i = 2; i < clientFields.length; i++){
				clientCNend = clientCNend.concat(clientFields[i]);
			}
		}
		
		if(!clientCNend.equalsIgnoreCase(serverCNend)){
			log.info("SSL error: common names are not equal!");
			return false;
		}
		
		return true;
	}

}
