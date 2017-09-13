package eu.arrowhead.core.serviceregistry_sql;

import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.messages.ServiceQueryForm;
import eu.arrowhead.common.messages.ServiceQueryResult;
import eu.arrowhead.common.messages.ServiceRegistryEntry;
import java.util.ArrayList;
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
import org.apache.log4j.Logger;

@Path("serviceregistry")
public class ServiceRegistryResource {

  private static Logger log = Logger.getLogger(ServiceRegistryResource.class.getName());

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the Service Registry.";
  }


  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("registration")
  public Response publishEntriesToRegistry(ServiceRegistryEntry entry) {
    if (entry == null || !entry.isValidFully()) {
      log.info("ServiceRegistry:Query throws BadPayloadException");
      throw new BadPayloadException("Bad payload: service registration form has missing/incomplete mandatory fields.");
    }

    //TODO register
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

  }

  /*
      Backwards compatibility
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/{serviceGroup}/{service}/{interf}")
  public Response publishingToRegistry(@PathParam("serviceGroup") String serviceGroup, @PathParam("service") String service,
                                       @PathParam("interf") String interf, ServiceRegistryEntry entry) {

    if (serviceGroup == null || service == null || interf == null || entry == null) {
      log.info("ServiceRegistry: Registration throws BadPayloadException");
      throw new BadPayloadException("Bad payload: service request form has missing/incomplete mandatory fields.");
    }

    //putting the service data into ArrowheadService
    entry.setProvidedService(new ArrowheadService());

    entry.getProvidedService().setServiceGroup(serviceGroup);
    entry.getProvidedService().setServiceDefinition(service);

    List<String> interfaces = new ArrayList<>();
    interfaces.add(interf);
    entry.getProvidedService().setInterfaces(interfaces);

    //TODO unregister
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("registration")
  public Response removeEntryFromRegistry(ServiceRegistryEntry entry) {
    if (entry == null || !entry.isValidFully()) {
      log.info("ServiceRegistry:Query throws BadPayloadException");
      throw new BadPayloadException("Bad payload: service registration form has missing/incomplete mandatory fields.");
    }

    boolean result = false;

    if (result) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }

  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("/{serviceGroup}/{service}/{interf}")
  public Response removingFromRegistry(@PathParam("serviceGroup") String serviceGroup, @PathParam("service") String service,
                                       @PathParam("interf") String interf, ServiceRegistryEntry entry) {

    if (serviceGroup == null || service == null || interf == null || !entry.isValid()) {
      log.info("ServiceRegistry:Query throws BadPayloadException");
      throw new BadPayloadException("Bad payload: service request form has missing/incomplete mandatory fields.");
    }

    //ArrowheadService will be empty in this case
    entry.setProvidedService(new ArrowheadService());
    entry.getProvidedService().setServiceDefinition(service);
    entry.getProvidedService().setServiceGroup(serviceGroup);
    entry.getProvidedService().setInterfaces(new ArrayList<String>());
    entry.getProvidedService().getInterfaces().add(interf);

    boolean result = false;

    if (result) {
      return Response.status(Response.Status.OK).build();
    } else {
      return Response.status(Response.Status.NO_CONTENT).build();
    }
  }

  /*
      Interface towards the Orchestrator
   */
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @Path(value = "query")
  public Response getServiceQueryForm(ServiceQueryForm queryForm) {

    if (queryForm == null || !queryForm.isValid()) {
      log.info("ServiceRegistry:Query throws BadPayloadException");
      throw new BadPayloadException("Bad payload: the request form has missing/incomplete mandatory fields.");
    }

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
  @Produces(MediaType.APPLICATION_JSON)
  @Path(value = "/all")
  public Response getAllServices() {

    ServiceQueryResult result;
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }

  /**
   * Public function for removing all entries in the DNS.
   *
   * @return Removes all registered service
   */
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Path(value = "/all")
  public Response removeAllServices() {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
  }
}
