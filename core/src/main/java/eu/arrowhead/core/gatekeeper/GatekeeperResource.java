package eu.arrowhead.core.gatekeeper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


@Path("gatekeeper")
public class GatekeeperResource {
	
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "This is the Gatekeeper Resource stub.";
    }
}
