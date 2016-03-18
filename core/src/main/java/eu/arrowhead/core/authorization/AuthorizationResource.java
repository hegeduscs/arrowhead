package eu.arrowhead.core.authorization;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import javax.ws.rs.core.UriInfo;

import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.messages.InterCloudAuthRequest;
import eu.arrowhead.common.model.messages.IntraCloudAuthRequest;
import eu.arrowhead.common.model.messages.IntraCloudAuthResponse;

import eu.arrowhead.core.authorization.database.ArrowheadCloud;
import eu.arrowhead.core.authorization.database.ArrowheadService;
import eu.arrowhead.core.authorization.database.ArrowheadSystem;
import eu.arrowhead.core.authorization.database.Clouds_Services;
import eu.arrowhead.core.authorization.database.Systems_Services;

/**
 * @author umlaufz, hegeduscs 
 * This class handles the requests targeted at core/authorization/*.
 * Note: PathParam values are NOT case sensitive.
 */
@Path("authorization")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorizationResource {

	DatabaseManager databaseManager = new DatabaseManager();
	private static Logger log = Logger.getLogger(AuthorizationResource.class.getName());
	
	/**
	 * Returns a list of ArrowheadSystems with the same systemGroup from the database.
	 * 
	 * @param {String} systemGroup
	 * @return List<ArrowheadSystem>
	 */
	@GET
	@Path("/systemgroup/{systemGroup}")
	public List<ArrowheadSystem> getSystems(@PathParam("systemGroup") String systemGroup) {
		List<ArrowheadSystem> systemList = new ArrayList<ArrowheadSystem>();
		systemList = databaseManager.getSystems(systemGroup);

		return systemList;
	}

	/**
	 * Returns an ArrowheadSystem from the database specified by the systemGroup
	 * and systemName.
	 * 
	 * @param {String} systemGroup
	 * @param {String} systemName
	 * @exception DataNotFoundException
	 * @return JAX-RS Response with status code 200 and ArrowheadSystem entity
	 */
	@GET
	@Path("/systemgroup/{systemGroup}/system/{systemName}")
	public Response getSystem(@PathParam("systemGroup") String systemGroup,
			@PathParam("systemName") String systemName) {
		ArrowheadSystem arrowheadSystem = databaseManager.getSystemByName(systemGroup, systemName);
		if(arrowheadSystem == null)
			throw new DataNotFoundException("System not found in the database.");
		
		log.info("System returned: " + systemName);
		return Response.ok(arrowheadSystem).build();
	}

	/**
	 * Checks whether the consumer System can use a Service from a list of
	 * provider Systems.
	 * 
	 * @param {String} systemGroup
	 * @param {String} systemName
	 * @param {IntraCloudAuthRequest} request - POJO with the necessary informations
	 * @exception DataNotFoundException, BadPayloadException
	 * @return IntraCloudAuthResponse - POJO containing a HashMap<ArrowheadSystem, boolean>
	 */
	@PUT
	@Path("/systemgroup/{systemGroup}/system/{systemName}")
	public Response isSystemAuthorized(@PathParam("systemGroup") String systemGroup,
			@PathParam("systemName") String systemName, IntraCloudAuthRequest request) {
		log.info("Entered the isSystemAuthorized function");
		
		if (!request.isPayloadUsable()) {
			log.info("Payload is not usable");
			throw new BadPayloadException("Bad payload: Missing arrowheadService, authenticationInfo"
					+ " or providerList from the request payload.");
		}
		log.info("Payload is usable");
		
		IntraCloudAuthResponse response = new IntraCloudAuthResponse();
		ArrowheadSystem consumer = databaseManager.getSystemByName(systemGroup, systemName);
		if(consumer == null){
			log.info("Consumer was not found");
			throw new DataNotFoundException(
				"Consumer System is not in the authorization database. (SG: " + systemGroup + 
				", SN: " + systemName + ")");
        }
		log.info("Consumer group: " + consumer.getSystemGroup() + 
				", consumer name: " + consumer.getSystemName());
		
		HashMap<eu.arrowhead.common.model.ArrowheadSystem, Boolean> authorizationState = 
				new HashMap<eu.arrowhead.common.model.ArrowheadSystem, Boolean>();
		log.info("Hashmap created");
		
		ArrowheadService service = null;
		service = databaseManager.getServiceByName(request.getService().getServiceGroup(),
				request.getService().getServiceDefinition());
		if (service == null) {
			log.info("Service is not in the database.");
			for (eu.arrowhead.common.model.ArrowheadSystem provider : request.getProviders()) {
				authorizationState.put(provider, false);
			}
			response.setAuthorizationMap(authorizationState);
			return Response.status(Status.OK).entity(response).build();
		}

		Systems_Services ss = new Systems_Services();
		for (eu.arrowhead.common.model.ArrowheadSystem provider : request.getProviders()) {
			ArrowheadSystem retrievedSystem = databaseManager.getSystemByName(provider.getSystemGroup(),
					provider.getSystemName());
			ss = databaseManager.getSS(consumer, retrievedSystem, service);
			if (ss == null) {
				authorizationState.put(provider, false);
			} else {
				authorizationState.put(provider, true);
			}
		}
		log.info("Authorization rights requested for System: " + systemName);
		
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
	 * Creates a relation between local Systems, defining the consumable
	 * services between Systems. (Not bidirectional.) OneToMany relation between
	 * consumer and providers, OneToMany relation between consumer and services.
	 * 
	 * @param {String} systemGroup
	 * @param {String} systemName
	 * @param {IntraCloudAuthEntry} entry - POJO with the necessary informations
	 * @exception DuplicateEntryException, BadPayloadException
	 * @return JAX-RS Response with status code 201 and ArrowheadSystem entity
	 *         (the consumer system)
	 */
	@POST
	@Path("/systemgroup/{systemGroup}/system/{systemName}")
	public Response addSystemToAuthorized(@PathParam("systemGroup") String systemGroup,
			@PathParam("systemName") String systemName, IntraCloudAuthEntry entry,
			@Context UriInfo uriInfo) {
		if (!entry.isPayloadUsable()) {
			throw new BadPayloadException("Bad payload: Missing serviceList, providerList, "
					+ "IP address, port or authenticationInfo from the entry payload.");
		}
		
		ArrowheadSystem consumer = databaseManager.getSystemByName(systemGroup, systemName);
		if (consumer == null) {
			consumer = new ArrowheadSystem();
			consumer.setSystemGroup(systemGroup);
			consumer.setSystemName(systemName);
			consumer.setIPAddress(entry.getIPAddress());
			consumer.setPort(entry.getPort());
			consumer.setAuthenticationInfo(entry.getAuthenticationInfo());
			consumer = databaseManager.save(consumer);
		}

		ArrowheadSystem retrievedSystem = null;
		ArrowheadService retrievedService = null;
		Systems_Services ss = new Systems_Services();

		for (ArrowheadSystem providerSystem : entry.getProviderList()) {
			retrievedSystem = databaseManager.getSystemByName(providerSystem.getSystemGroup(),
					providerSystem.getSystemName());
			if (retrievedSystem == null) {
				retrievedSystem = databaseManager.save(providerSystem);
			}
			for (ArrowheadService service : entry.getServiceList()) {
				retrievedService = databaseManager.getServiceByName(service.getServiceGroup(),
						service.getServiceDefinition());
				if (retrievedService == null) {
					retrievedService = databaseManager.save(service);
				}
				ss.setConsumer(consumer);
				ss.setProvider(retrievedSystem);
				ss.setService(retrievedService);
				databaseManager.saveRelation(ss);
			}
		}

		log.info("System added to authorization database: " + systemName);
		URI uri = uriInfo.getAbsolutePathBuilder().build();
		return Response.created(uri).entity(consumer).build();
	}

	/**
	 * Deletes all the authorization right relations where the given System is the consumer.
	 * 
	 * @param {String} systemGroup
	 * @param {String} systemName
	 * @return JAX-RS Response with status code 204
	 */
	@DELETE
	@Path("/systemgroup/{systemGroup}/system/{systemName}")
	public Response deleteSystemRelations(@PathParam("systemGroup") String systemGroup,
			@PathParam("systemName") String systemName) {
		ArrowheadSystem consumer = databaseManager.getSystemByName(systemGroup, systemName);
		if(consumer == null){
			return Response.noContent().build();
		}
		
		List<Systems_Services> ssList = new ArrayList<Systems_Services>();
		ssList = databaseManager.getSystemRelations(consumer);
		for (Systems_Services ss : ssList) {
			databaseManager.delete(ss);
		}

		log.info("Consumer System relations deleted from authorization database. "
				+ "System name: " + systemName);
		return Response.noContent().build();
	}
	
	/**
	 * Returns the list of consumable Services of a System.
	 * 
	 * @param {String} systemGroup
	 * @param {String} systemName
	 * @exception DataNotFoundException
	 * @return List<ArrowheadService>
	 */
	@GET
	@Path("/operator/{operatorName}/cloud/{cloudName}/services")
	public Set<ArrowheadService> getSystemServices(@PathParam("systemGroup") String systemGroup,
			@PathParam("systemName") String systemName) {
		ArrowheadSystem system = databaseManager.getSystemByName(systemGroup, systemName);
		if(system == null){
       	 	throw new DataNotFoundException(
       	 		"This System is not in the authorization database. (SG: " + systemGroup + 
				", SN: " + systemName + ")");
        }
		
		List<Systems_Services> ssList = new ArrayList<Systems_Services>();
		ssList = databaseManager.getSystemRelations(system);
		Set<ArrowheadService> serviceList = new HashSet<ArrowheadService>();
		for(Systems_Services ss : ssList){
			serviceList.add(ss.getService());
		}

		return serviceList;
	}

	/**
	 * Returns a list of ArrowheadClouds with the same operator from the database.
	 * 
	 * @param {String} operatorName
	 * @return List<ArrowheadCloud>
	 */
	@GET
	@Path("/operator/{operatorName}")
	public List<ArrowheadCloud> getClouds(@PathParam("operatorName") String operatorName) {
		List<ArrowheadCloud> cloudList = new ArrayList<ArrowheadCloud>();
		cloudList = databaseManager.getClouds(operatorName);

		return cloudList;
	}

	/**
	 * Returns an ArrowheadCloud from the database specified by the operatorName
	 * and cloudName.
	 * 
	 * @param {String} operatorName
	 * @param {String} cloudName
	 * @exception DataNotFoundException
	 * @return JAX-RS Response with status code 200 and ArrowheadCloud entity
	 */
	@GET
	@Path("/operator/{operatorName}/cloud/{cloudName}")
	public Response getCloud(@PathParam("operatorName") String operatorName, 
			@PathParam("cloudName") String cloudName) {
		ArrowheadCloud cloud = databaseManager.getCloudByName(operatorName, cloudName);
		return Response.ok(cloud).build();
	}

	/**
	 * Checks whether an external Cloud can use a local Service.
	 * 
	 * @param {String} operatorName
	 * @param {String} cloudName
	 * @param {InterCloudAuthRequest} request - POJO with the necessary informations
	 * @exception DataNotFoundException, BadPayloadException
	 * @return boolean
	 */
	@PUT
	@Path("/operator/{operatorName}/cloud/{cloudName}")
	@Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
	public Response isCloudAuthorized(@PathParam("operatorName") String operatorName,
			@PathParam("cloudName") String cloudName, InterCloudAuthRequest request) {
		log.info("Entered the isCloudAuthorized function");
		
		if (!request.isPayloadUsable()) {
			log.info("Payload is not usable");
			throw new BadPayloadException(
				"Bad payload: Missing arrowheadService or authenticationInfo from the request payload.");
		}
		log.info("Payload is usable");
		
		ArrowheadCloud cloud = databaseManager.getCloudByName(operatorName, cloudName);
        if(cloud == null){
        	log.info("Consumer was not found");
       	 	throw new DataNotFoundException(
       	 			"Consumer Cloud is not in the authorization database. (OP: " + operatorName + 
				", CN: " + cloudName + ")");
        }
        log.info("Cloud operator: " + cloud.getOperator() + ", cloud name: " + cloud.getCloudName());
        
		ArrowheadService service = null;
		boolean isAuthorized = false;
		service = databaseManager.getServiceByName(request.getService().getServiceGroup(),
				request.getService().getServiceDefinition());
		if(service == null){
			return Response.status(Status.OK).entity(isAuthorized).build();
		}
		
		Clouds_Services cs = new Clouds_Services();
		cs = databaseManager.getCS(cloud, service);
		log.info("Authorization rights requested for Cloud: " + cloudName);
		
		if (cs != null){
			isAuthorized = true;
		}

		return Response.status(Status.OK).entity(isAuthorized).build();
	}

	/**
	 * Adds a new Cloud and its consumable Services to the database.
	 * 
	 * @param {String} operatorName
	 * @param {String} cloudName
	 * @param {InterCloudAuthEntry} entry - POJO with the necessary informations
	 * @param {UriInfo} uriInfo - JAX-RS object containing URI information
	 * @exception DuplicateEntryException, BadPayloadException
	 * @return JAX-RS Response with status code 201 and ArrowheadCloud entity
	 */
	@POST
	@Path("/operator/{operatorName}/cloud/{cloudName}")
	public Response addCloudToAuthorized(@PathParam("operatorName") String operatorName,
			@PathParam("cloudName") String cloudName, InterCloudAuthEntry entry, 
			@Context UriInfo uriInfo) {
		if (!entry.isPayloadUsable()) {
			throw new BadPayloadException(
				"Bad payload: Missing serviceList or authenticationInfo from the entry payload.");
		}
		
		ArrowheadCloud cloud = databaseManager.getCloudByName(operatorName, cloudName);
		if(cloud == null){
			cloud = new ArrowheadCloud();
			cloud.setOperator(operatorName);
			cloud.setCloudName(cloudName);
			cloud.setAuthenticationInfo(entry.getAuthenticationInfo());
			cloud = databaseManager.save(cloud);
		}
		
		ArrowheadService retrievedService = null;
		Clouds_Services cs = new Clouds_Services();

		for (ArrowheadService service : entry.getServiceList()) {
			retrievedService = databaseManager.getServiceByName(service.getServiceGroup(),
					service.getServiceDefinition());
			if (retrievedService == null) {
				retrievedService = databaseManager.save(service);
			}
			cs.setCloud(cloud);
			cs.setService(retrievedService);
			databaseManager.saveRelation(cs);
		}

		log.info("Cloud added to authorization database: " + cloudName);
		URI uri = uriInfo.getAbsolutePathBuilder().build();
		return Response.created(uri).entity(cloud).build();
	}

	/**
	 * Deletes the authorization rights of the Cloud.
	 * 
	 * @param {String} operatorName
	 * @param {String} cloudName
	 * @return JAX-RS Response with status code 204
	 */
	@DELETE
	@Path("/operator/{operatorName}/cloud/{cloudName}")
	public Response deleteCloudRelations(@PathParam("operatorName") String operatorName,
			@PathParam("cloudName") String cloudName) {
		ArrowheadCloud cloud = databaseManager.getCloudByName(operatorName, cloudName);
		if(cloud == null){
			return Response.noContent().build();
		}
		
		List<Clouds_Services> csList = new ArrayList<Clouds_Services>();
		csList = databaseManager.getCloudRelations(cloud);
		for (Clouds_Services ss : csList) {
			databaseManager.delete(ss);
		}

		log.info("Cloud relations deleted from authorization database. "
				+ "Cloud name: " + cloudName);
		return Response.noContent().build();
	}

	/**
	 * Returns the list of consumable Services of a Cloud.
	 * 
	 * @param {String} operatorName
	 * @param {String} cloudName
	 * @exception DataNotFoundException
	 * @return List<ArrowheadService>
	 */
	@GET
	@Path("/operator/{operatorName}/cloud/{cloudName}/services")
	public Set<ArrowheadService> getCloudServices(@PathParam("operatorName") String operatorName,
			@PathParam("cloudName") String cloudName) {
		ArrowheadCloud cloud = databaseManager.getCloudByName(operatorName, cloudName);
		if(cloud == null){
       	 	throw new DataNotFoundException(
       	 		"Consumer Cloud is not in the authorization database. (OP: " + operatorName + 
				", CN: " + cloudName + ")");
        }
		
		List<Clouds_Services> csList = new ArrayList<Clouds_Services>();
		csList = databaseManager.getCloudRelations(cloud);
		Set<ArrowheadService> serviceList = new HashSet<ArrowheadService>();
		for(Clouds_Services cs : csList){
			serviceList.add(cs.getService());
		}

		return serviceList;
	}

	
}
