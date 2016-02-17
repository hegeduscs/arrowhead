package eu.arrowhead.core.orchestrator;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import eu.arrowhead.common.configuration.SysConfig;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.AuthorizationRequest;
import eu.arrowhead.common.model.messages.AuthorizationResponse;
import eu.arrowhead.common.model.messages.GSDRequestForm;
import eu.arrowhead.common.model.messages.GSDResult;
import eu.arrowhead.common.model.messages.ICNRequestForm;
import eu.arrowhead.common.model.messages.ICNResultForm;
import eu.arrowhead.common.model.messages.OrchestrationForm;
import eu.arrowhead.common.model.messages.OrchestrationResponse;
import eu.arrowhead.common.model.messages.ProvidedService;
import eu.arrowhead.common.model.messages.QoSReservationResponse;
import eu.arrowhead.common.model.messages.QoSReserve;
import eu.arrowhead.common.model.messages.QoSVerificationResponse;
import eu.arrowhead.common.model.messages.QoSVerify;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;
import eu.arrowhead.common.model.messages.ServiceRegistryEntry;
import eu.arrowhead.common.model.messages.ServiceRequestForm;
import eu.arrowhead.core.orchestrator.services.OrchestrationService;

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
