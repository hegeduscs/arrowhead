package eu.arrowhead.core.orchestrator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import eu.arrowhead.common.model.messages.ServiceRequestForm;

@Path("orchestration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NewOrchestratorResource {
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getIt() {
		return "Got it!";
	}
	
	@POST
	public Response orchestrationProcess(ServiceRequestForm srf){
		
		return null;
	}

}
