package eu.arrowhead.core.authorization;

import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
import eu.arrowhead.common.model.messages.IntraCloudAuthRequest;
import eu.arrowhead.common.model.messages.IntraCloudAuthResponse;

/**
 * @author umlaufz, hegeduscs 
 * This class handles the requests targeted at core/authorization/*.
 * Note: PathParam values are NOT case sensitive.
 */
@Path("authorization")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorizationResource {

	DatabaseManager dm = DatabaseManager.getInstance();
	HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
	private static Logger log = Logger.getLogger(AuthorizationResource.class.getName());

	/**
	 * Checks whether the consumer System can use a Service from a list of
	 * provider Systems.
	 * 
	 * @param {IntraCloudAuthRequest} request - POJO with the necessary informations
	 * @exception DataNotFoundException, BadPayloadException
	 * @return IntraCloudAuthResponse - POJO containing a HashMap<ArrowheadSystem, boolean>
	 */
	//TODO token generation if flag set true
	//TODO token generator function 
	@PUT
	@Path("/intracloud")
	public Response isSystemAuthorized(IntraCloudAuthRequest request) {
		log.info("Entered the  isSystemAuthorized function");
		
		if (!request.isPayloadUsable()) {
			log.info("Payload is not usable");
			throw new BadPayloadException("Bad payload: Missing/incomplete consumer, service"
					+ " or providerList in the request payload.");
		}
		log.info("Payload is usable");
		
		IntraCloudAuthResponse response = new IntraCloudAuthResponse();
		restrictionMap.put("systemGroup", request.getConsumer().getSystemGroup());
		restrictionMap.put("systemName", request.getConsumer().getSystemName());
		ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
		if(consumer == null){
			log.info("Consumer was not found");
			throw new DataNotFoundException(
				"Consumer System is not in the authorization database. (SG: " + 
				request.getConsumer().getSystemGroup() + 
				", SN: " + request.getConsumer().getSystemName() + ")");
        }
		log.info("Consumer group: " + consumer.getSystemGroup() + 
				", consumer name: " + consumer.getSystemName());
		
		HashMap<eu.arrowhead.common.model.ArrowheadSystem, Boolean> authorizationState = 
				new HashMap<eu.arrowhead.common.model.ArrowheadSystem, Boolean>();
		log.info("Hashmap created");
		
		restrictionMap.clear();
		restrictionMap.put("serviceGroup", request.getService().getServiceGroup());
		restrictionMap.put("serviceDefinition", request.getService().getServiceDefinition());
		ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
		if (service == null) {
			log.info("Service is not in the database.");
			for (eu.arrowhead.common.model.ArrowheadSystem provider : request.getProviders()) {
				authorizationState.put(provider, false);
			}
			response.setAuthorizationMap(authorizationState);
			return Response.status(Status.OK).entity(response).build();
		}

		IntraCloudAuthorization authRight = new IntraCloudAuthorization();
		for (eu.arrowhead.common.model.ArrowheadSystem provider : request.getProviders()) {
			restrictionMap.clear();
			restrictionMap.put("systemGroup", provider.getSystemGroup());
			restrictionMap.put("systemName", provider.getSystemName());
			ArrowheadSystem retrievedSystem = dm.get(ArrowheadSystem.class, restrictionMap);
			restrictionMap.clear();
			restrictionMap.put("consumer", consumer);
			restrictionMap.put("provider", retrievedSystem);
			restrictionMap.put("service", service);
			authRight = dm.get(IntraCloudAuthorization.class, restrictionMap);
			if (authRight == null) {
				authorizationState.put(provider, false);
			} else {
				authorizationState.put(provider, true);
			}
		}
		log.info("Authorization rights requested for System: " + request.getConsumer().getSystemName());
		
		log.info("Finalizing Map");
		response.setAuthorizationMap(authorizationState);
		for (eu.arrowhead.common.model.ArrowheadSystem ahsys : authorizationState.keySet()) {
			log.info("System group: " + ahsys.getSystemGroup() + ", system name: " + 
					ahsys.getSystemName() + ", value" + authorizationState.get(ahsys).toString());
		}
		
		log.info("Sending response");
		return Response.status(Status.OK).entity(response).build();
	}

	/**
	 * Checks whether an external Cloud can use a local Service.
	 * 
	 * @param {InterCloudAuthRequest} request - POJO with the necessary informations
	 * @exception DataNotFoundException, BadPayloadException
	 * @return boolean
	 */
	@PUT
	@Path("/intercloud")
	@Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
	public Response isCloudAuthorized(InterCloudAuthRequest request) {
		log.info("Entered the isCloudAuthorized function");
		
		if (!request.isPayloadUsable()) {
			log.info("Payload is not usable");
			throw new BadPayloadException(
				"Bad payload: Missing/incomplete cloud or service in the request payload.");
		}
		log.info("Payload is usable");
		
		restrictionMap.put("operator", request.getCloud().getOperator());
		restrictionMap.put("cloudName", request.getCloud().getCloudName());
		ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
        if(cloud == null){
        	log.info("Consumer was not found");
       	 	throw new DataNotFoundException(
       	 			"Consumer Cloud is not in the authorization database. (OP: " 
       	 			+ request.getCloud().getOperator() + 
       	 			", CN: " + request.getCloud().getCloudName() + ")");
        }
        log.info("Cloud operator: " + cloud.getOperator() + ", cloud name: " + cloud.getCloudName());
        
		boolean isAuthorized = false;
		
		restrictionMap.clear();
		restrictionMap.put("serviceGroup", request.getService().getServiceGroup());
		restrictionMap.put("serviceDefinition", request.getService().getServiceDefinition());
		ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
		if(service == null){
			return Response.status(Status.OK).entity(isAuthorized).build();
		}
		
		InterCloudAuthorization authRight = new InterCloudAuthorization();
		restrictionMap.clear();
		restrictionMap.put("cloud", cloud);
		restrictionMap.put("service", service);
		authRight = dm.get(InterCloudAuthorization.class, restrictionMap);
		log.info("Authorization rights requested for Cloud: " + request.getCloud().getCloudName());
		
		if (authRight != null){
			isAuthorized = true;
		}

		return Response.status(Status.OK).entity(isAuthorized).build();
	}

	
}
