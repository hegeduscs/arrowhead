/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.orchestrator.api;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.apache.log4j.Logger;

@Path("orchestrator/mgmt/common")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommonApi {

  private static final Logger log = Logger.getLogger(CommonApi.class.getName());
  private static final DatabaseManager dm = DatabaseManager.getInstance();
  private static final HashMap<String, Object> restrictionMap = new HashMap<>();

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "orchestrator/mgmt/common got it!";
  }

  /**
   * Returns the list of ArrowheadServices from the database.
   *
   * @return List<ArrowheadService>
   */
  @GET
  @Path("services")
  public List<ArrowheadService> getAllServices() {

    List<ArrowheadService> serviceList = dm.getAll(ArrowheadService.class, restrictionMap);
    if (serviceList.isEmpty()) {
      log.info("getAllServices throws DataNotFoundException");
      throw new DataNotFoundException("ArrowheadServices not found in the database.");
    }

    return serviceList;
  }

  /**
   * Returns an ArrowheadService from the database specified by the service definition.
   *
   * @return ArrowheadService
   */
  @GET
  @Path("services/servicedef/{serviceDefinition}")
  public ArrowheadService getService(@PathParam("serviceDefinition") String serviceDefinition) {

    restrictionMap.put("serviceDefinition", serviceDefinition);
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("getService throws DataNotFoundException");
      throw new DataNotFoundException("Requested ArrowheadService not found in the database.");
    }

    return service;
  }

  /**
   * Adds a list of ArrowheadServices to the database. Elements which would cause DuplicateEntryException or BadPayloadException (caused by missing
   * serviceDefinition) are being skipped. The returned list only contains the elements which were saved in the process.
   *
   * @return List<ArrowheadService>
   */
  @POST
  @Path("services")
  public Response addServices(List<ArrowheadService> serviceList) {

    List<ArrowheadService> savedServices = new ArrayList<>();
    for (ArrowheadService service : serviceList) {
      if (service.isValidForDatabase()) {
        restrictionMap.clear();
        restrictionMap.put("serviceDefinition", service.getServiceDefinition());
        ArrowheadService retrievedService = dm.get(ArrowheadService.class, restrictionMap);
        if (retrievedService == null) {
          dm.save(service);
          savedServices.add(service);
        }
      }
    }

    if (savedServices.isEmpty()) {
      return Response.status(Status.NO_CONTENT).build();
    } else {
      return Response.status(Status.CREATED).entity(savedServices).build();
    }
  }

  /**
   * Updates an existing ArrowheadService in the database. Returns 204 (no content) if the specified ArrowheadService was not in the database.
   */
  @PUT
  @Path("services")
  public Response updateService(ArrowheadService service) {

    if (!service.isValidForDatabase()) {
      log.info("updateService throws BadPayloadException");
      throw new BadPayloadException("Bad payload: missing service definition in the entry payload.");
    }

    restrictionMap.put("serviceDefinition", service.getServiceDefinition());
    ArrowheadService retrievedService = dm.get(ArrowheadService.class, restrictionMap);
    if (retrievedService != null) {
      retrievedService.setInterfaces(service.getInterfaces());
      retrievedService.setServiceMetadata(service.getServiceMetadata());
      retrievedService = dm.merge(retrievedService);
      return Response.status(Status.ACCEPTED).entity(retrievedService).build();
    } else {
      return Response.noContent().build();
    }
  }

  /**
   * Deletes the ArrowheadService from the database specified by the service definition. Returns 200 if the delete is successful, 204 (no content) if
   * the service was not in the database to begin with.
   */
  @DELETE
  @Path("services/servicedef/{serviceDefinition}")
  public Response deleteService(@PathParam("serviceDefinition") String serviceDefinition) {

    restrictionMap.put("serviceDefinition", serviceDefinition);
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      return Response.noContent().build();
    } else {
      dm.delete(service);
      return Response.ok().build();
    }
  }

  /**
   * Returns the list of ArrowheadSystems from the database.
   *
   * @return List<ArrowheadSystem>
   */
  @GET
  @Path("systems")
  public List<ArrowheadSystem> getAllSystems() {

    List<ArrowheadSystem> systemList = dm.getAll(ArrowheadSystem.class, restrictionMap);
    if (systemList.isEmpty()) {
      log.info("getAllSystems throws DataNotFoundException");
      throw new DataNotFoundException("ArrowheadSystems not found in the database.");
    }

    return systemList;
  }

  /**
   * Returns an ArrowheadSystem from the database specified by the system name.
   *
   * @return ArrowheadSystem
   */
  @GET
  @Path("systems/systemname/{systemName}")
  public ArrowheadSystem getSystem(@PathParam("systemName") String systemName) {

    restrictionMap.put("systemName", systemName);
    ArrowheadSystem system = dm.get(ArrowheadSystem.class, restrictionMap);
    if (system == null) {
      log.info("getSystem throws DataNotFoundException");
      throw new DataNotFoundException("Requested ArrowheadSystem not found in the database.");
    }

    return system;
  }

  /**
   * Adds a list of ArrowheadSystems to the database. Elements which would cause DuplicateEntryException or BadPayloadException (caused by missing
   * systemName or address) are being skipped. The returned list only contains the elements which were saved in the process.
   *
   * @return List<ArrowheadSystem>
   */
  @POST
  @Path("systems")
  public Response addSystems(List<ArrowheadSystem> systemList) {

    List<ArrowheadSystem> savedSystems = new ArrayList<>();
    for (ArrowheadSystem system : systemList) {
      if (system.isValid()) {
        restrictionMap.clear();
        restrictionMap.put("systemName", system.getSystemName());
        ArrowheadSystem retrievedSystem = dm.get(ArrowheadSystem.class, restrictionMap);
        restrictionMap.clear();
        if (retrievedSystem == null) {
          dm.save(system);
          savedSystems.add(system);
        }
      }
    }

    if (savedSystems.isEmpty()) {
      return Response.status(Status.NO_CONTENT).build();
    } else {
      return Response.status(Status.CREATED).entity(savedSystems).build();
    }
  }

  /**
   * Updates an existing ArrowheadSystem in the database. Returns 204 (no content) if the specified ArrowheadSystem was not in the database.
   */
  @PUT
  @Path("systems")
  public Response updateSystem(ArrowheadSystem system) {

    if (!system.isValid()) {
      log.info("updateSystem throws BadPayloadException");
      throw new BadPayloadException("Bad payload: missing system name or address in the entry payload.");
    }

    restrictionMap.put("systemName", system.getSystemName());
    ArrowheadSystem retrievedSystem = dm.get(ArrowheadSystem.class, restrictionMap);
    if (retrievedSystem != null) {
      retrievedSystem.setAddress(system.getAddress());
      retrievedSystem.setPort(system.getPort());
      retrievedSystem.setAuthenticationInfo(system.getAuthenticationInfo());
      retrievedSystem = dm.merge(retrievedSystem);
      return Response.status(Status.ACCEPTED).entity(retrievedSystem).build();
    } else {
      return Response.noContent().build();
    }
  }

  /**
   * Deletes the ArrowheadSystem from the database specified by the system name. Returns 200 if the delete is successful, 204 (no content) if the
   * system was not in the database to begin with.
   */
  @DELETE
  @Path("systems/systemname/{systemName}")
  public Response deleteSystem(@PathParam("systemName") String systemName) {

    restrictionMap.put("systemName", systemName);
    ArrowheadSystem system = dm.get(ArrowheadSystem.class, restrictionMap);
    if (system == null) {
      return Response.noContent().build();
    } else {
      dm.delete(system);
      return Response.ok().build();
    }
  }

  /**
   * Returns the list of ArrowheadClouds from the database.
   *
   * @return List<ArrowheadCloud>
   */
  @GET
  @Path("clouds")
  public List<ArrowheadCloud> getAllClouds() {

    List<ArrowheadCloud> cloudList = dm.getAll(ArrowheadCloud.class, restrictionMap);
    if (cloudList.isEmpty()) {
      log.info("CommonApi:getAllClouds throws DataNotFoundException");
      throw new DataNotFoundException("ArrowheadClouds not found in the database.");
    }

    return cloudList;
  }

  /**
   * Returns a list of ArrowheadClouds from the database specified by the operator.
   *
   * @return List<ArrowheadCloud>
   */
  @GET
  @Path("clouds/operator/{operator}")
  public List<ArrowheadCloud> getCloudList(@PathParam("operator") String operator) {

    restrictionMap.put("operator", operator);
    List<ArrowheadCloud> cloudList = dm.getAll(ArrowheadCloud.class, restrictionMap);
    if (cloudList.isEmpty()) {
      log.info("CommonApi:getCloudList throws DataNotFoundException");
      throw new DataNotFoundException("ArrowheadClouds not found in the database from this operator.");
    }

    return cloudList;
  }

  /**
   * Returns an ArrowheadCloud from the database specified by the operator and cloud name.
   *
   * @return ArrowheadCloud
   */
  @GET
  @Path("clouds/operator/{operator}/cloudname/{cloudName}")
  public ArrowheadCloud getCloud(@PathParam("operator") String operator, @PathParam("cloudName") String cloudName) {

    restrictionMap.put("operator", operator);
    restrictionMap.put("cloudName", cloudName);
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      log.info("CommonApi:getCloud throws DataNotFoundException");
      throw new DataNotFoundException("Requested ArrowheadCloud not found in the database.");
    }

    return cloud;
  }

  /**
   * Adds a list of ArrowheadClouds to the database. Elements which would cause DuplicateEntryException or BadPayloadException (caused by missing
   * operator, cloudName, address or serviceURI) are being skipped. The returned list only contains the elements which were saved in the process.
   *
   * @return List<ArrowheadCloud>
   */
  @POST
  @Path("clouds")
  public Response addClouds(List<ArrowheadCloud> cloudList) {

    List<ArrowheadCloud> savedClouds = new ArrayList<>();
    for (ArrowheadCloud cloud : cloudList) {
      if (cloud.isValid()) {
        restrictionMap.clear();
        restrictionMap.put("operator", cloud.getOperator());
        restrictionMap.put("cloudName", cloud.getCloudName());
        ArrowheadCloud retrievedCloud = dm.get(ArrowheadCloud.class, restrictionMap);
        restrictionMap.clear();
        if (retrievedCloud == null) {
          dm.save(cloud);
          savedClouds.add(cloud);
        }
      }
    }

    if (savedClouds.isEmpty()) {
      return Response.status(Status.NO_CONTENT).build();
    } else {
      return Response.status(Status.CREATED).entity(savedClouds).build();
    }
  }

  /**
   * Updates an existing ArrowheadCloud in the database. Returns 204 (no content) if the specified ArrowheadCloud was not in the database.
   */
  @PUT
  @Path("clouds")
  public Response updateCloud(ArrowheadCloud cloud) {

    if (!cloud.isValid()) {
      log.info("CommonApi:updateCloud throws BadPayloadException");
      throw new BadPayloadException("Bad payload: missing operator, cloudName, address or serviceURI in the entry payload.");
    }

    restrictionMap.put("operator", cloud.getOperator());
    restrictionMap.put("cloudName", cloud.getCloudName());
    ArrowheadCloud retrievedCloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (retrievedCloud != null) {
      retrievedCloud.setAddress(cloud.getAddress());
      retrievedCloud.setPort(cloud.getPort());
      retrievedCloud.setGatekeeperServiceURI(cloud.getGatekeeperServiceURI());
      retrievedCloud.setAuthenticationInfo(cloud.getAuthenticationInfo());
      retrievedCloud.setSecure(cloud.isSecure());
      retrievedCloud = dm.merge(retrievedCloud);
      return Response.status(Status.ACCEPTED).entity(retrievedCloud).build();
    } else {
      return Response.noContent().build();
    }
  }

  /**
   * Deletes the ArrowheadCloud from the database specified by the operator and cloud name. Returns 200 if the delete is successful, 204 (no content)
   * if the cloud was not in the database to begin with.
   */
  @DELETE
  @Path("clouds/operator/{operator}/cloudname/{cloudName}")
  public Response deleteCloud(@PathParam("operator") String operator, @PathParam("cloudName") String cloudName) {

    restrictionMap.put("operator", operator);
    restrictionMap.put("cloudName", cloudName);
    ArrowheadCloud cloud = dm.get(ArrowheadCloud.class, restrictionMap);
    if (cloud == null) {
      return Response.noContent().build();
    } else {
      dm.delete(cloud);
      return Response.ok().build();
    }
  }

}
