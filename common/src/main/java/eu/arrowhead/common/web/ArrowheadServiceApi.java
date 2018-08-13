package eu.arrowhead.common.web;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.exception.DataNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

@Path("mgmt/services")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArrowheadServiceApi {

  private final HashMap<String, Object> restrictionMap = new HashMap<>();
  private static final Logger log = Logger.getLogger(ArrowheadService.class.getName());
  private static final DatabaseManager dm = DatabaseManager.getInstance();

  @GET
  public List<ArrowheadService> getServices(@QueryParam("definition") String definition) {
    if (definition != null) {
      restrictionMap.put("serviceDefinition", definition);
    }

    List<ArrowheadService> serviceList = dm.getAll(ArrowheadService.class, restrictionMap);
    if (serviceList.isEmpty()) {
      log.info("getServices throws DataNotFoundException");
      throw new DataNotFoundException("ArrowheadServices not found in the database.");
    }

    return serviceList;
  }

  @GET
  @Path("{serviceId}")
  public ArrowheadService getService(@PathParam("serviceId") long serviceId) {
    return dm.get(ArrowheadService.class, serviceId)
             .orElseThrow(() -> new DataNotFoundException("ArrowheadService entry not found with id: " + serviceId));
  }

  @POST
  public Response addServices(@Valid List<ArrowheadService> serviceList) {

    List<ArrowheadService> savedServices = new ArrayList<>();
    for (ArrowheadService service : serviceList) {
      restrictionMap.clear();
      restrictionMap.put("serviceDefinition", service.getServiceDefinition());
      ArrowheadService retrievedService = dm.get(ArrowheadService.class, restrictionMap);
      if (retrievedService == null) {
        dm.save(service);
        savedServices.add(service);
      }
    }

    if (savedServices.isEmpty()) {
      return Response.status(Status.NO_CONTENT).build();
    } else {
      return Response.status(Status.CREATED).entity(savedServices).build();
    }
  }

  @PUT
  public Response updateService(@Valid ArrowheadService service) {
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

  @PUT
  @Path("{serviceId}")
  public Response updateService(@PathParam("serviceId") long serviceId, @Valid ArrowheadService service) {
    ArrowheadService retrievedService = dm.get(ArrowheadService.class, serviceId)
                                          .orElseThrow(() -> new DataNotFoundException("ArrowheadService entry not found with id: " + serviceId));
    retrievedService.partialUpdate(service);
    retrievedService = dm.merge(retrievedService);
    return Response.status(Status.ACCEPTED).entity(retrievedService).build();
  }

  @DELETE
  @Path("{serviceId}")
  public Response deleteService(@PathParam("serviceId") long serviceId) {
    return dm.get(ArrowheadService.class, serviceId).map(service -> {
      dm.delete(service);
      log.info("deleteService successfully returns.");
      return Response.ok().build();
    }).<DataNotFoundException>orElseThrow(() -> {
      log.info("deleteService had no effect.");
      throw new DataNotFoundException("ArrowheadService entry not found with id: " + serviceId);
    });
  }
}
