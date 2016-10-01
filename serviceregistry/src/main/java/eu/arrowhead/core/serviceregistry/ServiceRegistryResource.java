package eu.arrowhead.core.serviceregistry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;
import eu.arrowhead.common.model.messages.ServiceRegistryEntry;

@Path("serviceregistry")
public class ServiceRegistryResource {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "This is the Service Registry.";
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{serviceGroup}/{service}/{interf}")
	public void publishingToRegistry(@PathParam("serviceGroup") String serviceGroup,
			@PathParam("service") String service, @PathParam("interf") String interf, ServiceRegistryEntry entry) {
		ServiceRegistry.getInstance().register(serviceGroup, service, interf, entry);
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{serviceGroup}/{service}/{interf}")
	public void removingFromRegistry(@PathParam("serviceGroup") String serviceGroup,
			@PathParam("service") String service, @PathParam("interf") String interf, ServiceRegistryEntry entry) {
		ServiceRegistry.getInstance().unRegister(serviceGroup, service, interf, entry);
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
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Path(value = "/{serviceGroup}/{service}")
	public ServiceQueryResult getServiceQueryForm(@PathParam("serviceGroup") String serviceGroup,
			@PathParam("service") String service, ServiceQueryForm queryForm) {
		return ServiceRegistry.getInstance().provideServices(serviceGroup, service, queryForm);
	}

	/**
	 * 
	 * @return All registered service
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(value = "/all")
	public ServiceQueryResult getAllServices() {
		return ServiceRegistry.getInstance().provideAllServices();
	}
}
