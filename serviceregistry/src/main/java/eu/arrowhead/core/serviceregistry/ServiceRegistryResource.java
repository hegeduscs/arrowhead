package eu.arrowhead.core.serviceregistry;

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.messages.ServiceQueryForm;
import eu.arrowhead.common.messages.ServiceQueryResult;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the Service Registry.";
  }

  @POST
  @Path("register")
  public Response publishEntriesToRegistry(ServiceRegistryEntry entry, @Context ContainerRequestContext requestContext) {
    log.debug(
        "SR reg service: " + entry.getProvidedService().getServiceDefinition() + " provider: " + entry.getProvider().getSystemName() + " serviceURI: "
            + entry.getServiceURI());
    entry.missingFields(true, true, new HashSet<>(Arrays.asList("interfaces", "address")));

    try {
      if (ServiceRegistry.register(entry)) {
        return Response.status(Response.Status.OK).build();
      } else {
        return Response.status(Response.Status.RESET_CONTENT).build();
      }
    } catch (Exception e) {
      log.error("SR Registration failed:" + e.getMessage());
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PUT
  @Path("remove")
  public Response removeEntriesFromRegistry(ServiceRegistryEntry entry, @Context ContainerRequestContext requestContext) {
    log.debug("SR remove service: " + entry.getProvidedService().getServiceDefinition() + " provider: " + entry.getProvider().getSystemName()
                  + " serviceURI: " + entry.getServiceURI());
    entry.missingFields(true, true, null);

    if (ServiceRegistry.unRegister(entry)) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /*
     Backwards compatibility
   */
  @POST
  @Path("{service}/{interf}")
  public Response publishingToRegistry(@PathParam("service") String service, @PathParam("interf") String interf, ServiceRegistryEntry entry) {

    if (service == null || interf == null || entry == null) {
      log.info("publishingToRegistry throws BadPayloadException");
      throw new BadPayloadException("Bad payload: service request form has missing/incomplete mandatory fields.");
    }

    entry.setProvidedService(new ArrowheadService(service, Collections.singletonList(interf), null));

    if (ServiceRegistry.register(entry)) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.RESET_CONTENT).build();
    }
  }

  /*
     Backwards compatibility
   */
  @PUT
  @Path("{service}/{interf}")
  public Response removingFromRegistry(@PathParam("service") String service, @PathParam("interf") String interf, ServiceRegistryEntry entry) {

    if (service == null || interf == null || entry == null) {
      log.info("removingFromRegistry throws BadPayloadException");
      throw new BadPayloadException("Bad payload: service de-registration form has missing/incomplete mandatory fields.");
    }

    entry.setProvidedService(new ArrowheadService(service, Collections.singletonList(interf), null));

    if (ServiceRegistry.unRegister(entry)) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /*
      Interface towards the Orchestrator
   */
  @PUT
  @Path("query")
  public Response getServiceQueryForm(ServiceQueryForm queryForm) {
    queryForm.missingFields(true, null);

    ServiceQueryResult sqr = ServiceRegistry.provideServices(queryForm);
    if (!sqr.getServiceQueryData().isEmpty()) {
      return Response.status(Response.Status.OK).entity(sqr).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).entity(sqr).build();
    }
  }

  /**
   * Public function for checking all entries
   *
   * @return All registered service
   */
  @GET
  @Path("all")
  public Response getAllServices() {
    ServiceQueryResult result = ServiceRegistry.provideAllServices();
    if (result.getServiceQueryData().isEmpty()) {
      return Response.status(Status.NO_CONTENT).entity(result).build();
    } else {
      return Response.status(Response.Status.OK).entity(result).build();
    }
  }

  /**
   * Public function for removing all entries in the DNS.
   *
   * @return Removes all registered service
   */
  @DELETE
  @Path("all")
  public Response removeAllServices() {
    if (ServiceRegistry.removeAllServices()) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

}
