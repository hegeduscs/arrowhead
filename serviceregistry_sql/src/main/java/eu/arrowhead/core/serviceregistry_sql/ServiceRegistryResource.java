package eu.arrowhead.core.serviceregistry_sql;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.messages.ServiceQueryForm;
import eu.arrowhead.common.messages.ServiceQueryResult;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

@Path("serviceregistry")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ServiceRegistryResource {

  private static Logger log = Logger.getLogger(ServiceRegistryResource.class.getName());
  private HashMap<String, Object> restrictionMap = new HashMap<>();
  static DatabaseManager dm = DatabaseManager.getInstance();

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getIt() {
    return "This is the Service Registry Arrowhead Core System.";
  }


  @POST
  @Path("register")
  public Response registerService(ServiceRegistryEntry entry) {
    if (!entry.isValidFully()) {
      log.error("registerService throws BadPayloadException");
      throw new BadPayloadException("Bad payload: ServiceRegistryEntry has missing/incomplete mandatory field(s).");
    }

    ServiceRegistryEntry savedEntry = dm.save(entry);
    return Response.status(Status.CREATED).entity(savedEntry).build();

  }

  @PUT
  @Path("query")
  public Response queryRegistry(ServiceQueryForm queryForm) {
    if (!queryForm.isValid()) {
      log.error("queryRegistry throws BadPayloadException");
      throw new BadPayloadException("Bad payload: ServiceQueryForm has missing/incomplete mandatory field(s).");
    }

    restrictionMap.put("serviceGroup", queryForm.getService().getServiceGroup());
    restrictionMap.put("serviceDefinition", queryForm.getService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      log.info("Service " + queryForm.getService().toString() + " is not in the registry.");
      return Response.status(Status.NO_CONTENT).entity(new ServiceQueryResult()).build();
    }

    restrictionMap.clear();
    restrictionMap.put("providedService", service);
    List<ServiceRegistryEntry> providedServices = dm.getAll(ServiceRegistryEntry.class, restrictionMap);

    //TODO version filter (?)

    if (queryForm.isMetadataSearch()) {
      RegistryUtils.filterOnMeta(providedServices, queryForm.getService().getServiceMetadata());
    }
    if (queryForm.isPingProviders()) {
      RegistryUtils.filterOnPing(providedServices);
    }

    return Response.status(Status.OK).entity(providedServices).build();
  }

  @PUT
  @Path("remove")
  public Response removeService(ServiceRegistryEntry entry) {
    if (!entry.isValidFully()) {
      log.error("removeService throws BadPayloadException");
      throw new BadPayloadException("Bad payload: ServiceRegistryEntry has missing/incomplete mandatory field(s).");
    }

    dm.delete(entry);
    return Response.status(Status.OK).entity(entry).build();
  }

  //TODO do more variations (list args, delete all for a provider, etc)

}
