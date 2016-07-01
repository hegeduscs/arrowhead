package eu.arrowhead.core.api;

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
import javax.ws.rs.core.UriInfo;
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

@Path("authorization")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthenticationApi {

	DatabaseManager dm = DatabaseManager.getInstance();
	HashMap<String, Object> restrictionMap = new HashMap<String, Object>();
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "Got it";
	}
	
	@GET
	@Path("/intracloud")
	public List<IntraCloudAuthorization> getIntraCloudAuthRights(){
		List<IntraCloudAuthorization> authRights = dm.getAll(IntraCloudAuthorization.class, restrictionMap);
		
		return authRights;
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
	//TODO token generation if flag set true
	//TODO token generator function 
	@PUT
	@Path("/systemgroup/{systemGroup}/system/{systemName}")
	public Response isSystemAuthorized(@PathParam("systemGroup") String systemGroup,
			@PathParam("systemName") String systemName, IntraCloudAuthRequest request) {
		
		if (!request.isPayloadUsable()) {
			throw new BadPayloadException("Bad payload: Missing arrowheadService, authenticationInfo"
					+ " or providerList from the request payload.");
		}
		
		IntraCloudAuthResponse response = new IntraCloudAuthResponse();
		restrictionMap.put("systemGroup", systemGroup);
		restrictionMap.put("systemName", systemName);
		ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
		if(consumer == null){
			throw new DataNotFoundException(
				"Consumer System is not in the authorization database. (SG: " + systemGroup + 
				", SN: " + systemName + ")");
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
		
		restrictionMap.put("systemGroup", systemGroup);
		restrictionMap.put("systemName", systemName);
		ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
		if (consumer == null) {
			consumer = new ArrowheadSystem();
			consumer.setSystemGroup(systemGroup);
			consumer.setSystemName(systemName);
			consumer.setAddress(entry.getAddress());
			consumer.setPort(entry.getPort());
			consumer.setAuthenticationInfo(entry.getAuthenticationInfo());
			consumer = dm.save(consumer);
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
				dm.merge(authRight); //TODO needs testing if this works with the new dm
			}
		}

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
	@Path("/systemgroup/{systemGroup}/systemname/{systemName}/services")
	public Set<ArrowheadService> getSystemServices(@PathParam("systemGroup") String systemGroup,
			@PathParam("systemName") String systemName) throws DataNotFoundException {
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
	 * Checks whether an external Cloud can use a local Service.
	 * 
	 * @param {String} operatorName
	 * @param {String} cloudName
	 * @param {InterCloudAuthRequest} request - POJO with the necessary informations
	 * @exception DataNotFoundException, BadPayloadException
	 * @return boolean
	 */
	@PUT
	@Path("/operator/{operator}/cloud/{cloudName}")
	@Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
	public Response isCloudAuthorized(@PathParam("operator") String operator,
			@PathParam("cloudName") String cloudName, InterCloudAuthRequest request) throws DataNotFoundException {
		
		if (!request.isPayloadUsable()) {
			throw new BadPayloadException(
				"Bad payload: Missing arrowheadService or authenticationInfo from the request payload.");
		}
		
		restrictionMap.put("operator", operator);
		restrictionMap.put("cloudName", cloudName);
		ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
        if(cloud == null){
       	 	throw new DataNotFoundException(
       	 			"Consumer Cloud is not in the authorization database. (OP: " + operator + 
				", CN: " + cloudName + ")");
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
	 * @param {String} operatorName
	 * @param {String} cloudName
	 * @param {InterCloudAuthEntry} entry - POJO with the necessary informations
	 * @param {UriInfo} uriInfo - JAX-RS object containing URI information
	 * @exception DuplicateEntryException, BadPayloadException
	 * @return JAX-RS Response with status code 201 and ArrowheadCloud entity
	 */
	@POST
	@Path("/operator/{operator}/cloud/{cloudName}")
	public Response addCloudToAuthorized(@PathParam("operator") String operator,
			@PathParam("cloudName") String cloudName, InterCloudAuthEntry entry, 
			@Context UriInfo uriInfo) {
		if (!entry.isPayloadUsable()) {
			throw new BadPayloadException(
				"Bad payload: Missing serviceList or authenticationInfo from the entry payload.");
		}
		
		restrictionMap.put("operator", operator);
		restrictionMap.put("cloudName", cloudName);
		ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);;
		if(cloud == null){
			cloud = new ArrowheadCloud();
			cloud.setOperator(operator);
			cloud.setCloudName(cloudName);
			cloud.setAuthenticationInfo(entry.getAuthenticationInfo());
			cloud = dm.save(cloud);
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
			dm.merge(authRight); //TODO needs testing if this works with the new dm
		}

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
	@Path("/operator/{operator}/cloud/{cloudName}/services")
	public Set<ArrowheadService> getCloudServices(@PathParam("operator") String operator,
			@PathParam("cloudName") String cloudName) throws DataNotFoundException {
		
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
