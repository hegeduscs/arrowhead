package eu.arrowhead.core.serviceregistry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("serviceregistry")
public class ServiceRegistryResource {
	
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "This is the Service Registry.";
    }
}
