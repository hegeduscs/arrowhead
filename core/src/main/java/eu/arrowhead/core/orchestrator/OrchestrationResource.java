package eu.arrowhead.core.orchestrator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("orchestration")
public class OrchestrationResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "This is the Orchestration Service!";
    }
    
    /*
     * Testing communication between core systems
     */
    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public String testMethod(){
    	Client client = ClientBuilder.newClient();
    	
    	Response response = client.target("http://localhost:8080/core/authorization/operator/A/cloud/b")
    			.request().get();
    	String message = response.readEntity(String.class);
    	
    	return message;
    }
}
