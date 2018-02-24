/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.authorization;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.InterCloudAuthorization;
import eu.arrowhead.common.database.IntraCloudAuthorization;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.messages.InterCloudAuthEntry;
import eu.arrowhead.common.messages.IntraCloudAuthEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

@Path("authorization/mgmt")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthorizationApi {

  private final HashMap<String, Object> restrictionMap = new HashMap<>();
  private static final Logger log = Logger.getLogger(AuthorizationApi.class.getName());
  private static final DatabaseManager dm = DatabaseManager.getInstance();

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "authorization/mgmt got it";
  }

  /**
   * Returns all the IntraCloud authorization rights from the database.
   *
   * @return List<IntraCloudAuthorization>
   */
  @GET
  @Path("intracloud")
  public List<IntraCloudAuthorization> getIntraCloudAuthRights() {

    List<IntraCloudAuthorization> authRights = dm.getAll(IntraCloudAuthorization.class, restrictionMap);
    if (authRights.isEmpty()) {
      log.info("getIntraCloudAuthRights throws DataNotFoundException.");
      throw new DataNotFoundException("IntraCloud authorization rights were not found in the database.");
    }

    log.info("getIntraCloudAuthRights successfully returns " + authRights.size() + " entries.");
    return authRights;
  }

  /**
   * Returns the list of consumable Services of a System.
   *
   * @return List<ArrowheadService>
   */

  @GET
  @Path("intracloud/systemname/{systemName}/services")
  public Set<ArrowheadService> getSystemServices(@PathParam("systemName") String systemName, @QueryParam("provider_side") boolean providerSide) {

    restrictionMap.put("systemName", systemName);
    ArrowheadSystem system = dm.get(ArrowheadSystem.class, restrictionMap);
    if (system == null) {
      log.info("getSystemServices throws DataNotFoundException.");
      throw new DataNotFoundException("The system " + systemName + " is not in the authorization database");
    }

    restrictionMap.clear();
    if (!providerSide) {
      restrictionMap.put("consumer", system);
    } else {
      restrictionMap.put("provider", system);
    }
    List<IntraCloudAuthorization> authRightsList = dm.getAll(IntraCloudAuthorization.class, restrictionMap);
    if (authRightsList.isEmpty()) {
      log.info("getSystemServices throws DataNotFoundException.");
      throw new DataNotFoundException("IntraCloud authorization rights were not found in the database for this consumer system.");
    }

    Set<ArrowheadService> serviceList = new HashSet<>();
    for (IntraCloudAuthorization authRight : authRightsList) {
      serviceList.add(authRight.getService());
    }
    log.info("getSystemServices successfully returns " + serviceList.size() + " services");
    return serviceList;
  }

  @GET
  @Path("intracloud/systemname/{systemName}")
  public List<IntraCloudAuthorization> getSystemAuthRights(@PathParam("systemName") String systemName) {

    restrictionMap.put("systemName", systemName);
    ArrowheadSystem system = dm.get(ArrowheadSystem.class, restrictionMap);
    if (system == null) {
      log.info("getSystemAuthRights throws DataNotFoundException.");
      throw new DataNotFoundException("The system " + systemName + " is not in the authorization database");
    }

    restrictionMap.clear();
    restrictionMap.put("consumer", system);
    restrictionMap.put("provider", system);
    List<IntraCloudAuthorization> authRights = dm.getAllOfEither(IntraCloudAuthorization.class, restrictionMap);
    if (authRights.isEmpty()) {
      log.info("getSystemAuthRights throws DataNotFoundException.");
      throw new DataNotFoundException("This System is not in the authorization database. " + system.getSystemName());
    }
    log.info("getSystemAuthRights returns");
    return authRights;
  }

  @GET
  @Path("intracloud/servicedef/{serviceDefinition}")
  public List<IntraCloudAuthorization> getServiceIntraAuthRights(@PathParam("serviceDefinition") String serviceDefinition) {

    restrictionMap.put("serviceDefinition", serviceDefinition);
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("getServiceIntraAuthRights throws DataNotFoundException.");
      throw new DataNotFoundException("The service " + serviceDefinition + " is not in the authorization database");
    }

    restrictionMap.clear();
    restrictionMap.put("service", service);
    List<IntraCloudAuthorization> authRights = dm.getAll(IntraCloudAuthorization.class, restrictionMap);
    if (authRights.isEmpty()) {
      log.info("getServiceIntraAuthRights throws DataNotFoundException.");
      throw new DataNotFoundException("This Service is not in the authorization database. " + service.toString());
    }
    log.info("getServiceIntraAuthRights returns");
    return authRights;
  }


  /**
   * Creates relations between local Systems, defining the consumable services between Systems. (Not bidirectional.) OneToMany relation between
   * consumer and providers, OneToMany relation between consumer and services.
   *
   * @return JAX-RS Response with status code 201 and ArrowheadSystem entity (the consumer system)
   */
  @POST
  @Path("intracloud")
  public Response addSystemToAuthorized(IntraCloudAuthEntry entry) {

    if (!entry.isValid()) {
      log.info("addSystemToAuthorized throws BadPayloadException.");
      throw new BadPayloadException("Bad payload: Missing/incomplete consumer, serviceList or providerList in the entry payload.");
    }

    restrictionMap.put("systemName", entry.getConsumer().getSystemName());
    ArrowheadSystem consumer = dm.get(ArrowheadSystem.class, restrictionMap);
    if (consumer == null) {
      log.info("Consumer System " + entry.getConsumer().getSystemName() + " was not in the database, saving it now.");
      consumer = dm.save(entry.getConsumer());
    }

    ArrowheadSystem retrievedSystem;
    ArrowheadService retrievedService;
    List<IntraCloudAuthorization> savedAuthRights = new ArrayList<>();
    for (ArrowheadSystem providerSystem : entry.getProviderList()) {
      restrictionMap.clear();
      restrictionMap.put("systemName", providerSystem.getSystemName());
      retrievedSystem = dm.get(ArrowheadSystem.class, restrictionMap);
      if (retrievedSystem == null) {
        log.info("Provider System " + providerSystem.getSystemName() + " was not in the database, saving it now.");
        retrievedSystem = dm.save(providerSystem);
      }
      for (ArrowheadService service : entry.getServiceList()) {
        restrictionMap.clear();
        restrictionMap.put("serviceDefinition", service.getServiceDefinition());
        retrievedService = dm.get(ArrowheadService.class, restrictionMap);
        if (retrievedService == null) {
          log.info("Service " + service.toString() + " was not in the database, saving it now.");
          retrievedService = dm.save(service);
        }
        restrictionMap.clear();
        restrictionMap.put("consumer", consumer);
        restrictionMap.put("provider", retrievedSystem);
        restrictionMap.put("service", retrievedService);
        IntraCloudAuthorization authRight = dm.get(IntraCloudAuthorization.class, restrictionMap);
        if (authRight == null) {
          authRight = dm.save(new IntraCloudAuthorization(consumer, retrievedSystem, retrievedService));
          savedAuthRights.add(authRight);
        }
      }
    }

    log.info("addSystemToAuthorized: " + savedAuthRights.size() + " authorization rights created.");
    GenericEntity<List<IntraCloudAuthorization>> entity = new GenericEntity<List<IntraCloudAuthorization>>(savedAuthRights) {
    };
    return Response.status(Status.CREATED).entity(entity).build();
  }

  /**
   * Deletes the IntraCloudAuthorization entry with the id specified by the path parameter. Returns 200 if the delete is successful, 204 (no content)
   * if the entry was not in the database to begin with.
   */
  @DELETE
  @Path("intracloud/{id}")
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
   * Deletes all the authorization right relations where the given System is the consumer/provider (decided by query parameter).
   *
   * @return JAX-RS Response with status code 200 (if delete is succesxfull) or 204 (if nothing happens).
   */
  @DELETE
  @Path("intracloud/systemname/{systemName}")
  public Response deleteSystemRelations(@PathParam("systemName") String systemName, @QueryParam("provider_side") boolean providerSide) {

    restrictionMap.put("systemName", systemName);
    ArrowheadSystem system = dm.get(ArrowheadSystem.class, restrictionMap);
    if (system == null) {
      log.info("deleteSystemRelations had no effect.");
      return Response.noContent().build();
    }

    restrictionMap.clear();
    if (!providerSide) {
      restrictionMap.put("consumer", system);
    } else {
      restrictionMap.put("provider", system);
    }
    List<IntraCloudAuthorization> authRightsList = dm.getAll(IntraCloudAuthorization.class, restrictionMap);
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
   * Returns all the InterCloud authorization rights from the database.
   *
   * @return List<InterCloudAuthorization>
   */
  @GET
  @Path("intercloud")
  public List<InterCloudAuthorization> getInterCloudAuthRights() {

    List<InterCloudAuthorization> authRights = dm.getAll(InterCloudAuthorization.class, restrictionMap);
    if (authRights.isEmpty()) {
      log.info("getInterCloudAuthRights throws DataNotFoundException.");
      throw new DataNotFoundException("InterCloud authorization rights were not found in the database.");
    }

    log.info("getInterCloudAuthRights successfully returns " + authRights.size() + " entries.");
    return authRights;
  }

  /**
   * Returns the list of consumable Services of a Cloud.
   *
   * @return List<ArrowheadService>
   */

  @GET
  @Path("intercloud/operator/{operator}/cloudname/{cloudName}/services")
  public Set<ArrowheadService> getCloudServices(@PathParam("operator") String operator, @PathParam("cloudName") String cloudName) {

    restrictionMap.put("operator", operator);
    restrictionMap.put("cloudName", cloudName);
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      log.info("getCloudServices throws DataNotFoundException.");
      throw new DataNotFoundException("Consumer Cloud (" + operator + ":" + cloudName + ") is not in the authorization database");
    }

    restrictionMap.clear();
    restrictionMap.put("cloud", cloud);
    List<InterCloudAuthorization> authRightsList = dm.getAll(InterCloudAuthorization.class, restrictionMap);
    Set<ArrowheadService> serviceList = new HashSet<>();
    for (InterCloudAuthorization authRight : authRightsList) {
      serviceList.add(authRight.getService());
    }

    log.info("getCloudServices successfully returns " + serviceList.size() + " services.");
    return serviceList;
  }

  @GET
  @Path("intercloud/operator/{operator}/cloudname/{cloudName}")
  public List<InterCloudAuthorization> getCloudAuthRights(@PathParam("operator") String operator, @PathParam("cloudName") String cloudName) {

    restrictionMap.put("operator", operator);
    restrictionMap.put("cloudName", cloudName);
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      log.info("getCloudServices throws DataNotFoundException.");
      throw new DataNotFoundException("Consumer Cloud (" + operator + ":" + cloudName + ") is not in the authorization database");
    }

    restrictionMap.clear();
    restrictionMap.put("cloud", cloud);
    List<InterCloudAuthorization> authRightsList = dm.getAll(InterCloudAuthorization.class, restrictionMap);
    if (authRightsList.isEmpty()) {
      log.info("getCloudAuthRights throws DataNotFoundException.");
      throw new DataNotFoundException("This Cloud is not in the authorization database. " + cloud.toString());
    }
    log.info("getCloudAuthRights successfully returns " + authRightsList.size() + " auth entries.");
    return authRightsList;
  }

  @GET
  @Path("intercloud/servicedef/{serviceDefinition}")
  public List<InterCloudAuthorization> getServiceInterAuthRights(@PathParam("serviceDefinition") String serviceDefinition) {

    restrictionMap.put("serviceDefinition", serviceDefinition);
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("getServiceInterAuthRights throws DataNotFoundException.");
      throw new DataNotFoundException("Consumer Cloud (" + serviceDefinition + ") is not in the authorization database");
    }

    restrictionMap.clear();
    restrictionMap.put("service", service);
    List<InterCloudAuthorization> authRights = dm.getAll(InterCloudAuthorization.class, restrictionMap);
    if (authRights.isEmpty()) {
      log.info("getServiceInterAuthRights throws DataNotFoundException.");
      throw new DataNotFoundException("This Service is not in the authorization database. " + service.toString());
    }
    log.info("getServiceInterAuthRights returns");
    return authRights;
  }

  /**
   * Adds a new Cloud and its consumable Services to the database.
   *
   * @return JAX-RS Response with status code 201 and ArrowheadCloud entity
   */
  @POST
  @Path("intercloud")
  public Response addCloudToAuthorized(InterCloudAuthEntry entry) {

    if (!entry.isPayloadUsable()) {
      log.info("addCloudToAuthorized throws BadPayloadException.");
      throw new BadPayloadException("Bad payload: Missing/incomplete cloud or serviceList in the entry payload.");
    }

    restrictionMap.put("operator", entry.getCloud().getOperator());
    restrictionMap.put("cloudName", entry.getCloud().getCloudName());
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      log.info("Consumer Cloud was not in the database, saving it now." + entry.getCloud().toString());
      cloud = dm.save(entry.getCloud());
    }

    ArrowheadService retrievedService;
    List<InterCloudAuthorization> savedAuthRights = new ArrayList<>();
    for (ArrowheadService service : entry.getServiceList()) {
      restrictionMap.clear();
      restrictionMap.put("serviceDefinition", service.getServiceDefinition());
      retrievedService = dm.get(ArrowheadService.class, restrictionMap);
      if (retrievedService == null) {
        log.info("Service was not in the database, saving it now." + service.toString());
        retrievedService = dm.save(service);
      }
      restrictionMap.clear();
      restrictionMap.put("cloud", cloud);
      restrictionMap.put("service", retrievedService);
      InterCloudAuthorization authRight = dm.get(InterCloudAuthorization.class, restrictionMap);
      if (authRight == null) {
        authRight = dm.save(new InterCloudAuthorization(cloud, retrievedService));
        savedAuthRights.add(authRight);
      }
    }

    log.info("addCloudToAuthorized: " + savedAuthRights.size() + " authorization rights created.");
    GenericEntity<List<InterCloudAuthorization>> entity = new GenericEntity<List<InterCloudAuthorization>>(savedAuthRights) {
    };
    return Response.status(Status.CREATED).entity(entity).build();
  }

  /**
   * Deletes the InterCloudAuthorization entry with the id specified by the path parameter. Returns 200 if the delete is successful, 204 (no content)
   * if the entry was not in the database to begin with.
   */
  @DELETE
  @Path("intercloud/{id}")
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
   * @return JAX-RS Response with status code 200 (if delete is successful) or 204 (if nothing happens).
   */
  @DELETE
  @Path("intercloud/operator/{operator}/cloudname/{cloudName}")
  public Response deleteCloudRelations(@PathParam("operator") String operator, @PathParam("cloudName") String cloudName) {
    log.info("Entered the deleteCloudRelations method.");

    restrictionMap.put("operator", operator);
    restrictionMap.put("cloudName", cloudName);
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      log.info("deleteCloudRelations had no effect.");
      return Response.noContent().build();
    }

    restrictionMap.clear();
    restrictionMap.put("cloud", cloud);
    List<InterCloudAuthorization> authRightsList = dm.getAll(InterCloudAuthorization.class, restrictionMap);
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

}
