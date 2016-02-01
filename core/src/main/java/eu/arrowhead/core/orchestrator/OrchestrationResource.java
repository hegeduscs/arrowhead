package eu.arrowhead.core.orchestrator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("orchestration")
public class OrchestrationResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "This is the Orchestration Service";
    }
}
