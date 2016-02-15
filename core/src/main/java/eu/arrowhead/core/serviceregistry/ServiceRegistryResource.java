package eu.arrowhead.core.serviceregistry;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import eu.arrowhead.common.model.serviceregistry.Provider;
import eu.arrowhead.common.model.serviceregistry.ServiceRegistryEntry;


@Path("serviceregistry")
public class ServiceRegistryResource {
	
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "This is the Service Registry.";
    }
    
    @PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{serviceGroup}/{service}/{interf}/")
	public void publishingToRegistry(@PathParam("serviceGroup") String serviceGroup, @PathParam("service") String service,
			@PathParam("interf") String interf, ServiceRegistryEntry entry) {
		ServiceRegistry.getInstance().register(serviceGroup, service, interf, entry);
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{serviceGroup}/{service}/{interf}")
	public void removingFromRegistry(@PathParam("serviceGroup") String serviceGroup, @PathParam("service") String service,
			@PathParam("interf") String interf) {
		ServiceRegistry.getInstance().unRegister(serviceGroup, service, interf);
	}

	/**
	 * Query paramaters are the fields from Service
	 * 
	 * Query Form GET-be query parameternek egy string lista kell IDD-k
	 * 
	 * @param serviceGroup
	 * @param service
	 * @param interf
	 * @param serviceMetadata
	 * @param pingProviders
	 * @return Providers: List of (ArrowheadSystem, ServiceURI:String)
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(value = "/{serviceGroup}/{service}")
	public List<Provider> getServiceQueryForm(@PathParam("serviceGroup") String serviceGroup,
			@PathParam("service") String service, @QueryParam("ServiceMetadata") String serviceMetadata,
			@QueryParam("PingProviders") boolean pingProviders, @QueryParam("ServiceInterfaces") List<String> serviceInterfaces,
			@QueryParam("TSIG_key") String tSIG_key) {
		return ServiceRegistry.getInstance().provideServices(serviceGroup, service, serviceMetadata, pingProviders,
				serviceInterfaces, tSIG_key);
	}
}
