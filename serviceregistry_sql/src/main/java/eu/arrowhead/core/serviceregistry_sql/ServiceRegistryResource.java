package eu.arrowhead.core.serviceregistry_sql;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.json.supportadapter.ArrowheadServiceSupport;
import eu.arrowhead.common.json.supportadapter.ServiceRegistryEntrySupport;
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
    if (!entry.isValidFully()) {
      log.error("registerService throws BadPayloadException");
      throw new BadPayloadException("ServiceRegistryEntry has missing/incomplete mandatory field(s).", Status.BAD_REQUEST.getStatusCode(),
                                    BadPayloadException.class.getName(), requestContext.getUriInfo().getAbsolutePath().toString());
    }
    if (requestContext.getSecurityContext().isSecure()) {
      String subjectName = requestContext.getSecurityContext().getUserPrincipal().getName();
      String clientCN = SecurityUtils.getCertCNFromSubject(subjectName);
      String[] clientFields = clientCN.split("\\.", 3);
      if (!entry.getProvider().getSystemName().equalsIgnoreCase(clientFields[0]) || !entry.getProvider().getSystemGroup()
          .equalsIgnoreCase(clientFields[1])) {
        log.error("Provider system fields and cert common name do not match! Service registering denied.");
        throw new AuthenticationException(
            "Provider system " + entry.getProvider().toStringLog() + " fields and cert common name (" + clientCN + ") do not match!",
            Status.UNAUTHORIZED.getStatusCode(), AuthenticationException.class.getName(), requestContext.getUriInfo().getAbsolutePath().toString());
      }
    }

    restrictionMap.put("serviceGroup", entry.getProvidedService().getServiceGroup());
    restrictionMap.put("serviceDefinition", entry.getProvidedService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);
    if (service == null) {
      service = dm.save(entry.getProvidedService());
    } else {
      service.setInterfaces(entry.getProvidedService().getInterfaces());
      service.setServiceMetadata(entry.getProvidedService().getServiceMetadata());
      dm.merge(service);
    }
    entry.setProvidedService(service);

    restrictionMap.clear();
    restrictionMap.put("systemGroup", entry.getProvider().getSystemGroup());
    restrictionMap.put("systemName", entry.getProvider().getSystemName());
    ArrowheadSystem provider = dm.get(ArrowheadSystem.class, restrictionMap);
    if (provider == null) {
      provider = dm.save(entry.getProvider());
    } else {
      provider.setAddress(entry.getProvider().getAddress());
      provider.setPort(entry.getProvider().getPort());
      dm.merge(provider);
    }
    entry.setProvider(provider);

    ServiceRegistryEntry savedEntry = dm.save(entry);
    log.info("New ServiceRegistryEntry " + entry.toString() + " is saved.");
    return Response.status(Status.CREATED).entity(savedEntry).build();
  }

  @POST
  @Path("register/support")
  public Response registerServiceSupport(ServiceRegistryEntrySupport supportEntry, @Context ContainerRequestContext requestContext) {
    ArrowheadServiceSupport supportService = supportEntry.getProvidedService();
    ArrowheadService service = new ArrowheadService(supportService.getServiceGroup(), supportService.getServiceDefinition(),
                                                    supportService.getInterfaces(), supportService.getServiceMetadata());
    ServiceRegistryEntry entry = new ServiceRegistryEntry(service, supportEntry.getProvider(), supportEntry.getServiceURI());

    Response response = registerService(entry, requestContext);
    ServiceRegistryEntrySupport savedSupportEntry = new ServiceRegistryEntrySupport((ServiceRegistryEntry) response.getEntity());
    return Response.status(Status.CREATED).entity(savedSupportEntry).build();
  }

  @PUT
  @Path("query")
  public Response queryRegistry(ServiceQueryForm queryForm, @Context ContainerRequestContext requestContext) {
    if (!queryForm.isValid()) {
      log.error("queryRegistry throws BadPayloadException");
      throw new BadPayloadException("ServiceQueryForm has missing/incomplete mandatory field(s).", Status.BAD_REQUEST.getStatusCode(),
                                    BadPayloadException.class.getName(), requestContext.getUriInfo().getAbsolutePath().toString());
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
    if (!entry.isValidFully()) {
      log.error("removeService throws BadPayloadException");
      throw new BadPayloadException("Bad payload: ServiceRegistryEntry has missing/incomplete mandatory field(s).",
                                    Status.BAD_REQUEST.getStatusCode(), BadPayloadException.class.getName(),
                                    requestContext.getUriInfo().getAbsolutePath().toString());
    }
    if (requestContext.getSecurityContext().isSecure()) {
      String subjectName = requestContext.getSecurityContext().getUserPrincipal().getName();
      String clientCN = SecurityUtils.getCertCNFromSubject(subjectName);
      String[] clientFields = clientCN.split("\\.", 3);
      if (!entry.getProvider().getSystemName().equalsIgnoreCase(clientFields[0]) || !entry.getProvider().getSystemGroup()
          .equalsIgnoreCase(clientFields[1])) {
        log.error("Provider system fields and cert common name do not match! Service removing denied.");
        throw new AuthenticationException(
            "Provider system " + entry.getProvider().toStringLog() + " fields and cert common name (" + clientCN + ") do not match!",
            Status.UNAUTHORIZED.getStatusCode(), AuthenticationException.class.getName(), requestContext.getUriInfo().getAbsolutePath().toString());
      }
    }

    restrictionMap.put("serviceGroup", entry.getProvidedService().getServiceGroup());
    restrictionMap.put("serviceDefinition", entry.getProvidedService().getServiceDefinition());
    ArrowheadService service = dm.get(ArrowheadService.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("systemGroup", entry.getProvider().getSystemGroup());
    restrictionMap.put("systemName", entry.getProvider().getSystemName());
    ArrowheadSystem provider = dm.get(ArrowheadSystem.class, restrictionMap);

    restrictionMap.clear();
    restrictionMap.put("providedService", service);
    restrictionMap.put("provider", provider);
    ServiceRegistryEntry retrievedEntry = dm.get(ServiceRegistryEntry.class, restrictionMap);
    if (retrievedEntry != null) {
      dm.delete(retrievedEntry);
      log.info("ServiceRegistryEntry " + retrievedEntry.toString() + " deleted.");
      return Response.status(Status.OK).entity(retrievedEntry).build();
    } else {
      log.info("ServiceRegistryEntry " + entry.toString() + " was not found in the SR to delete.");
      return Response.status(Status.NO_CONTENT).entity(entry).build();
    }
  }

  @PUT
  @Path("remove/support")
  public Response removeServiceSupport(ServiceRegistryEntrySupport supportEntry, @Context ContainerRequestContext requestContext) {
    ArrowheadServiceSupport supportService = supportEntry.getProvidedService();
    ArrowheadService service = new ArrowheadService(supportService.getServiceGroup(), supportService.getServiceDefinition(),
                                                    supportService.getInterfaces(), supportService.getServiceMetadata());
    ServiceRegistryEntry entry = new ServiceRegistryEntry(service, supportEntry.getProvider(), supportEntry.getServiceURI());

    Response response = removeService(entry, requestContext);
    ServiceRegistryEntrySupport savedSupportEntry = new ServiceRegistryEntrySupport((ServiceRegistryEntry) response.getEntity());
    return Response.status(Status.NO_CONTENT).entity(savedSupportEntry).build();
  }

}
