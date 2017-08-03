package eu.arrowhead.core.api;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.InterCloudAuthorization;
import eu.arrowhead.common.database.IntraCloudAuthorization;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.InterCloudAuthEntry;
import eu.arrowhead.common.model.messages.InterCloudAuthRequest;
import eu.arrowhead.common.model.messages.InterCloudAuthResponse;
import eu.arrowhead.common.model.messages.IntraCloudAuthEntry;
import eu.arrowhead.common.model.messages.IntraCloudAuthRequest;
import eu.arrowhead.common.model.messages.IntraCloudAuthResponse;
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
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

@Path("auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorizationApi {

  private static Logger log = Logger.getLogger(AuthorizationApi.class.getName());
  private DatabaseManager dm = DatabaseManager.getInstance();
  private HashMap<String, Object> restrictionMap = new HashMap<>();

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "Got it";
  }

  /**
   * Returns all the IntraCloud authorization rights from the database.
   *
   * @return List<IntraCloudAuthorization>
   */
  @GET
  @Path("/intracloud")
  public List<IntraCloudAuthorization> getIntraCloudAuthRights() {

    List<IntraCloudAuthorization> authRights = dm.getAll(IntraCloudAuthorization.class, restrictionMap);
    if (authRights.isEmpty()) {
      log.info("AuthorizationApi:getIntraCloudAuthRights throws DataNotFoundException.");
      throw new DataNotFoundException("IntraCloud authorization rights " + "were not found in the database.");
    }

    log.info("getIntraCloudAuthRights successfully returns " + authRights.size() + " entries.");
    return authRights;
  }

  /**
   * Checks whether the consumer System can use a Service from a list of provider Systems.
   *
   * @return IntraCloudAuthResponse
   * @throws DataNotFoundException, BadPayloadException
   */
  //TODO token generation if flag set true
  @PUT
  @Path("/intracloud")
  public Response isSystemAuthorized(IntraCloudAuthRequest request) {
    log.info("Entered the isSystemAuthorized function");

    if (!request.isPayloadUsable()) {
      log.info("AuthorizationApi:isSystemAuthorized throws BadPayloadException.");
      throw new BadPayloadException("Bad payload: Missing/incomplete consumer, service" + " or providerList in the request payload.");
    }

    IntraCloudAuthResponse response = new IntraCloudAuthResponse();
    restrictionMap.put("systemGroup", request.getConsumer().getSystemGroup());
    restrictionMap.put("systemName", request.getConsumer().getSystemName());
    ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
    if (consumer == null) {
      log.info("Consumer is not in the authorization database. " + "(AuthorizationApi:isSystemAuthorized DataNotFoundException)");
      throw new DataNotFoundException("Consumer System is not in the database. " + request.getConsumer().toString());
    }

    HashMap<ArrowheadSystem, Boolean> authorizationState = new HashMap<>();
    log.info("authorizationState hashmap created");

    restrictionMap.clear();
    restrictionMap.put("serviceGroup", request.getService().getServiceGroup());
    restrictionMap.put("serviceDefinition", request.getService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("Service is not in the database. Returning NOT AUTHORIZED state.");
      for (ArrowheadSystem provider : request.getProviders()) {
        authorizationState.put(provider, false);
      }
      response.setAuthorizationMap(authorizationState);
      return Response.status(Status.OK).entity(response).build();
    }

    IntraCloudAuthorization authRight;
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

    response.setAuthorizationMap(authorizationState);
    log.info("isSystemAuthorized successfully returns.");
    return Response.status(Status.OK).entity(response).build();
  }

  /**
   * Creates a relation between local Systems, defining the consumable services between Systems. (Not bidirectional.) OneToMany relation between
   * consumer and providers, OneToMany relation between consumer and services.
   *
   * @return JAX-RS Response with status code 201 and ArrowheadSystem entity (the consumer system)
   */
  @POST
  @Path("/intracloud")
  public Response addSystemToAuthorized(IntraCloudAuthEntry entry) {
    log.info("Entered the addSystemToAuthorized function");

    if (!entry.isPayloadUsable()) {
      log.info("AuthorizationApi:addSystemToAuthorized throws BadPayloadException.");
      throw new BadPayloadException("Bad payload: Missing/incomplete consumer, " + "serviceList or providerList in the entry payload.");
    }

    restrictionMap.put("systemGroup", entry.getConsumer().getSystemGroup());
    restrictionMap.put("systemName", entry.getConsumer().getSystemName());
    ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
    if (consumer == null) {
      log.info("Consumer System " + entry.getConsumer().toString() + " was not in the database, saving it now.");
      consumer = dm.save(entry.getConsumer());
    }

    ArrowheadSystem retrievedSystem = null;
    ArrowheadService retrievedService = null;
    IntraCloudAuthorization authRight = new IntraCloudAuthorization();
    List<IntraCloudAuthorization> savedAuthRights = new ArrayList<>();

    for (ArrowheadSystem providerSystem : entry.getProviderList()) {
      restrictionMap.clear();
      restrictionMap.put("systemGroup", providerSystem.getSystemGroup());
      restrictionMap.put("systemName", providerSystem.getSystemName());
      retrievedSystem = dm.get(ArrowheadSystem.class, restrictionMap);
      if (retrievedSystem == null) {
        log.info("Provider System " + providerSystem.toString() + " was not in the database, saving it now.");
        retrievedSystem = dm.save(providerSystem);
      }
      for (ArrowheadService service : entry.getServiceList()) {
        restrictionMap.clear();
        restrictionMap.put("serviceGroup", service.getServiceGroup());
        restrictionMap.put("serviceDefinition", service.getServiceDefinition());
        retrievedService = dm.get(ArrowheadService.class, restrictionMap);
        if (retrievedService == null) {
          log.info("Service " + service.toString() + " was not in the database, saving it now.");
          retrievedService = dm.save(service);
        }
        authRight.setConsumer(consumer);
        authRight.setProvider(retrievedSystem);
        authRight.setService(retrievedService);
        authRight = dm.merge(authRight);
        savedAuthRights.add(authRight);
      }
    }

    log.info(savedAuthRights.size() + " authorization rights created.");
    GenericEntity<List<IntraCloudAuthorization>> entity = new GenericEntity<List<IntraCloudAuthorization>>(savedAuthRights) {
    };
    return Response.status(Status.CREATED).entity(entity).build();
  }

  /**
   * Deletes the IntraCloudAuthorization entry with the id specified by the path parameter. Returns 200 if the delete is succesful, 204 (no content)
   * if the entry was not in the database to begin with.
   */
  @DELETE
  @Path("/intracloud/{id}")
  public Response deleteIntraEntry(@PathParam("id") Integer id) {

    restrictionMap.put("id", id);
    IntraCloudAuthorization entry = dm.get(IntraCloudAuthorization.class, restrictionMap);
    if (entry == null) {
      log.info("deleteIntraEntry had no effect.");
      return Response.noContent().build();
    } else {
      dm.delete(entry);
      log.info("deleteIntraEntry successfully returns.");
      return Response.ok().build();
    }
  }

  /**
   * Deletes all the authorization right relations where the given System is the consumer.
   *
   * @return JAX-RS Response with status code 200 (if delete is succesfull) or 204 (if nothing happens).
   */
  @DELETE
  @Path("/intracloud/systemgroup/{systemGroup}/systemname/{systemName}")
  public Response deleteSystemRelations(@PathParam("systemGroup") String systemGroup, @PathParam("systemName") String systemName) {
    log.info("Entered the deleteSystemRelations method.");

    restrictionMap.put("systemGroup", systemGroup);
    restrictionMap.put("systemName", systemName);
    ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
    if (consumer == null) {
      log.info("deleteSystemRelations had no effect.");
      return Response.noContent().build();
    }

    List<IntraCloudAuthorization> authRightsList = new ArrayList<>();
    restrictionMap.clear();
    restrictionMap.put("consumer", consumer);
    authRightsList = dm.getAll(IntraCloudAuthorization.class, restrictionMap);
    if (!authRightsList.isEmpty()) {
      for (IntraCloudAuthorization authRight : authRightsList) {
        dm.delete(authRight);
      }

      log.info("deleteSystemRelations successfully returns.");
      return Response.ok().build();
    }

    log.info("deleteSystemRelations had no effect.");
    return Response.noContent().build();
  }

  /**
   * Returns the list of consumable Services of a System.
   *
   * @return List<ArrowheadService>
   */
  @GET
  @Path("/intracloud/systemgroup/{systemGroup}/systemname/{systemName}/services")
  public Set<ArrowheadService> getSystemServices(@PathParam("systemGroup") String systemGroup, @PathParam("systemName") String systemName) {

    restrictionMap.put("systemGroup", systemGroup);
    restrictionMap.put("systemName", systemName);
    ArrowheadSystem system = dm.get(ArrowheadSystem.class, restrictionMap);
    if (system == null) {
      log.info("AuthorizationApi:getSystemServices throws DataNotFoundException.");
      throw new DataNotFoundException("This System is not in the authorization database. (SG: " + systemGroup + ", SN: " + systemName + ")");
    }

    List<IntraCloudAuthorization> authRightsList = new ArrayList<>();
    restrictionMap.clear();
    restrictionMap.put("consumer", system);
    authRightsList = dm.getAll(IntraCloudAuthorization.class, restrictionMap);
    Set<ArrowheadService> serviceList = new HashSet<>();
    for (IntraCloudAuthorization authRight : authRightsList) {
      serviceList.add(authRight.getService());
    }

    log.info("getSystemServices successfully returns " + serviceList.size() + " services");
    return serviceList;
  }

  /**
   * Returns all the InterCloud authorization rights from the database.
   *
   * @return List<InterCloudAuthorization>
   */
  @GET
  @Path("/intercloud")
  public List<InterCloudAuthorization> getInterCloudAuthRights() {

    List<InterCloudAuthorization> authRights = new ArrayList<>();
    authRights = dm.getAll(InterCloudAuthorization.class, restrictionMap);
    if (authRights.isEmpty()) {
      log.info("AuthorizationApi:getInterCloudAuthRights throws DataNotFoundException.");
      throw new DataNotFoundException("InterCloud authorization rights " + "were not found in the database.");
    }

    log.info("getInterCloudAuthRights successfully returns " + authRights.size() + " entries.");
    return authRights;
  }

  /**
   * Checks whether an external Cloud can use a local Service.
   *
   * @return boolean
   * @throws DataNotFoundException, BadPayloadException
   */
  @PUT
  @Path("/intercloud")
  public Response isCloudAuthorized(InterCloudAuthRequest request) {
    log.info("Entered the isCloudAuthorized function");

    if (!request.isPayloadUsable()) {
      log.info("AuthorizationApi:isCloudAuthorized throws BadPayloadException.");
      throw new BadPayloadException("Bad payload: Missing/incomplete cloud or service in the request payload.");
    }

    restrictionMap.put("operator", request.getCloud().getOperator());
    restrictionMap.put("cloudName", request.getCloud().getCloudName());
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      log.info("Consumer Cloud is not in the authorization database. " + "(AuthorizationApi:isCloudAuthorized DataNotFoundException)");
      throw new DataNotFoundException("Consumer Cloud is not in the authorization database. " + request.getCloud().toString());
    }

    Boolean isAuthorized = false;
    restrictionMap.clear();
    restrictionMap.put("serviceGroup", request.getService().getServiceGroup());
    restrictionMap.put("serviceDefinition", request.getService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("Service is not in the database. Returning NOT AUTHORIZED state. " + request.getService().toString());
      return Response.status(Status.OK).entity(isAuthorized).build();
    }

    InterCloudAuthorization authRight = new InterCloudAuthorization();
    restrictionMap.clear();
    restrictionMap.put("cloud", cloud);
    restrictionMap.put("service", service);
    authRight = dm.get(InterCloudAuthorization.class, restrictionMap);

    if (authRight != null) {
      isAuthorized = true;
    }

    log.info("isCloudAuthorized successfully returns with the answer: " + isAuthorized.toString());
    InterCloudAuthResponse response = new InterCloudAuthResponse(isAuthorized);
    return Response.status(Status.OK).entity(response).build();
  }

  /**
   * Adds a new Cloud and its consumable Services to the database.
   *
   * @return JAX-RS Response with status code 201 and ArrowheadCloud entity
   */
  @POST
  @Path("/intercloud")
  public Response addCloudToAuthorized(InterCloudAuthEntry entry) {
    log.info("Entered the addCloudToAuthorized function");

    if (!entry.isPayloadUsable()) {
      log.info("AuthorizationApi:addCloudToAuthorized throws BadPayloadException.");
      throw new BadPayloadException("Bad payload: Missing/incomplete cloud or serviceList in the entry payload.");
    }

    restrictionMap.put("operator", entry.getCloud().getOperator());
    restrictionMap.put("cloudName", entry.getCloud().getCloudName());
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      log.info("Consumer Cloud was not in the database, saving it now." + entry.getCloud().toString());
      cloud = dm.save(entry.getCloud());
    }

    ArrowheadService retrievedService = null;
    InterCloudAuthorization authRight = new InterCloudAuthorization();
    List<InterCloudAuthorization> savedAuthRights = new ArrayList<>();

    for (ArrowheadService service : entry.getServiceList()) {
      restrictionMap.clear();
      restrictionMap.put("serviceGroup", service.getServiceGroup());
      restrictionMap.put("serviceDefinition", service.getServiceDefinition());
      retrievedService = dm.get(ArrowheadService.class, restrictionMap);
      if (retrievedService == null) {
        log.info("Service was not in the database, saving it now." + service.toString());
        retrievedService = dm.save(service);
      }
      authRight.setCloud(cloud);
      authRight.setService(retrievedService);
      authRight = dm.merge(authRight);
      savedAuthRights.add(authRight);
    }

    log.info(savedAuthRights.size() + " authorization rights created.");
    GenericEntity<List<InterCloudAuthorization>> entity = new GenericEntity<List<InterCloudAuthorization>>(savedAuthRights) {
    };
    return Response.status(Status.CREATED).entity(entity).build();
  }

  /**
   * Deletes the InterCloudAuthorization entry with the id specified by the path parameter. Returns 200 if the delete is succesful, 204 (no content)
   * if the entry was not in the database to begin with.
   */
  @DELETE
  @Path("/intercloud/{id}")
  public Response deleteInterEntry(@PathParam("id") Integer id) {

    restrictionMap.put("id", id);
    InterCloudAuthorization entry = dm.get(InterCloudAuthorization.class, restrictionMap);
    if (entry == null) {
      log.info("deleteInterEntry had no effect.");
      return Response.noContent().build();
    } else {
      dm.delete(entry);
      log.info("deleteInterEntry successfully returns.");
      return Response.ok().build();
    }
  }

  /**
   * Deletes the authorization rights of the Cloud.
   *
   * @return JAX-RS Response with status code 200 (if delete is succesfull) or 204 (if nothing happens).
   */
  @DELETE
  @Path("/intercloud/operator/{operator}/cloudname/{cloudName}")
  public Response deleteCloudRelations(@PathParam("operator") String operator, @PathParam("cloudName") String cloudName) {
    log.info("Entered the deleteCloudRelations method.");

    restrictionMap.put("operator", operator);
    restrictionMap.put("cloudName", cloudName);
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      log.info("deleteCloudRelations had no effect.");
      return Response.noContent().build();
    }

    List<InterCloudAuthorization> authRightsList = new ArrayList<>();
    restrictionMap.clear();
    restrictionMap.put("cloud", cloud);
    authRightsList = dm.getAll(InterCloudAuthorization.class, restrictionMap);
    if (!authRightsList.isEmpty()) {
      for (InterCloudAuthorization authRight : authRightsList) {
        dm.delete(authRight);
      }

      log.info("deleteCloudRelations successfully returns.");
      return Response.ok().build();
    }

    log.info("deleteCloudRelations had no effect.");
    return Response.noContent().build();
  }

  /**
   * Returns the list of consumable Services of a Cloud.
   *
   * @return List<ArrowheadService>
   */
  @GET
  @Path("/intercloud/operator/{operator}/cloudname/{cloudName}/services")
  public Set<ArrowheadService> getCloudServices(@PathParam("operator") String operator, @PathParam("cloudName") String cloudName) {

    restrictionMap.put("operator", operator);
    restrictionMap.put("cloudName", cloudName);
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      log.info("AuthorizationApi:getCloudServices throws DataNotFoundException.");
      throw new DataNotFoundException("Consumer Cloud is not in the authorization database. (OP: " + operator + ", CN: " + cloudName + ")");
    }

    List<InterCloudAuthorization> authRightsList = new ArrayList<>();
    restrictionMap.clear();
    restrictionMap.put("cloud", cloud);
    authRightsList = dm.getAll(InterCloudAuthorization.class, restrictionMap);
    Set<ArrowheadService> serviceList = new HashSet<>();
    for (InterCloudAuthorization authRight : authRightsList) {
      serviceList.add(authRight.getService());
    }

    log.info("getCloudServices successfully returns " + serviceList.size() + " services.");
    return serviceList;
  }


}
