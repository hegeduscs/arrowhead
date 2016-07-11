package eu.arrowhead.core.orchestrator;

import java.util.HashMap;
import java.util.Map;

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

import org.apache.log4j.Logger;

import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.OrchestrationResponse;
import eu.arrowhead.common.model.messages.ServiceRequestForm;

/**
 * @author pardavib, mereszd
 *
 */

//TODO should make 

@Path("orchestrator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OldOrchestratorResource {

	private OldOrchestratorService orchestratorService;
	private static Logger log = Logger.getLogger(OldOrchestratorResource.class.getName());

	/**
	 * This function catches all GET requests made to the orchestrator URIs.
	 * 
	 * @return Response
	 */
	@GET
	public Response getOrchestration() {
		log.info("Orchestrator cannot be reached through GET methods.");
		return Response.status(Status.BAD_REQUEST).build();
	}

	@GET
	@Path("/example")
	@Produces(MediaType.TEXT_PLAIN)
	public Response example() {
		//TODO new SRF
		//TODO remove QoS interaction
		ArrowheadService requestedService = new ArrowheadService();
		ArrowheadSystem requesterSystem = new ArrowheadSystem();
		Map<String, Boolean> orchestrationFlags = new HashMap<>();
		orchestrationFlags.put("matchmaking", false);
		orchestrationFlags.put("externalServiceRequest", false);
		orchestrationFlags.put("triggerInterCloud", false);
		orchestrationFlags.put("metadataSearch", false);
		orchestrationFlags.put("pingProvider", false);
		Response resp = Response.status(Status.OK)
				.entity(new ServiceRequestForm(requestedService, "requestedQoS", requesterSystem, orchestrationFlags, null))
				.build();

		// return resp.readEntity(ServiceRequestForm.class).toString();
		return resp;
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

		//TODO new orch process
		Boolean isInterCloud;
		OrchestrationResponse orchResponse;

		log.info("Entering orchestration process.");

		// Checking the existence of expected request payload
		if (serviceRequestForm == null) {
			log.info("ServiceRequestForm not found in request payload.");
			return Response.status(Status.BAD_REQUEST).build();
		} else {
			log.info("Creating SRF from payload: " + serviceRequestForm.toString());
			orchestratorService = new OldOrchestratorService(serviceRequestForm);
			log.info("SRF created.");
			isInterCloud = orchestratorService.isInterCloud();
			log.info("isIntercloud value is: " + isInterCloud.toString());
		}

		// Deciding on local or inter-cloud orchestration
		if (isInterCloud) {
			log.info("Intercloud orchestration process initiated.");
			orchResponse = orchestratorService.intercloudOrchestration();

		} else {
			log.info("Local orchestration process initiated.");
			orchResponse = orchestratorService.localOrchestration();
		}

		// Returning response if everything is OK.
		if (orchResponse != null) {
			log.info("Orchestration process finished successfully.");
			return Response.status(Status.OK).entity(orchResponse).build();
		}
		// Returning error in case of a problem.
		log.info("Error occured during orchestration process.");
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}
}
