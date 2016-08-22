package eu.arrowhead.core.authorization;

import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import eu.arrowhead.common.configuration.DatabaseManager;
import eu.arrowhead.common.database.InterCloudAuthorization;
import eu.arrowhead.common.database.IntraCloudAuthorization;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.InterCloudAuthRequest;
import eu.arrowhead.common.model.messages.InterCloudAuthResponse;
import eu.arrowhead.common.model.messages.IntraCloudAuthRequest;
import eu.arrowhead.common.model.messages.IntraCloudAuthResponse;
import eu.arrowhead.common.ssl.SecurityUtils;

/**
 * This is the REST resource for the Authorization Core System.
 */
@Path("authorization")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorizationResource {

	@Context
	Configuration configuration;
	DatabaseManager dm = DatabaseManager.getInstance();
	HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
	private static Logger log = Logger.getLogger(AuthorizationResource.class.getName());
	
	@GET
    public String getIt() {
	    return "This is the Authorization Resource.";
    }

	/**
	 * Checks whether the consumer System can use a Service from a list of
	 * provider Systems.
	 * 
	 * @param IntraCloudAuthRequest request
	 * @exception DataNotFoundException, BadPayloadException
	 * @return IntraCloudAuthResponse
	 */
	@PUT
	@Path("/intracloud")
	public Response isSystemAuthorized(@Context SecurityContext sc, IntraCloudAuthRequest request) {
		log.info("Entered the AuthorizationResource:isSystemAuthorized function");
		
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
		
		if (!request.isPayloadUsable()) {
			log.info("AuthorizationResource:isSystemAuthorized BadPayloadException");
			throw new BadPayloadException("Bad payload: Missing/incomplete consumer, service"
					+ " or providerList in the request payload.");
		}
		
		IntraCloudAuthResponse response = new IntraCloudAuthResponse();
		restrictionMap.put("systemGroup", request.getConsumer().getSystemGroup());
		restrictionMap.put("systemName", request.getConsumer().getSystemName());
		ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
		if(consumer == null){
			log.info("Consumer is not in the database. "
					+ "(AuthorizationResource:isSystemAuthorized DataNotFoundException)");
			throw new DataNotFoundException(
				"Consumer System is not in the authorization database. " + 
				request.getConsumer().toString());
        }
		
		HashMap<ArrowheadSystem, Boolean> authorizationState = new HashMap<ArrowheadSystem, Boolean>();
		log.info("authorizationState hashmap created");
		
		restrictionMap.clear();
		restrictionMap.put("serviceGroup", request.getService().getServiceGroup());
		restrictionMap.put("serviceDefinition", request.getService().getServiceDefinition());
		ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
		if (service == null) {
			log.info("Service is not in the database. Returning NOT AUTHORIZED state. "
					+ request.getService().toString());
			for (ArrowheadSystem provider : request.getProviders()) {
				authorizationState.put(provider, false);
			}
			response.setAuthorizationMap(authorizationState);
			return Response.status(Status.OK).entity(response).build();
		}

		IntraCloudAuthorization authRight = new IntraCloudAuthorization();
		for (ArrowheadSystem provider : request.getProviders()) {
			restrictionMap.clear();
			restrictionMap.put("systemGroup", provider.getSystemGroup());
			restrictionMap.put("systemName", provider.getSystemName());
			ArrowheadSystem retrievedSystem = dm.get(ArrowheadSystem.class, restrictionMap);
			
			restrictionMap.clear();
			restrictionMap.put("consumer", consumer);
			restrictionMap.put("provider", retrievedSystem);
			restrictionMap.put("service", service);
			authRight = dm.get(IntraCloudAuthorization.class, restrictionMap);
			log.info("Authorization rights requested for System: " + request.getConsumer().toString());
			
			if (authRight == null) {
				authorizationState.put(provider, false);
				log.info("This (consumer/provider/service) request is NOT AUTHORIZED.");
			} else {
				authorizationState.put(provider, true);
				log.info("This (consumer/provider/service) request is AUTHORIZED.");
			}
		}
		
		log.info("Sending authorization response with " + authorizationState.size() + " entries.");
		response.setAuthorizationMap(authorizationState);
		return Response.status(Status.OK).entity(response).build();
	}

	/**
	 * Checks whether an external Cloud can use a local Service.
	 * 
	 * @param InterCloudAuthRequest request
	 * @exception DataNotFoundException, BadPayloadException
	 * @return boolean
	 */
	@PUT
	@Path("/intercloud")
	public Response isCloudAuthorized(@Context SecurityContext sc, InterCloudAuthRequest request) {
		log.info("Entered the AuthorizationResource:isCloudAuthorized function");
		
		if (sc.isSecure()) {
			System.out.println("Got a request from a secure channel. Cert: " + sc.getUserPrincipal().getName());
			if(!isClientAuthorized(sc, configuration)){
				//throw new AuthenticationException("This client is not allowed to use this resource.");
				log.info("Unauthorized access! (SSL)");
			}
			else{
				log.info("Identification is successful! (SSL)");
			}
		}
		
		if (!request.isPayloadUsable()) {
			log.info("AuthorizationResource:isCloudAuthorized BadPayloadException");
			throw new BadPayloadException(
				"Bad payload: Missing/incomplete cloud or service in the request payload.");
		}
		
		restrictionMap.put("operator", request.getCloud().getOperator());
		restrictionMap.put("cloudName", request.getCloud().getCloudName());
		ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
        if(cloud == null){
        	log.info("Consumer is not in the database. "
        			+ "(AuthorizationResource:isCloudAuthorized DataNotFoundException)");
       	 	throw new DataNotFoundException(
       	 			"Consumer Cloud is not in the authorization database. " 
       	 			+ request.getCloud().toString());
        }
        
		boolean isAuthorized = false;
		restrictionMap.clear();
		restrictionMap.put("serviceGroup", request.getService().getServiceGroup());
		restrictionMap.put("serviceDefinition", request.getService().getServiceDefinition());
		ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
		if(service == null){
			log.info("Service is not in the database. Returning NOT AUTHORIZED state."
					+ request.getService().toString());
			return Response.status(Status.OK).entity(new InterCloudAuthResponse(isAuthorized)).build();
		}
		
		InterCloudAuthorization authRight = new InterCloudAuthorization();
		restrictionMap.clear();
		restrictionMap.put("cloud", cloud);
		restrictionMap.put("service", service);
		authRight = dm.get(InterCloudAuthorization.class, restrictionMap);
		log.info("Authorization rights requested for Cloud: " + request.getCloud().toString());
		
		if (authRight != null){
			log.info("This (cloud/service) request is AUTHORIZED.");
			isAuthorized = true;
		}
		else{
			log.info("This (cloud/service) request is NOT AUTHORIZED.");
		}
		
		return Response.status(Status.OK).entity(new InterCloudAuthResponse(isAuthorized)).build();
	}
	
	private static boolean isClientAuthorized(SecurityContext sc, Configuration configuration){
		String subjectname = sc.getUserPrincipal().getName();
		String clientCN = SecurityUtils.getCertCNFromSubject(subjectname);
		log.info("The client common name for the request: " + clientCN);
		String serverCN = (String) configuration.getProperty("server_common_name");
		
		String[] serverFields = serverCN.split("\\.", -1);
		String allowedCN = "orchestrator.coresystems";
		if(serverFields.length < 3){
			log.info("SSL error: server CN have less than 3 fields!");
			return false;
		}
		else{
			for(int i = 2; i < serverFields.length; i++){
				allowedCN = allowedCN.concat("." + serverFields[i]);
			}
		}
		
		if(!clientCN.equalsIgnoreCase(allowedCN)){
			log.info("SSL error: common names are not equal!");
			return false;
		}
		
		return true;
	}

	
}
