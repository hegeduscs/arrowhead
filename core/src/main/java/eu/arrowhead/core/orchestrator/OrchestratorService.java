package eu.arrowhead.core.orchestrator;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import eu.arrowhead.common.configuration.SysConfig;
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
import eu.arrowhead.common.model.messages.ServiceRequestForm;

/**
 * @author pardavib, mereszd
 *
 */

public class OrchestratorService {

	private URI uri;
	private Client client;
	private ServiceRequestForm serviceRequestForm;
	public SysConfig sysConfig = SysConfig.getInstance();

	public OrchestratorService() {
		super();
		uri = null;
		client = ClientBuilder.newClient();
	}

	public OrchestratorService(ServiceRequestForm serviceRequestForm) {
		super();
		uri = null;
		client = ClientBuilder.newClient();
		this.serviceRequestForm = serviceRequestForm;
	}

	/**
	 * This function represents the local orchestration process.
	 */
	public OrchestrationResponse localOrchestration() {
		ServiceQueryForm srvQueryForm = new ServiceQueryForm(this.serviceRequestForm);
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

		// Poll the Service Registry
		srvQueryResult = getServiceQueryResult(srvQueryForm);
		for (ProvidedService providedService : srvQueryResult.getServiceQueryData()) {
			providers.add(providedService.getProvider());
		}
		selectedSystem = providers.get(0); //temporarily selects the first fit System
		if (isExternal() == false){
			//Poll the Authorization
			authRequest = new AuthorizationRequest(serviceRequestForm.getRequestedService(), providers,
					"AuthenticationInfo", true);
			authResponse = getAuthorizationResponse(authRequest);
			
			// Poll the QoS Service
			qosVerification = new QoSVerify(serviceRequestForm.getRequesterSystem(),
					serviceRequestForm.getRequestedService(), providers, "RequestedQoS");
			qosVerificationResponse = getQosVerificationResponse(qosVerification);
			qosMap = qosVerificationResponse.getResponse();
			
			// Reserve QoS resources
			for (Entry<ArrowheadSystem, Boolean> entry : qosMap.entrySet()) {
				selectedSystem = entry.getKey(); // TEMPORARLY selects a random system
			}
			qosReservation = new QoSReserve(selectedSystem, serviceRequestForm.getRequesterSystem(),
					serviceRequestForm.getRequestedService(), "RequestedQoS");
			qosReservationResponse = doQosReservation(qosReservation);
		}
		// Compile Orchestration Form
		orchForm = new OrchestrationForm(serviceRequestForm.getRequestedService(), selectedSystem, "serviceURI",
				"Orchestration Done");
		// Compile Orchestration Response
		responseFormList.add(orchForm);
		return new OrchestrationResponse(responseFormList);
	}

	/**
	 * This function represents the Intercloud orchestration process.
	 */
	public OrchestrationResponse intercloudOrchestration() {
		GSDRequestForm gsdRequestForm;
		GSDResult gsdResult;
		ICNRequestForm icnRequestForm;
		ICNResultForm icnResultForm;
		OrchestrationForm orchForm;
		OrchestrationResponse orchResponse;
		ArrayList<OrchestrationForm> responseFormList = new ArrayList<OrchestrationForm>();

		// Init Global Service Discovery
		gsdRequestForm = new GSDRequestForm(serviceRequestForm.getRequestedService());
		gsdResult = getGSDResult(gsdRequestForm);

		// TODO: Choose partnering cloud based on certain things...

		// Init Inter-Cloud Negotiation
		icnRequestForm = new ICNRequestForm(serviceRequestForm.getRequestedService(), "authInfo", null);
		icnResultForm = getICNResultForm(icnRequestForm);

		// Compile Orchestration Form
		orchForm = new OrchestrationForm(serviceRequestForm.getRequestedService(), null, "serviceURI", "ICN Done");
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
	private ServiceQueryResult getServiceQueryResult(ServiceQueryForm sqf) {

		/*
		 * uri =UriBuilder.fromUri(sysConfig.getServiceRegistryURI()).path(
		 * serviceRequestForm. getRequestedService().getServiceGroup()).
		 * path(serviceRequestForm.getRequestedService().getServiceDefinition())
		 * .build();
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
	private AuthorizationResponse getAuthorizationResponse(AuthorizationRequest authRequest) {
		// Poll the Authorization Service

		/*
		 * uri = UriBuilder.fromUri(sysConfig.getAuthorizationURI()).path(
		 * "SystemGroup")
		 * .path(serviceRequestForm.getRequestedService().getServiceGroup()).
		 * path("System")
		 * .path(serviceRequestForm.getRequestedService().getInterfaces().get(0)
		 * ).build();
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

	/**
	 * Returns if Inter-Cloud Orchestration is required or not based on the
	 * Service Request Form.
	 * 
	 * @return Boolean
	 */
	public Boolean isInterCloud() {
		return this.serviceRequestForm.getOrchestrationFlags().get("TriggerInterCloud");
	}
	
	public Boolean isExternal (){
		return this.serviceRequestForm.getOrchestrationFlags().get("ExternalServiceRequest");
	}

}
