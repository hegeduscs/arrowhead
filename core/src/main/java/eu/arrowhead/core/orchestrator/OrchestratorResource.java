package eu.arrowhead.core.orchestrator;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import eu.arrowhead.common.model.messages.OrchestrationResponse;
import eu.arrowhead.common.model.messages.ServiceRequestForm;

/**
 * @author pardavib, mereszd
 *
 */
@Path("orchestrator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrchestratorResource {

	private OrchestratorService orchestratorService;

	/**
	 * This function catches all GET requests made to the orchestrator URIs.
	 * 
	 * @return Response
	 */
	@GET
	public Response getOrchestration() {
		return Response.status(Status.BAD_REQUEST).build();
	}
	
	@GET
	@Path("stringtest")
	public String stringTest(){
		String ret;
		ret = "OrchestratorUri: " + orchestratorService.sysConfig.getOrchestratorURI() + "\n AuthorizationUri: "
				+ orchestratorService.sysConfig.getAuthorizationURI() + "\n ServiceRegistryUri: " +
				orchestratorService.sysConfig.getServiceRegistryURI() + "\n GateKeeperUri: " + 
				orchestratorService.sysConfig.getGatekeeperURI();
		return ret;
	}

	/**
	 * This function represents the main orchestration process initiated by the
	 * consumer.
	 * 
	 * @return Response
	 */
	@POST
	@Path("/orchestration")
	public Response postOrchestration(@Context UriInfo uriInfo, ServiceRequestForm serviceRequestForm) {

		Boolean isInterCloud;
		OrchestrationResponse orchResponse;

		// Checking the existence of expected request payload
		if (serviceRequestForm == null) {
			return Response.status(Status.BAD_REQUEST).build();
		} else {
			orchestratorService = new OrchestratorService(serviceRequestForm);
			isInterCloud = orchestratorService.isInterCloud();
		}

		// Deciding on local or inter-cloud orchestration
		if (isInterCloud) {
			orchResponse = orchestratorService.localOrchestration();
		} else {
			orchResponse = orchestratorService.intercloudOrchestration();
		}

		// Returning response if everything is OK.
		if (orchResponse != null) {
			return Response.status(Status.OK).entity(orchResponse).build();
		}

		// Returning error in case of a problem.
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}
}
