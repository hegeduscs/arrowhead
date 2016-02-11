package eu.arrowhead.core.orchestrator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.AuthorizationRequest;
import eu.arrowhead.common.model.messages.AuthorizationResponse;
import eu.arrowhead.common.model.messages.OrchestrationResponse;
import eu.arrowhead.common.model.messages.ProvidedService;
import eu.arrowhead.common.model.messages.QoSReserve;
import eu.arrowhead.common.model.messages.QoSVerificationResponse;
import eu.arrowhead.common.model.messages.QoSVerify;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;
import eu.arrowhead.common.model.messages.ServiceRequestForm;
import eu.arrowhead.core.orchestrator.services.OrchestrationService;

/**
 * @author pardavib
 *
 */
@Path("orchestrator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrchestrationResource {

	private OrchestrationService orchestrationService = new OrchestrationService();

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String welcome(@Context UriInfo uriInfo) {
		// return "Hello, this is the Orchestration Service.";
		return uriInfo.getBaseUriBuilder().path("serviceregistry").path("serviceregistry").build().toString();
	}

	/**
	 * This function represents the main orchestration process initiated by the
	 * consumer.
	 * 
	 * @return
	 */
	@POST
	@Path("/orchestration")
	public Response doOrchestration(@Context UriInfo uriInfo, ServiceRequestForm srForm) {
		ServiceQueryForm srvQueryForm = new ServiceQueryForm(srForm);
		ServiceQueryResult srvQueryResult;
		AuthorizationRequest authRequest;
		AuthorizationResponse authResponse;
		List<ArrowheadSystem> providers = new ArrayList<ArrowheadSystem>();
		QoSVerify qosVerification = new QoSVerify();
		QoSVerificationResponse qosVerificationResponse;
		QoSReserve qosReservation = new QoSReserve();
		boolean qosReservationResponse;
		URI uri;

		// Check for intercloud orchestration
		if (srForm.getOrchestrationFlags().get("TriggerInterCloud")) {
			doIntercloudOrchestration();
			return Response.status(Status.OK).entity(null).build();
		}

		// Poll the Service Registry
		uri = uriInfo.getBaseUriBuilder()
				.path("ServiceRegistry")
				.path(srForm.getRequestedService().getServiceGroup())
				.path("servicename") // HONNAN?
				.path(srForm.getRequestedService().getInterfaces().get(0))
				.build();
		srvQueryResult = getServiceQueryResult(srvQueryForm, uri);

		// Poll the Authorization Service
		uri = uriInfo.getBaseUriBuilder()
				.path("SystemGroup")
				.path(srForm.getRequestedService().getServiceGroup())
				.path("System")
				.path(srForm.getRequestedService().getInterfaces().get(0))
				.build();

		for (ProvidedService providedService : srvQueryResult.getServiceQueryData()) {
			providers.add(providedService.getProvider());
		}

		authRequest = new AuthorizationRequest(srForm.getRequestedService(), providers, "AuthenticationInfo", true);
		authResponse = getAuthorizationResponse(authRequest, uri);

		/*
		 * qosVerificationResponse =
		 * getQosVerificationResponse(qosVerification);
		 * 
		 * // TODO: Matchmaking
		 * 
		 * qosReservationResponse = doQosReservation(qosReservation);
		 * 
		 * // TODO: Compile orchestration response
		 * 
		 * // TODO: Send orchestration form
		 * 
		 * System.out.println("Orchestration process finished.");
		 */

		return Response.status(Status.OK).entity(null).build();
	}

	/**
	 * This function represents the Intercloud orchestration process.
	 */
	private void doIntercloudOrchestration() {
		return;
	}

	/**
	 * Sends the Service Query Form to the Service Registry and asks for the
	 * Service Query Result.
	 * 
	 * @param uriInfo
	 */
	private ServiceQueryResult getServiceQueryResult(ServiceQueryForm sqf, URI uri) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(uri);
		Response response = target.request().header("Content-type", "application/json").put(Entity.json(sqf));

		return response.readEntity(ServiceQueryResult.class);
	}

	/**
	 * Sends the Authorization Request to the Authorization service and asks for
	 * the Authorization Response.
	 */
	private AuthorizationResponse getAuthorizationResponse(AuthorizationRequest authRequest, URI uri) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(uri);
		Response response = target.request().header("Content-type", "application/json").put(Entity.json(authRequest));

		return response.readEntity(AuthorizationResponse.class);
	}

	/**
	 * Sends the QoS Verify message to the QoS service and asks for the QoS
	 * Verification Response.
	 */
	private QoSVerificationResponse getQosVerificationResponse(QoSVerify qosVerify, URI uri) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(uri);
		Response response = target.request().header("Content-type", "application/json").put(Entity.json(qosVerify));

		return response.readEntity(QoSVerificationResponse.class);
	}

	/**
	 * Sends QoS reservation to the QoS service.
	 * 
	 * @return boolean Reservation response
	 */
	private boolean doQosReservation(QoSReserve qosReserve, URI uri) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(uri);
		Response response = target.request().header("Content-type", "application/json").put(Entity.json(qosReserve));

		// return response.readEntity(Boolean.class);

		// Always true until QoS Service is added to the system.
		return true;
	}
}
