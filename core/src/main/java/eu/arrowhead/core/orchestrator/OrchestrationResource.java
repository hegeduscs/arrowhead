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
 * @author pardavib
 *
 */
@Path("orchestrator")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrchestrationResource {
	// private SysConfig sysConfig = SysConfig.getInstance();
	URI uri = null;
	Client client = ClientBuilder.newClient();

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
		QoSVerify qosVerification;
		QoSVerificationResponse qosVerificationResponse;
		Map<ArrowheadSystem, Boolean> qosMap;
		ArrowheadSystem selectedSystem = null;
		QoSReserve qosReservation;
		QoSReservationResponse qosReservationResponse;
		OrchestrationForm orchForm;
		OrchestrationResponse orchResponse;
		ArrayList<OrchestrationForm> responseFormList = new ArrayList<OrchestrationForm>();

		// Check for intercloud orchestration
		if (srForm.getOrchestrationFlags().get("TriggerInterCloud")) {
			orchResponse = doIntercloudOrchestration(srForm);
			return Response.status(Status.OK).entity(orchResponse).build();
		}
		// Poll the Service Registry
		srvQueryResult = getServiceQueryResult(srvQueryForm, srForm);
		for (ProvidedService providedService : srvQueryResult.getServiceQueryData()) {
			providers.add(providedService.getProvider());
		}
		authRequest = new AuthorizationRequest(srForm.getRequestedService(), providers, "AuthenticationInfo", true);
		authResponse = getAuthorizationResponse(authRequest, srForm);
		// Poll the QoS Service
		qosVerification = new QoSVerify(srForm.getRequesterSystem(), srForm.getRequestedService(), providers,
				"RequestedQoS");
		qosVerificationResponse = getQosVerificationResponse(qosVerification);
		qosMap = qosVerificationResponse.getResponse();
		// Reserve QoS resources
		for (Entry<ArrowheadSystem, Boolean> entry : qosMap.entrySet()) {
			selectedSystem = entry.getKey(); // TEMPORARLY selects a random
												// system
		}
		qosReservation = new QoSReserve(selectedSystem, srForm.getRequesterSystem(), srForm.getRequestedService(),
				"RequestedQoS");
		qosReservationResponse = doQosReservation(qosReservation);
		// Compile Orchestration Form
		orchForm = new OrchestrationForm(srForm.getRequestedService(), selectedSystem, "serviceURI", "Orchestration Done");
		// Compile Orchestration Response
		responseFormList.add(orchForm);
		orchResponse = new OrchestrationResponse(responseFormList);
		// Send orchestration form
		return Response.status(Status.OK).entity(orchResponse).build();
	}

	/**
	 * This function represents the Intercloud orchestration process.
	 */
	private OrchestrationResponse doIntercloudOrchestration(ServiceRequestForm srForm) {
		GSDRequestForm gsdRequestForm;
		GSDResult gsdResult;
		ICNRequestForm icnRequestForm;
		OrchestrationForm orchForm;
		OrchestrationResponse orchResponse;
		ArrayList<OrchestrationForm> responseFormList = new ArrayList<OrchestrationForm>();

		// Init Global Service Discovery
		gsdRequestForm = new GSDRequestForm(srForm.getRequestedService());
		gsdResult = getGSDResult(gsdRequestForm);

		// TODO: Choose partnering cloud based on certain things...

		// Init Inter-Cloud Negotiation
		// TODO: null should be changed below...
		icnRequestForm = new ICNRequestForm(srForm.getRequestedService(), "authInfo", null);

		// Compile Orchestration Form
		orchForm = new OrchestrationForm(srForm.getRequestedService(), null, "serviceURI", "ICN Done");
		// Compile Orchestration Response
		responseFormList.add(orchForm);
		orchResponse = new OrchestrationResponse(responseFormList);
		// Return orchestration form
		return orchResponse;
	}

	/**
	 * Sends the Service Query Form to the Service Registry and asks for the
	 * Service Query Result.
	 * 
	 * @param sqf
	 * @param srForm
	 * @return ServiceQueryResult
	 */
	private ServiceQueryResult getServiceQueryResult(ServiceQueryForm sqf, ServiceRequestForm srForm) {
		/*
		 * uri
		 * =UriBuilder.fromUri(sysConfig.getServiceRegistryURI()).path(srForm.
		 * getRequestedService().getServiceGroup()).
		 * path(srForm.getRequestedService().getServiceDefinition()).build();
		 */
		System.out.println("orchestator: inside the getServiceQueryResult function");
		// WebTarget target = client.target(uri);
		WebTarget target = client.target("http://localhost:8080/ext/serviceregistry/query");
		Response response = target.request().header("Content-type", "application/json").put(Entity.json(sqf));
		System.out.println("orchestrator: gSQR received the response");
		ServiceQueryResult sqr = response.readEntity(ServiceQueryResult.class);
		System.out.println("orchestrator received the following serviceURIs:");
		for (ProvidedService providedService : sqr.getServiceQueryData()) {
			System.out.println(providedService.getServiceURI() + providedService.getProvider().getIPAddress());
		}
		return sqr;
	}

	/**
	 * Sends the Authorization Request to the Authorization service and asks for
	 * the Authorization Response.
	 * 
	 * @param authRequest
	 * @param srForm
	 * @return AuthorizationResponse
	 */
	private AuthorizationResponse getAuthorizationResponse(AuthorizationRequest authRequest,
			ServiceRequestForm srForm) {
		// Poll the Authorization Service

		/*
		 * uri = UriBuilder.fromUri(sysConfig.getAuthorizationURI()).path(
		 * "SystemGroup")
		 * .path(srForm.getRequestedService().getServiceGroup()).path("System")
		 * .path(srForm.getRequestedService().getInterfaces().get(0)).build();
		 */
		// WebTarget target = client.target(uri);
		// Response response = target.request().header("Content-type",
		// "application/json").put(Entity.json(authRequest));
		System.out.println("orchestrator: inside the getAuthorizationResponse function");
		Map<String, String> stringmap = new HashMap<String, String>();
		Map<ArrowheadSystem, Boolean> systemmap = new HashMap<ArrowheadSystem, Boolean>();
		return new AuthorizationResponse(systemmap, 2, stringmap);
		// return response.readEntity(AuthorizationResponse.class);
	}

	/**
	 * Sends the QoS Verify message to the QoS service and asks for the QoS
	 * Verification Response.
	 * 
	 * @param qosVerify
	 * @return QoSVerificationResponse
	 */
	private QoSVerificationResponse getQosVerificationResponse(QoSVerify qosVerify) {
		// uri =
		// UriBuilder.fromUri(sysConfig.getQoSURI()).path("verify").build();
		// WebTarget target = client.target(uri);
		WebTarget target = client.target("http://localhost:8080/ext/qosservice/verification");
		Response response = target.request().header("Content-type", "application/json").put(Entity.json(qosVerify));
		return response.readEntity(QoSVerificationResponse.class);
	}

	/**
	 * Sends QoS reservation to the QoS service.
	 * 
	 * @param qosReserve
	 * @return boolean indicating that the reservation completed successfully
	 */
	private QoSReservationResponse doQosReservation(QoSReserve qosReserve) {
		// uri =
		// UriBuilder.fromUri(sysConfig.getQoSURI()).path("reserve").build();
		// WebTarget target = client.target(uri);
		WebTarget target = client.target("http://localhost:8080/ext/qosservice/reservation");
		Response response = target.request().header("Content-type", "application/json").put(Entity.json(qosReserve));
		return response.readEntity(QoSReservationResponse.class);
	}

	/**
	 * Initiates the Global Service Discovery process by sending a
	 * GSDRequestForm to the Gatekeeper service and fetches the results.
	 * 
	 * @param gsdRequestForm
	 * @return GSDResult
	 */
	private GSDResult getGSDResult(GSDRequestForm gsdRequestForm) {
		// uri =
		// UriBuilder.fromUri(sysConfig.getQoSURI()).path("verify").build();
		// WebTarget target = client.target(uri);
		WebTarget target = client.target("http://localhost:8080/core/gatekeeper/init_gsd");
		Response response = target.request().header("Content-type", "application/json")
				.put(Entity.json(gsdRequestForm));
		return response.readEntity(GSDResult.class);
	}

	/**
	 * Initiates the Inter-Cloud Negotiation process by sending an
	 * ICNRequestForm to the Gatekeeper service and fetches the results.
	 * 
	 * @param icnRequestForm
	 * @return ICNResultForm
	 */
	private ICNResultForm getICNResultForm(ICNRequestForm icnRequestForm) {
		// uri =
		// UriBuilder.fromUri(sysConfig.getQoSURI()).path("verify").build();
		// WebTarget target = client.target(uri);
		WebTarget target = client.target("http://localhost:8080/core/gatekeeper/init_icn");
		Response response = target.request().header("Content-type", "application/json")
				.put(Entity.json(icnRequestForm));
		return response.readEntity(ICNResultForm.class);
	}

}
