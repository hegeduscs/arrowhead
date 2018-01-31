/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.serviceregistry_sql.support;

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.json.support.ServiceRegistryEntrySupport;
import eu.arrowhead.core.serviceregistry_sql.ServiceRegistryResource;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("serviceregistry/support")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OldServiceRegResource {

  @POST
  @Path("register")
  public Response registerServiceSupport(ServiceRegistryEntrySupport supportEntry, @Context ContainerRequestContext requestContext) {
    ArrowheadService service = new ArrowheadService(supportEntry.getProvidedService());
    ArrowheadSystem system = new ArrowheadSystem(supportEntry.getProvider());
    ServiceRegistryEntry entry = new ServiceRegistryEntry(service, system, system.getPort(), supportEntry.getServiceURI());

    ServiceRegistryResource serviceRegResource = new ServiceRegistryResource();
    Response response = serviceRegResource.registerService(entry, requestContext);
    ServiceRegistryEntrySupport savedSupportEntry = new ServiceRegistryEntrySupport((ServiceRegistryEntry) response.getEntity());
    return Response.status(response.getStatus()).entity(savedSupportEntry).build();
  }

  @PUT
  @Path("remove")
  public Response removeServiceSupport(ServiceRegistryEntrySupport supportEntry, @Context ContainerRequestContext requestContext) {
    ArrowheadService service = new ArrowheadService(supportEntry.getProvidedService());
    ArrowheadSystem system = new ArrowheadSystem(supportEntry.getProvider());
    ServiceRegistryEntry entry = new ServiceRegistryEntry(service, system, system.getPort(), supportEntry.getServiceURI());

    ServiceRegistryResource serviceRegResource = new ServiceRegistryResource();
    Response response = serviceRegResource.removeService(entry, requestContext);
    ServiceRegistryEntrySupport savedSupportEntry = new ServiceRegistryEntrySupport((ServiceRegistryEntry) response.getEntity());
    return Response.status(response.getStatus()).entity(savedSupportEntry).build();
  }

}
