package eu.arrowhead.core.api;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import eu.arrowhead.common.configuration.DatabaseManager;
import eu.arrowhead.common.database.InterCloudAuthorization;
import eu.arrowhead.common.database.IntraCloudAuthorization;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.InterCloudAuthEntry;
import eu.arrowhead.common.model.messages.InterCloudAuthRequest;
import eu.arrowhead.common.model.messages.IntraCloudAuthEntry;
import eu.arrowhead.common.model.messages.IntraCloudAuthRequest;
import eu.arrowhead.common.model.messages.IntraCloudAuthResponse;

@Path("auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorizationApi {

	DatabaseManager dm = DatabaseManager.getInstance();
	HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "Got it";
	}
	
	/**
	 * Returns all the IntraCloud authorization rights from the database.
	 * 
	 * @return List<IntraCloudAuthorization>
	 * @throws DataNotFoundException
	 */
	@GET
	@Path("/intracloud")
	public List<IntraCloudAuthorization> getIntraCloudAuthRights(){
		List<IntraCloudAuthorization> authRights = new ArrayList<IntraCloudAuthorization>();
		authRights = dm.getAll(IntraCloudAuthorization.class, restrictionMap);
		if(authRights.isEmpty()){
			throw new DataNotFoundException("IntraCloud authorization rights not found in the database.");
		}
		
		return authRights;
	}

	/**
	 * Checks whether the consumer System can use a Service from a list of
	 * provider Systems.
	 * 
	 * @param {IntraCloudAuthRequest} request - POJO with the necessary informations
	 * @return IntraCloudAuthResponse - POJO containing a HashMap<ArrowheadSystem, boolean>
	 * @throws DataNotFoundException, BadPayloadException
	 */
	//TODO token generation if flag set true
	//TODO token generator function 
	@PUT
	@Path("/intracloud")
	public Response isSystemAuthorized(IntraCloudAuthRequest request) {
		
		if (!request.isPayloadUsable()) {
			throw new BadPayloadException("Bad payload: Missing/incomplete consumer, service"
					+ " or providerList in the request payload.");
		}
		
		IntraCloudAuthResponse response = new IntraCloudAuthResponse();
		restrictionMap.put("systemGroup", request.getConsumer().getSystemGroup());
		restrictionMap.put("systemName", request.getConsumer().getSystemName());
		ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
		if(consumer == null){
			throw new DataNotFoundException(
				"Consumer System is not in the database. (SG: " + 
				request.getConsumer().getSystemGroup() + ", SN: " + 
				request.getConsumer().getSystemName() + ")");
        }
		
		HashMap<ArrowheadSystem, Boolean> authorizationState = new HashMap<ArrowheadSystem, Boolean>();
		
		restrictionMap.clear();
		restrictionMap.put("serviceGroup", request.getService().getServiceGroup());
		restrictionMap.put("serviceDefinition", request.getService().getServiceDefinition());
		ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
		if (service == null) {
			for (ArrowheadSystem provider : request.getProviders()) {
				authorizationState.put(provider, false);
			}
			response.setAuthorizationMap(authorizationState);
			return Response.status(Status.OK).entity(response).build();
		}

		IntraCloudAuthorization authRight = null;
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
			if (authRight == null) {
				authorizationState.put(provider, false);
			} else {
				authorizationState.put(provider, true);
				authRight = null;
			}
		}

		response.setAuthorizationMap(authorizationState);

		return Response.status(Status.OK).entity(response).build();
	}

	/**
	 * Creates a relation between local Systems, defining the consumable
	 * services between Systems. (Not bidirectional.) OneToMany relation between
	 * consumer and providers, OneToMany relation between consumer and services.
	 * 
	 * @param {IntraCloudAuthEntry} entry - POJO with the necessary informations
	 * @return JAX-RS Response with status code 201 and ArrowheadSystem entity
	 *         (the consumer system)
	 * @throws DuplicateEntryException, BadPayloadException
	 */
	@POST
	@Path("/intracloud")
	public Response addSystemToAuthorized(IntraCloudAuthEntry entry) {
		if (!entry.isPayloadUsable()) {
			throw new BadPayloadException("Bad payload: Missing/incomplete consumer, "
					+ "serviceList or providerList in the entry payload.");
		}
		
		restrictionMap.put("systemGroup", entry.getConsumer().getSystemGroup());
		restrictionMap.put("systemName", entry.getConsumer().getSystemName());
		ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
		if (consumer == null) {
			consumer = dm.save(entry.getConsumer());
		}

		ArrowheadSystem retrievedSystem = null;
		ArrowheadService retrievedService = null;
		IntraCloudAuthorization authRight = new IntraCloudAuthorization();

		for (ArrowheadSystem providerSystem : entry.getProviderList()) {
			restrictionMap.clear();
			restrictionMap.put("systemGroup", providerSystem.getSystemGroup());
			restrictionMap.put("systemName", providerSystem.getSystemName());
			retrievedSystem = dm.get(ArrowheadSystem.class, restrictionMap);
			if (retrievedSystem == null) {
				retrievedSystem = dm.save(providerSystem);
			}
			for (ArrowheadService service : entry.getServiceList()) {
				restrictionMap.clear();
				restrictionMap.put("serviceGroup", service.getServiceGroup());
				restrictionMap.put("serviceDefinition", service.getServiceDefinition());
				retrievedService = dm.get(ArrowheadService.class, restrictionMap);
				if (retrievedService == null) {
					retrievedService = dm.save(service);
				}
				authRight.setConsumer(consumer);
				authRight.setProvider(retrievedSystem);
				authRight.setService(retrievedService);
				dm.merge(authRight);
			}
		}

		return Response.status(Status.CREATED).entity(consumer).build();
	}

	/**
	 * Deletes all the authorization right relations where the given System is the consumer.
	 * 
	 * @param {String} systemGroup
	 * @param {String} systemName
	 * @return JAX-RS Response with status code 200 (if delete is succesfull)
	 * or 204 (if nothing happens).
	 */
	@DELETE
	@Path("/intracloud/systemgroup/{systemGroup}/system/{systemName}")
	public Response deleteSystemRelations(@PathParam("systemGroup") String systemGroup,
			@PathParam("systemName") String systemName) {
		
		restrictionMap.put("systemGroup", systemGroup);
		restrictionMap.put("systemName", systemName);
		ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
		if(consumer == null){
			return Response.noContent().build();
		}
		
		List<IntraCloudAuthorization> authRightsList = new ArrayList<IntraCloudAuthorization>();
		restrictionMap.clear();
		restrictionMap.put("consumer", consumer);
		authRightsList = dm.getAll(IntraCloudAuthorization.class, restrictionMap);
		for (IntraCloudAuthorization authRight : authRightsList) {
			dm.delete(authRight);
		}

		return Response.ok().build();
	}
	
	/**
	 * Returns the list of consumable Services of a System.
	 * 
	 * @param {String} systemGroup
	 * @param {String} systemName
	 * @return List<ArrowheadService>
	 * @throws DataNotFoundException
	 */
	@GET
	@Path("/intracloud/systemgroup/{systemGroup}/systemname/{systemName}/services")
	public Set<ArrowheadService> getSystemServices(@PathParam("systemGroup") String systemGroup,
			@PathParam("systemName") String systemName) {
		restrictionMap.put("systemGroup", systemGroup);
		restrictionMap.put("systemName", systemName);
		ArrowheadSystem system = dm.get(ArrowheadSystem.class, restrictionMap);
		if(system == null){
       	 	throw new DataNotFoundException(
       	 		"This System is not in the authorization database. (SG: " + systemGroup + 
				", SN: " + systemName + ")");
        }
		
		List<IntraCloudAuthorization> authRightsList = new ArrayList<IntraCloudAuthorization>();
		restrictionMap.clear();
		restrictionMap.put("consumer", system);
		authRightsList = dm.getAll(IntraCloudAuthorization.class, restrictionMap);
		Set<ArrowheadService> serviceList = new HashSet<ArrowheadService>();
		for(IntraCloudAuthorization authRight : authRightsList){
			serviceList.add(authRight.getService());
		}

		return serviceList;
	}
	
	/**
	 * Returns all the InterCloud authorization rights from the database.
	 * 
	 * @return List<InterCloudAuthorization>
	 * @throws DataNotFoundException
	 */
	@GET
	@Path("/intercloud")
	public List<InterCloudAuthorization> getInterCloudAuthRights(){
		List<InterCloudAuthorization> authRights = new ArrayList<InterCloudAuthorization>();
		authRights = dm.getAll(InterCloudAuthorization.class, restrictionMap);
		if(authRights.isEmpty()){
			throw new DataNotFoundException("InterCloud authorization rights not found in the database.");
		}
		
		return authRights;
	}

	/**
	 * Checks whether an external Cloud can use a local Service.
	 * 
	 * @param {InterCloudAuthRequest} request - POJO with the necessary informations
	 * @return boolean
	 * @throws DataNotFoundException, BadPayloadException
	 */
	@PUT
	@Path("/intercloud")
	@Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
	public Response isCloudAuthorized(InterCloudAuthRequest request) {
		
		if (!request.isPayloadUsable()) {
			throw new BadPayloadException(
				"Bad payload: Missing/incomplete cloud or service in the request payload.");
		}
		
		restrictionMap.put("operator", request.getCloud().getOperator());
		restrictionMap.put("cloudName", request.getCloud().getCloudName());
		ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
        if(cloud == null){
       	 	throw new DataNotFoundException(
       	 			"Consumer Cloud is not in the authorization database. (OP: " 
       	 			+ request.getCloud().getOperator() + ", CN: " 
       	 			+ request.getCloud().getCloudName() + ")");
        }
        
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
		
		if (authRight != null){
			isAuthorized = true;
		}

		return Response.status(Status.OK).entity(isAuthorized).build();
	}

	/**
	 * Adds a new Cloud and its consumable Services to the database.
	 * 
	 * @param {InterCloudAuthEntry} entry - POJO with the necessary informations
	 * @return JAX-RS Response with status code 201 and ArrowheadCloud entity
	 * @throws DuplicateEntryException, BadPayloadException
	 */
	@POST
	@Path("/intercloud")
	public Response addCloudToAuthorized(InterCloudAuthEntry entry) {
		if (!entry.isPayloadUsable()) {
			throw new BadPayloadException(
				"Bad payload: Missing/incomplete cloud or serviceList in the entry payload.");
		}
		
		restrictionMap.put("operator", entry.getCloud().getOperator());
		restrictionMap.put("cloudName", entry.getCloud().getCloudName());
		ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);;
		if(cloud == null){
			cloud = dm.save(entry.getCloud());
		}
		
		ArrowheadService retrievedService = null;
		InterCloudAuthorization authRight = new InterCloudAuthorization();

		for (ArrowheadService service : entry.getServiceList()) {
			restrictionMap.clear();
			restrictionMap.put("serviceGroup", service.getServiceGroup());
			restrictionMap.put("serviceDefinition", service.getServiceDefinition());
			retrievedService = dm.get(ArrowheadService.class, restrictionMap);
			if (retrievedService == null) {
				retrievedService = dm.save(service);
			}
			authRight.setCloud(cloud);
			authRight.setService(retrievedService);
			dm.merge(authRight);
		}

		return Response.status(Status.CREATED).entity(cloud).build();
	}

	/**
	 * Deletes the authorization rights of the Cloud.
	 * 
	 * @param {String} operatorName
	 * @param {String} cloudName
	 * @return JAX-RS Response with status code 200 (if delete is succesfull)
	 * or 204 (if nothing happens).
	 */
	@DELETE
	@Path("/operator/{operator}/cloud/{cloudName}")
	public Response deleteCloudRelations(@PathParam("operator") String operator,
			@PathParam("cloudName") String cloudName) {
		
		restrictionMap.put("operator", operator);
		restrictionMap.put("cloudName", cloudName);
		ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);;
		if(cloud == null){
			return Response.noContent().build();
		}
		
		List<InterCloudAuthorization> authRightsList = new ArrayList<InterCloudAuthorization>();
		restrictionMap.clear();
		restrictionMap.put("cloud", cloud);
		authRightsList = dm.getAll(InterCloudAuthorization.class, restrictionMap);
		for (InterCloudAuthorization authRight : authRightsList) {
			dm.delete(authRight);
		}

		return Response.ok().build();
	}

	/**
	 * Returns the list of consumable Services of a Cloud.
	 * 
	 * @param {String} operatorName
	 * @param {String} cloudName
	 * @return List<ArrowheadService>
	 * @throws DataNotFoundException
	 */
	@GET
	@Path("/operator/{operator}/cloud/{cloudName}/services")
	public Set<ArrowheadService> getCloudServices(@PathParam("operator") String operator,
			@PathParam("cloudName") String cloudName) {
		
		restrictionMap.put("operator", operator);
		restrictionMap.put("cloudName", cloudName);
		ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);;
		if(cloud == null){
       	 	throw new DataNotFoundException(
       	 		"Consumer Cloud is not in the authorization database. (OP: " + operator + 
				", CN: " + cloudName + ")");
        }
		
		List<InterCloudAuthorization> authRightsList = new ArrayList<InterCloudAuthorization>();
		restrictionMap.clear();
		restrictionMap.put("cloud", cloud);
		authRightsList = dm.getAll(InterCloudAuthorization.class, restrictionMap);
		Set<ArrowheadService> serviceList = new HashSet<ArrowheadService>();
		for(InterCloudAuthorization authRight : authRightsList){
			serviceList.add(authRight.getService());
		}

		return serviceList;
	}

}
