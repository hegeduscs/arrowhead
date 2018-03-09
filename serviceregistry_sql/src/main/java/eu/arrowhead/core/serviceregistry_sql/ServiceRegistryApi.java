/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.serviceregistry_sql;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.messages.ServiceQueryResult;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

@Path("serviceregistry/mgmt")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ServiceRegistryApi {

  private final HashMap<String, Object> restrictionMap = new HashMap<>();
  private static final DatabaseManager dm = DatabaseManager.getInstance();
  private static final Logger log = Logger.getLogger(ServiceRegistryApi.class.getName());

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "serviceregistry/mgmt got it";
  }

  @GET
  @Path("all")
  public Response getAllServices() {
    List<ServiceRegistryEntry> providedServices = dm.getAll(ServiceRegistryEntry.class, null);

    for (ServiceRegistryEntry entry : providedServices) {
      entry.fromDatabase();
    }

    ServiceQueryResult result = new ServiceQueryResult(providedServices);
    log.info("getAllServices returns " + result.getServiceQueryData().size() + " entries");
    if (result.getServiceQueryData().isEmpty()) {
      return Response.status(Status.NO_CONTENT).entity(result).build();
    } else {
      return Response.status(Response.Status.OK).entity(result).build();
    }
  }

  @DELETE
  @Path("all")
  public Response removeAllServices() {
    List<ServiceRegistryEntry> providedServices = dm.getAll(ServiceRegistryEntry.class, null);
    if (providedServices.isEmpty()) {
      log.info("removeAllServices had no effect");
      return Response.status(Status.NO_CONTENT).build();
    }
    for (ServiceRegistryEntry entry : providedServices) {
      dm.delete(entry);
    }
    log.info("removeAllServices returns successfully");
    return Response.status(Status.OK).build();
  }

  @GET
  @Path("systemname/{systemName}")
  public List<ServiceRegistryEntry> getAllByProvider(@PathParam("systemName") String systemName) {
    restrictionMap.put("systemName", systemName);
    ArrowheadSystem system = dm.get(ArrowheadSystem.class, restrictionMap);
    if (system == null) {
      log.info("getAllByProvider throws DataNotFoundException");
      throw new DataNotFoundException("There are no Service Registry entries with the requested ArrowheadSystem in the database.");
    }

    restrictionMap.clear();
    restrictionMap.put("provider", system);
    List<ServiceRegistryEntry> srList = dm.getAll(ServiceRegistryEntry.class, restrictionMap);
    if (srList.isEmpty()) {
      log.info("getAllByProvider throws DataNotFoundException");
      throw new DataNotFoundException("There are no Service Registry entries with the requested ArrowheadSystem in the database.");
    }

    for (ServiceRegistryEntry entry : srList) {
      entry.fromDatabase();
    }

    log.info("getAllByProvider returns " + srList.size() + " entries");
    return srList;
  }

  @GET
  @Path("servicedef/{serviceDefinition}")
  public List<ServiceRegistryEntry> getAllByService(@PathParam("serviceDefinition") String serviceDefinition) {
    restrictionMap.put("serviceDefinition", serviceDefinition);
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("getAllByService throws DataNotFoundException");
      throw new DataNotFoundException("There are no Service Registry entries with the requested ArrowheadService in the database.");
    }

    restrictionMap.clear();
    restrictionMap.put("providedService", service);
    List<ServiceRegistryEntry> srList = dm.getAll(ServiceRegistryEntry.class, restrictionMap);
    if (srList.isEmpty()) {
      log.info("getAllByService throws DataNotFoundException");
      throw new DataNotFoundException("There are no Service Registry entries with the requested ArrowheadService in the database.");
    }

    for (ServiceRegistryEntry entry : srList) {
      entry.fromDatabase();
    }

    log.info("getAllByService returns " + srList.size() + " entries");
    return srList;
  }

  @PUT
  @Path("update")
  public Response updateServiceRegistryEntry(ServiceRegistryEntry entry) {
    if (!entry.isValid()) {
      log.info("updateServiceRegistryEntry throws BadPayloadException");
      throw new BadPayloadException("Bad payload: ServiceRegistryEntry has missing/incomplete mandatory field(s).");
    }
    entry.toDatabase();

    restrictionMap.put("serviceDefinition", entry.getProvidedService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("updateServiceRegistryEntry throws DataNotFoundException");
      throw new DataNotFoundException("Requested Service Registry entry not found in the database.");
    }

    restrictionMap.clear();
    restrictionMap.put("systemName", entry.getProvider().getSystemName());
    ArrowheadSystem provider = dm.get(ArrowheadSystem.class, restrictionMap);
    if (provider == null) {
      log.info("updateServiceRegistryEntry throws DataNotFoundException");
      throw new DataNotFoundException("Requested Service Registry entry not found in the database.");
    }

    restrictionMap.clear();
    restrictionMap.put("provider", provider);
    restrictionMap.put("providedService", service);
    ServiceRegistryEntry retreivedEntry = dm.get(ServiceRegistryEntry.class, restrictionMap);
    if (retreivedEntry == null) {
      log.info("updateServiceRegistryEntry throws DataNotFoundException");
      throw new DataNotFoundException("Requested Service Registry entry not found in the database.");
    }
    retreivedEntry.setServiceURI(entry.getServiceURI());
    if (entry.getPort() > 0) {
      retreivedEntry.setPort(entry.getPort());
    }
    retreivedEntry.setMetadata(entry.getMetadata());
    retreivedEntry.setEndOfValidity(entry.getEndOfValidity());
    retreivedEntry = dm.merge(retreivedEntry);

    log.info("updateServiceRegistryEntry successfully returns.");
    return Response.status(Status.ACCEPTED).entity(retreivedEntry).build();
  }

}
