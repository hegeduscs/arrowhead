package eu.arrowhead.core.serviceregistry;

import com.github.danieln.dnssdjava.DnsSDException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DnsException;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;
import eu.arrowhead.common.model.messages.ServiceRegistryEntry;
import javax.ws.rs.core.Response.Status;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("serviceregistry")
public class ServiceRegistryResource {

    private static Logger log = Logger.getLogger(ServiceRegistryResource.class.getName());

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "This is the Service Registry.";
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
            throw new BadPayloadException("Bad payload: service request form has missing/incomplete " + "mandatory fields.");
        }

        //putting the service data into ArrowheadService
      entry.setProvidedService(new ArrowheadService());

      entry.getProvidedService().setServiceGroup(serviceGroup);
      entry.getProvidedService().setServiceDefinition(service);

      List<String> interfaces = new ArrayList<>();
      interfaces.add(interf);
      entry.getProvidedService().setInterfaces(interfaces);

      try {
          if (ServiceRegistry.register(entry))
              return Response.status(Response.Status.OK).build();
          else
              return Response.status(Response.Status.RESET_CONTENT).build();
      } catch (DnsSDException e) {
          log.error("SR Registration failed:" + e.getMessage());
          return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
      }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{serviceGroup}/{service}/{interf}")
    public Response removingFromRegistry(@PathParam("serviceGroup") String serviceGroup, @PathParam("service") String service,
                                         @PathParam("interf") String interf, ServiceRegistryEntry entry) {

        if (serviceGroup == null || service == null || interf == null || !entry.isValid()) {
            log.info("ServiceRegistry:Query throws BadPayloadException");
            throw new BadPayloadException("Bad payload: service request form has missing/incomplete " + "mandatory fields.");
        }

        //ArrowheadService will be empty in this case
        entry.setProvidedService(new ArrowheadService());
        entry.getProvidedService().setServiceDefinition(service);
        entry.getProvidedService().setServiceGroup(serviceGroup);
        entry.getProvidedService().setInterfaces(new ArrayList<String>());
        entry.getProvidedService().getInterfaces().add(interf);

        boolean result;

        try {
            result = ServiceRegistry.unRegister(entry);
        } catch (DnsException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (result)
            return Response.status(Response.Status.OK).build();
        else
            return Response.status(Response.Status.NO_CONTENT).build();
    }

    /*
        Interface towards the Orchestrator
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "query")
    public Response getServiceQueryForm(ServiceQueryForm queryForm) {

        if (queryForm==null || queryForm.isValid() == false) {
            log.info("ServiceRegistry:Query throws BadPayloadException");
            throw new BadPayloadException("Bad payload: the request form has missing/incomplete " + "mandatory fields.");
        }

        ServiceQueryResult sqr = ServiceRegistry.provideServices(queryForm);

        if (!sqr.getServiceQueryData().isEmpty())
            return Response.status(Response.Status.OK).entity(sqr).build();
        else
            return Response.status(Response.Status.NO_CONTENT).entity(sqr).build();
    }

    /**
     * Public function for checking all entries
     * @return All registered service
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/all")
    public Response getAllServices() {

        ServiceQueryResult result;

        try {
            result = ServiceRegistry.provideAllServices();
            if (result.getServiceQueryData().isEmpty())
                return Response.status(Status.NO_CONTENT).entity(result).build();
            else
                return Response.status(Response.Status.OK).entity(result).build();
        } catch (DnsSDException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Public function for removing all entries in the DNS.
     * @return Removes all registered service
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path(value = "/all")
    public Response removeAllServices() {

        if (ServiceRegistry.removeAllServices())
            return Response.status(Response.Status.OK).build();
        else
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
