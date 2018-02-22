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
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.messages.ServiceQueryForm;
import eu.arrowhead.common.messages.ServiceQueryResult;
import eu.arrowhead.common.security.SecurityUtils;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

@Path("serviceregistry")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ServiceRegistryResource {

  private static final Logger log = Logger.getLogger(ServiceRegistryResource.class.getName());
  private final HashMap<String, Object> restrictionMap = new HashMap<>();
  static final DatabaseManager dm = DatabaseManager.getInstance();

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the Service Registry Arrowhead Core System.";
  }

  @POST
  @Path("register")
  public Response registerService(ServiceRegistryEntry entry, @Context ContainerRequestContext requestContext) {
    log.debug("SR reg service: " + entry.getProvidedService() + " provider: " + entry.getProvider() + " serviceURI: " + entry.getServiceURI());
    if (!entry.isValid()) {
      log.error("registerService throws BadPayloadException");
      throw new BadPayloadException("ServiceRegistryEntry has missing/incomplete mandatory field(s).", Status.BAD_REQUEST.getStatusCode(),
                                    BadPayloadException.class.getName(), requestContext.getUriInfo().getAbsolutePath().toString());
    }

    entry.toDatabase();

    restrictionMap.put("serviceDefinition", entry.getProvidedService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      service = dm.save(entry.getProvidedService());
    } else {
      service.setInterfaces(entry.getProvidedService().getInterfaces());
      dm.merge(service);
    }
    entry.setProvidedService(service);

    restrictionMap.clear();
    restrictionMap.put("systemName", entry.getProvider().getSystemName());
    ArrowheadSystem provider = dm.get(ArrowheadSystem.class, restrictionMap);
    if (provider == null) {
      provider = dm.save(entry.getProvider());
    } else {
      provider.setAddress(entry.getProvider().getAddress());
      provider.setAuthenticationInfo(entry.getProvider().getAuthenticationInfo());
      dm.merge(provider);
    }
    entry.setProvider(provider);

    ServiceRegistryEntry savedEntry = dm.save(entry);
    savedEntry.fromDatabase();
    log.info("New ServiceRegistryEntry " + entry.toString() + " is saved.");
    return Response.status(Status.CREATED).entity(savedEntry).build();
  }

  @PUT
  @Path("query")
  public Response queryRegistry(ServiceQueryForm queryForm, @Context ContainerRequestContext requestContext) {
    if (!queryForm.isValid()) {
      log.error("queryRegistry throws BadPayloadException");
      throw new BadPayloadException("ServiceQueryForm has missing/incomplete mandatory field(s).", Status.BAD_REQUEST.getStatusCode(),
                                    BadPayloadException.class.getName(), requestContext.getUriInfo().getAbsolutePath().toString());
    }

    restrictionMap.put("serviceDefinition", queryForm.getService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("Service " + queryForm.getService().toString() + " is not in the registry.");
      return Response.status(Status.NO_CONTENT).entity(new ServiceQueryResult()).build();
    }

    restrictionMap.clear();
    restrictionMap.put("providedService", service);
    List<ServiceRegistryEntry> providedServices = dm.getAll(ServiceRegistryEntry.class, restrictionMap);
    for (ServiceRegistryEntry entry : providedServices) {
      entry.fromDatabase();
    }

    //NOTE add version filter too later, if deemed needed
    if (queryForm.isMetadataSearch()) {
      RegistryUtils.filterOnMeta(providedServices, queryForm.getService().getServiceMetadata());
    }
    if (queryForm.isPingProviders()) {
      RegistryUtils.filterOnPing(providedServices);
    }

    log.info("Service " + queryForm.getService().toString() + " queried successfully.");
    ServiceQueryResult result = new ServiceQueryResult(providedServices);
    return Response.status(Status.OK).entity(result).build();
  }

  @PUT
  @Path("remove")
  public Response removeService(ServiceRegistryEntry entry, @Context ContainerRequestContext requestContext) {
    log.debug("SR remove service: " + entry.getProvidedService() + " provider: " + entry.getProvider() + " serviceURI: " + entry.getServiceURI());
    if (!entry.isValid()) {
      log.error("removeService throws BadPayloadException");
      throw new BadPayloadException("Bad payload: ServiceRegistryEntry has missing/incomplete mandatory field(s).",
                                    Status.BAD_REQUEST.getStatusCode(), BadPayloadException.class.getName(),
                                    requestContext.getUriInfo().getAbsolutePath().toString());
    }
    if (requestContext.getSecurityContext().isSecure()) {
      String subjectName = requestContext.getSecurityContext().getUserPrincipal().getName();
      String clientCN = SecurityUtils.getCertCNFromSubject(subjectName);
      String[] clientFields = clientCN.split("\\.", 2);
      if (!entry.getProvider().getSystemName().equalsIgnoreCase(clientFields[0])) {
        log.error("Provider system name and cert common name do not match! Service removing denied.");
        throw new AuthenticationException(
            "Provider system " + entry.getProvider().getSystemName() + " and cert common name (" + clientCN + ") do not match!",
            Status.UNAUTHORIZED.getStatusCode(), AuthenticationException.class.getName(), requestContext.getUriInfo().getAbsolutePath().toString());
      }
    }

    restrictionMap.put("serviceDefinition", entry.getProvidedService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("systemName", entry.getProvider().getSystemName());
    ArrowheadSystem provider = dm.get(ArrowheadSystem.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("providedService", service);
    restrictionMap.put("provider", provider);
    ServiceRegistryEntry retrievedEntry = dm.get(ServiceRegistryEntry.class, restrictionMap);
    if (retrievedEntry != null) {
      dm.delete(retrievedEntry);
      retrievedEntry.fromDatabase();
      log.info("ServiceRegistryEntry " + retrievedEntry.toString() + " deleted.");
      return Response.status(Status.OK).entity(retrievedEntry).build();
    } else {
      log.info("ServiceRegistryEntry " + entry.toString() + " was not found in the SR to delete.");
      return Response.status(Status.NO_CONTENT).entity(entry).build();
    }
  }

}
