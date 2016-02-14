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

	private OrchestrationService orchestrationService = new OrchestrationService();
	//private SysConfig sysConfig = SysConfig.getInstance();

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String welcome(@Context UriInfo uriInfo) {
		return "Hello, this is the Orchestration Service.";
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
		QoSVerify qosVerification;
		QoSVerificationResponse qosVerificationResponse;
		Map<ArrowheadSystem, Boolean> qosMap;
		ArrowheadSystem selectedSystem = null;
		QoSReserve qosReservation;
		QoSReservationResponse qosReservationResponse;
		URI uri = null;
		OrchestrationForm orchForm;
		OrchestrationResponse orchResponse;
		ArrayList<OrchestrationForm> responseFormList = new ArrayList<OrchestrationForm>();
		System.out.println("orchestrator: SRF received");

		// Check for intercloud orchestration
		if (srForm.getOrchestrationFlags().get("TriggerInterCloud")) {
			System.out.println("orchestrator: inside the intercloud IF statement");
			doIntercloudOrchestration();
			//return Response.status(Status.OK).entity(null).build();
		}

		// Poll the Service Registry
		/*uri = UriBuilder.fromUri(sysConfig.getServiceRegistryURI()).path(srForm.getRequestedService().getServiceGroup())
				.path(srForm.getRequestedService().getServiceDefinition()).build();*/
		srvQueryResult = getServiceQueryResult(srvQueryForm, uri);
		System.out.println("orchestrator: returned to the main function after getting the SQR");

		// Poll the Authorization Service
		/*uri = UriBuilder.fromUri(sysConfig.getAuthorizationURI()).path("SystemGroup")
				.path(srForm.getRequestedService().getServiceGroup()).path("System")
				.path(srForm.getRequestedService().getInterfaces().get(0)).build();*/

		for (ProvidedService providedService : srvQueryResult.getServiceQueryData()) {
			providers.add(providedService.getProvider());
		}
		
		System.out.println("A providers lista hossza: " + providers.size());

		authRequest = new AuthorizationRequest(srForm.getRequestedService(), providers, "AuthenticationInfo", true);
		System.out.println("orchestrator: AuthRequest created");
		authResponse = getAuthorizationResponse(authRequest, uri);

		// Poll the QoS Service
		uri = uriInfo.getBaseUriBuilder().path("QoS") // NEED TO SPECIFY
				.path("verify").build();
		qosVerification = new QoSVerify(srForm.getRequesterSystem(), srForm.getRequestedService(), providers,
				"RequestedQoS");
		qosVerificationResponse = getQosVerificationResponse(qosVerification, uri);

		// TODO: Matchmaking

		// Poll the QoS Service
		/*uri = uriInfo.getBaseUriBuilder().path("QoS") // NEED TO SPECIFY
				.path("reserve").build();*/
		qosMap = qosVerificationResponse.getResponse();

		// Reserve QoS resources
		for (Entry<ArrowheadSystem, Boolean> entry : qosMap.entrySet()) {
			selectedSystem = entry.getKey(); // TEMPORARLY selects a random
												// system
		}

		qosReservation = new QoSReserve(selectedSystem, srForm.getRequesterSystem(), srForm.getRequestedService());
		qosReservationResponse = doQosReservation(qosReservation, uri);
		System.out.println("orchestrator: qosreservationresponse received, back to main method");

		// Compile Orchestration Form
		orchForm = new OrchestrationForm(srForm.getRequestedService(), selectedSystem, "serviceURI",
				"vegigfutottam");
		System.out.println("orchestrator: orchform created");

		// Compile Orchestration Response
		responseFormList.add(orchForm);
		orchResponse = new OrchestrationResponse(responseFormList);

		// Send orchestration form
		System.out.println("orchestrator: about to return");
		//return orchResponse;
		return Response.status(Status.OK).entity(orchResponse).build();
	}

	/**
	 * This function represents the Intercloud orchestration process.
	 */
	private void doIntercloudOrchestration() {

		// TODO: Inter-cloud orchestration

		return;
	}

	/**
	 * Sends the Service Query Form to the Service Registry and asks for the
	 * Service Query Result.
	 * 
	 * @param sqf
	 * @param uriInfo
	 * @return ServiceQueryResult
	 */
	private ServiceQueryResult getServiceQueryResult(ServiceQueryForm sqf, URI uri) {
		System.out.println("orchestator: inside the getServiceQueryResult function");
		Client client = ClientBuilder.newClient();
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
	 * @param uri
	 * @return AuthorizationResponse
	 */
	private AuthorizationResponse getAuthorizationResponse(AuthorizationRequest authRequest, URI uri) {
		System.out.println("orchestrator: inside the getAuthorizationResponse function");
		//Client client = ClientBuilder.newClient();
		//WebTarget target = client.target(uri);
		//Response response = target.request().header("Content-type", "application/json").put(Entity.json(authRequest));
		Map<String,String> stringmap = new HashMap<String,String>();
		Map<ArrowheadSystem, Boolean> systemmap = new HashMap<ArrowheadSystem, Boolean>();
		return new AuthorizationResponse(systemmap, 2, stringmap);
		//return response.readEntity(AuthorizationResponse.class);
	}

	/**
	 * Sends the QoS Verify message to the QoS service and asks for the QoS
	 * Verification Response.
	 * 
	 * @param qosVerify
	 * @param uri
	 * @return QoSVerificationResponse
	 */
	private QoSVerificationResponse getQosVerificationResponse(QoSVerify qosVerify, URI uri) {
		System.out.println("orchestrator: inside the getQoSVerificationResponse method");
		Client client = ClientBuilder.newClient();
		//WebTarget target = client.target(uri);
		WebTarget target = client.target("http://localhost:8080/ext/qosservice/verification");
		Response response = target.request().header("Content-type", "application/json").put(Entity.json(qosVerify));
		return response.readEntity(QoSVerificationResponse.class);
	}

	/**
	 * Sends QoS reservation to the QoS service.
	 * 
	 * @param qosReserve
	 * @param uri
	 * @return boolean indicating that the reservation completed successfully
	 */
	private QoSReservationResponse doQosReservation(QoSReserve qosReserve, URI uri) {
		System.out.println("orchestrator: inside the doQoSReservation method");
		Client client = ClientBuilder.newClient();
		//WebTarget target = client.target(uri);
		WebTarget target = client.target("http://localhost:8080/ext/qosservice/reservation");
		Response response = target.request().header("Content-type", "application/json").put(Entity.json(qosReserve));
		return response.readEntity(QoSReservationResponse.class);
	}
}
