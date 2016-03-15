package eu.arrowhead.core.orchestrator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import eu.arrowhead.common.configuration.SysConfig;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.GSDEntry;
import eu.arrowhead.common.model.messages.GSDRequestForm;
import eu.arrowhead.common.model.messages.GSDResult;
import eu.arrowhead.common.model.messages.ICNRequestForm;
import eu.arrowhead.common.model.messages.ICNResultForm;
import eu.arrowhead.common.model.messages.IntraCloudAuthRequest;
import eu.arrowhead.common.model.messages.IntraCloudAuthResponse;
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
	private static Logger log = Logger.getLogger(OrchestratorService.class.getName());

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
		srvQueryForm.setTsig_key("RIuxP+vb5GjLXJo686NvKQ==");
		ArrowheadService srv = null;
		ServiceQueryResult srvQueryResult;
		IntraCloudAuthRequest authReq;
		IntraCloudAuthResponse authResp;
		List<ArrowheadSystem> providers = new ArrayList<ArrowheadSystem>();
		List<ProvidedService> provservices = new ArrayList<ProvidedService>();
		QoSVerify qosVerification;
		QoSVerificationResponse qosVerificationResponse;
		ArrowheadSystem selectedSystem = null;
		QoSReserve qosReservation;
		QoSReservationResponse qosReservationResponse;
		List<OrchestrationForm> responseFormList = new ArrayList<OrchestrationForm>();

		// Poll the Service Registry
		log.info("Polling the Service Registry.");
		srvQueryResult = getServiceQueryResult(srvQueryForm, this.serviceRequestForm);
		provservices = srvQueryResult.getServiceQueryData();
		if (provservices.isEmpty()){
			log.info("Could not find any matching providers");
			return null;
		}
		for (ProvidedService providedService : provservices) {
			providers.add(providedService.getProvider());
		}
		// If the SRF is external, no need for Auth and QoS
		if (isExternal() == false) {

			// Poll the Authorization
			log.info("Polling the Authorization service.");
			authReq = new IntraCloudAuthRequest("authInfo", this.serviceRequestForm.getRequestedService(), false,
					providers);
			authResp = getAuthorizationResponse(authReq, this.serviceRequestForm);

			// Removing the non-authenticated systems from the providers list
			for (ArrowheadSystem ahsys : authResp.getAuthorizationMap().keySet()) {
				if (authResp.getAuthorizationMap().get(ahsys) == false)
					providers.remove(ahsys);
			}

			// Poll the QoS Service
			log.info("Polling the QoS service.");
			qosVerification = new QoSVerify(serviceRequestForm.getRequesterSystem(),
					serviceRequestForm.getRequestedService(), providers, "RequestedQoS");
			qosVerificationResponse = getQosVerificationResponse(qosVerification);

			// Removing the bad QoS ones from consideration - temporarily
			// everything is true
			for (ArrowheadSystem ahsys : qosVerificationResponse.getResponse().keySet()) {
				if (qosVerificationResponse.getResponse().get(ahsys) == false)
					providers.remove(ahsys);
			}

			// Reserve QoS resources
			log.info("Reserving QoS resources.");
			selectedSystem = providers.get(0); // temporarily selects the first
			qosReservation = new QoSReserve(selectedSystem, this.serviceRequestForm.getRequesterSystem(),
					"requestedQoS", this.serviceRequestForm.getRequestedService());
			qosReservationResponse = doQosReservation(qosReservation);

			// Actualizing the provservices list
			for (ProvidedService provservice : provservices) {
				if (providers.contains(provservice.getProvider()) == false)
					provservices.remove(provservice);
			}
		}
		else {
			log.info("SRF is external, Auth and Qos polling skipped.");
		}
		
		// Create Orchestration Forms - returning every matching System as
		// Required for the Demo
		log.info("Creating orchestration forms.");
		for (ProvidedService provservice : provservices) {
			//Setting the correct interface list for the Form
			srv = this.serviceRequestForm.getRequestedService();
			srv.getInterfaces().clear();
			srv.getInterfaces().add(provservice.getServiceInterface());
			responseFormList.add(new OrchestrationForm(srv, provservice.getProvider(), provservice.getServiceURI(), "authorizationInfo"));
		}
		return new OrchestrationResponse(responseFormList);
	}

	/**
	 * This function represents the Intercloud orchestration process.
	 */
	public OrchestrationResponse intercloudOrchestration() {
		GSDRequestForm gsdRequestForm;
		GSDResult gsdResult;
		ICNResultForm icnResultForm;
		OrchestrationResponse orchResponse;
		ArrayList<OrchestrationForm> responseFormList = new ArrayList<OrchestrationForm>();

		// Init Global Service Discovery
		log.info("Initiating global service discovery.");
		gsdRequestForm = new GSDRequestForm(serviceRequestForm.getRequestedService());
		gsdResult = getGSDResult(gsdRequestForm);
		if (gsdResult.getResponse().isEmpty()){
			log.info("bad");
			return null;
		}
		log.info("orchestrator: Got the results from the Gatekeeper");

		// Putting an ICN Request Form to every single matching cloud
		log.info("Processing global service discovery data.");
		for (GSDEntry entry : gsdResult.getResponse()) {
			// ICN Request for each cloud contained in an Entry
			log.info("Sendin ICN to the following cloud: " + entry.getCloud().getName());
			icnResultForm = getICNResultForm(new ICNRequestForm(this.serviceRequestForm.getRequestedService(),
					"authenticationInfo", entry.getCloud(),this.serviceRequestForm.getRequesterSystem()));
			// Adding every OrchestrationForm from the returned Response to the
			// final Response
			for (OrchestrationForm of : icnResultForm.getInstructions().getResponse()) {
				log.info("Adding the following ServiceURI: " + of.getServiceURI());
				responseFormList.add(of);
			}
		}
		if (responseFormList.isEmpty()){
			log.info("badbad");
			return null;
		}
		// Creating the response
		log.info("Creating orchestration response.");
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
	private ServiceQueryResult getServiceQueryResult(ServiceQueryForm sqf, ServiceRequestForm srf) {
		log.info("orchestator: inside the getServiceQueryResult function");
		ArrowheadService as = srf.getRequestedService();
		String strtarget = sysConfig.getServiceRegistryURI() + "/" + as.getServiceGroup() + "/"
				+ as.getServiceDefinition();
		log.info("orchestrator: sending the ServiceQueryForm to this address:" + strtarget);
		WebTarget target = client.target(strtarget);
		Response response = target.request().header("Content-type", "application/json").put(Entity.json(sqf));
		log.info("The data of the ServiceQueryForm: ");
		log.info("Metadata: " + sqf.getServiceMetaData());
		log.info("The TSIG_key: " + sqf.getTsig_key());
		log.info("The service interfaces: ");
		for (String str : sqf.getServiceInterfaces()){
			log.info("Service interface: " + str);
		}
		ServiceQueryResult sqr = response.readEntity(ServiceQueryResult.class);
		log.info("orchestrator received the following services from the SR:");
		for (ProvidedService providedService : sqr.getServiceQueryData()) {
			System.out.println(
					providedService.getProvider().getSystemGroup() + providedService.getProvider().getSystemName());
		}
		return sqr;
	}

	/**
	 * Sends the Authorization Request to the Authorization service and asks for
	 * the Authorization Response.
	 * 
	 * @param authRequest
	 * @return AuthorizationResponse
	 */
	private IntraCloudAuthResponse getAuthorizationResponse(IntraCloudAuthRequest authReq, ServiceRequestForm srf) {
		log.info("orchestrator: inside the getAuthorizationResponse function");
		String strtarget = sysConfig.getAuthorizationURI() + "/systemgroup/" + srf.getRequesterSystem().getSystemGroup()
				+ "/system/" + srf.getRequesterSystem().getSystemName();
		log.info("orchestrator: sending AuthReq to this address: " + strtarget);
		WebTarget target = client.target(strtarget);
		Response response = target.request().header("Content-type", "application/json").put(Entity.json(authReq));
		IntraCloudAuthResponse authResp = response.readEntity(IntraCloudAuthResponse.class);
		log.info("orchestrator received the following services from the AR:");
		for (ArrowheadSystem ahsys : authResp.getAuthorizationMap().keySet()) {
			System.out.println("System: " + ahsys.getSystemGroup() + ahsys.getSystemName());
			System.out.println("Value: " + authResp.getAuthorizationMap().get(ahsys));
		}
		return authResp;
	}

	/**
	 * Sends the QoS Verify message to the QoS service and asks for the QoS
	 * Verification Response.
	 * 
	 * @param qosVerify
	 * @return QoSVerificationResponse
	 */
	private QoSVerificationResponse getQosVerificationResponse(QoSVerify qosVerify) {
		log.info("orchestrator: inside the getQoSVerificationResponse function");
		String strtarget = sysConfig.getOrchestratorURI().replace("orchestrator/orchestration", "QoSManager") + "/QoSVerify";
		log.info("orchestrator: sending QoSVerify to this address: " + strtarget);
		WebTarget target = client.target(strtarget);
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
		log.info("orchestrator: inside the doQoSReservation function");
		String strtarget = sysConfig.getOrchestratorURI().replace("orchestrator/orchestration", "QoSManager") + "/QoSReserve";
		log.info("orchestrator: sending QoSReserve to this address: " + strtarget);
		WebTarget target = client.target(strtarget);
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
		log.info("orchestrator: inside the getGSDResult function");
		String strtarget = sysConfig.getGatekeeperURI() + "/init_gsd/";
		log.info("orchestrator: sent GSDRequestForm to the following: " + strtarget);
		WebTarget target = client.target(strtarget);
		Response response = target.request().header("Content-type", "application/json")
				.put(Entity.json(gsdRequestForm));
		log.info("orchestrator: received response from the GateKeeper, returning");
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
		log.info("orchestrator: inside the getICNResultForm function");
		String strtarget = sysConfig.getGatekeeperURI() + "/init_icn/";
		WebTarget target = client.target(strtarget);
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
		for (String str : serviceRequestForm.getOrchestrationFlags().keySet()){
			log.info("Key name: " + str + ", key value " + serviceRequestForm.getOrchestrationFlags().get(str));
		}
		return this.serviceRequestForm.getOrchestrationFlags().get("triggerInterCloud");
	}

	public Boolean isExternal() {
		return this.serviceRequestForm.getOrchestrationFlags().get("externalServiceRequest");
	}

}
