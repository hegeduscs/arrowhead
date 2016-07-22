package eu.arrowhead.core.orchestrator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.configuration.SysConfig;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.OrchestrationForm;
import eu.arrowhead.common.model.messages.OrchestrationResponse;
import eu.arrowhead.common.model.messages.OrchestrationStoreQuery;
import eu.arrowhead.common.model.messages.OrchestrationStoreQueryResponse;
import eu.arrowhead.common.model.messages.ProvidedService;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;
import eu.arrowhead.common.model.messages.ServiceRequestForm;
import javax.ws.rs.core.Response;

public class OrchestratorService {
	
	private URI uri;
	private Client client;
	private ServiceRequestForm serviceRequestForm;
	private Boolean testing = true;
	private static Logger log = Logger.getLogger(OrchestratorService.class.getName());
	
	public OrchestratorService(ServiceRequestForm serviceRequestForm) {
		super();
		uri = null;
		client = ClientBuilder.newClient();
		this.serviceRequestForm = serviceRequestForm;
	}

	public OrchestratorService() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public OrchestrationResponse externalRequest(){
		//Query Service Registry
		String token = null;
		ServiceQueryResult sqr = this.queryServiceRegistry();
		List<ProvidedService> psList = new ArrayList<ProvidedService>();
		psList = sqr.getServiceQueryData();
		//Matchmaking
		ProvidedService pS = this.extMatchMaking(psList);
		//Additional Tasks
		this.extAT();
		//Generating Token
		if (serviceRequestForm.getOrchestrationFlags().get("generateToken")){
			token = this.generateToken();
		}
		List<OrchestrationForm> oflist = new ArrayList<OrchestrationForm>();
		OrchestrationForm of = new OrchestrationForm(pS.getOffered(), pS.getProvider(), pS.getServiceURI(), token);
		oflist.add(of);
		OrchestrationResponse or = new OrchestrationResponse(oflist);
		return or;
	}
	
	public OrchestrationResponse legacyModeOrchestration(){
		System.out.println("Inside the legacyModeOrchestration method.");
		List<OrchestrationForm> oflist = new ArrayList<OrchestrationForm>();
		if (testing){
			System.out.println("Testing mode active");
			oflist.add(this.getDummyOF());
		}
		else{
			System.out.println("Live mode active");
			String URI = SysConfig.getOrchestratorURI();
			URI = UriBuilder.fromPath(URI).path("store").toString();
			Boolean onlyActive = serviceRequestForm.getOrchestrationFlags().get("storeOnlyActive");
			OrchestrationStoreQuery osq = new OrchestrationStoreQuery(serviceRequestForm.getRequestedService(), serviceRequestForm.getRequesterSystem(), onlyActive);
			Response response = Utility.sendRequest(URI, "PUT", osq);
			OrchestrationStoreQueryResponse osqr = response.readEntity(OrchestrationStoreQueryResponse.class);
			List<OrchestrationStore> entryList = new ArrayList<OrchestrationStore>();
			for (OrchestrationStore oStore : entryList){
				OrchestrationForm oForm = new OrchestrationForm(oStore.getService(), oStore.getProviderSystem(), oStore.getProviderSystem().getAddress(), oStore.getProviderSystem().getAuthenticationInfo());
				oflist.add(oForm);
			}
		}
		//Placeholder for future authorization token generation
		System.out.println("Generating token (somehow)");
		OrchestrationResponse or = new OrchestrationResponse(oflist);
		return or;
	}
	
	public OrchestrationResponse triggerInterCloud(){
		//TODO: GSD
		//TODO: Inter-cloud matchmaking
		//TODO: ICN
		List<OrchestrationForm> oflist = new ArrayList<OrchestrationForm>();
		oflist.add(this.getDummyOF());
		System.out.println("Generating token (somehow)");
		OrchestrationResponse or = new OrchestrationResponse(oflist);
		return or;
	}
	
	public OrchestrationResponse overrideStoreNotSet(){
		//TODO: Query Orchestration Store
		//TODO: Iterating on rules based on priority
		//TODO: Additional tasks based on ProviderCloud
		//TODO: IF yes: ICN
		//TODO: IF no: Query Service Registry + cross-check with authorization
		List<OrchestrationForm> oflist = new ArrayList<OrchestrationForm>();
		oflist.add(this.getDummyOF());
		System.out.println("Generating token (somehow)");
		OrchestrationResponse or = new OrchestrationResponse(oflist);
		return or;
	}
	
	public OrchestrationResponse regularOrchestration(){
		//TODO: Query Service Registry
		//TODO: Cross-check with Authorization
		//TODO: Filtering for preferred
		//TODO: Intra-cloud matchmaking
		//TODO: IF Orchestration was successful -> DONE
		//TODO: IF not, deciding based on EnableInterCloud
		//TODO: IF EnableInterCloud is false -> ERROR
		//TODO: IF EnableIntercloud is true
		//TODO: GSD
		//TODO: Inter-cloud matchmaking
		//TODO: ICN
		List<OrchestrationForm> oflist = new ArrayList<OrchestrationForm>();
		oflist.add(this.getDummyOF());
		System.out.println("Generating token (somehow)");
		OrchestrationResponse or = new OrchestrationResponse(oflist);
		return or;
	}
	
	public OrchestrationForm getDummyOF(){
		ArrowheadService ah_service = new ArrowheadService("AITIA", "Very good service", null, null);
		ArrowheadSystem ah_system = new ArrowheadSystem("AITIA", "1", "192.168.1.1", "8080", "not good");
		OrchestrationForm of = new OrchestrationForm(ah_service, ah_system, "localhost", "not good");
		return of;
	}
	
	public ServiceQueryResult queryServiceRegistry(){
		String URI = SysConfig.getServiceRegistryURI();
		String serviceGroup = serviceRequestForm.getRequestedService().getServiceGroup();
		String serviceDefinition = serviceRequestForm.getRequestedService().getServiceDefinition();
		URI = UriBuilder.fromPath(URI).path(serviceGroup).path(serviceDefinition).toString();
		ServiceQueryForm sqf = new ServiceQueryForm(serviceRequestForm);
		//tsig_key has to be set
		Response response = Utility.sendRequest(URI, "PUT", sqf);
		ServiceQueryResult sqr = response.readEntity(ServiceQueryResult.class);
		return sqr;
	}
	
	public ProvidedService extMatchMaking(List<ProvidedService> psList){
		if (psList.isEmpty())
			return null;
		if (serviceRequestForm.getPreferredProviders().isEmpty())
			return psList.get(0); //if there is no preference returning the first match
		int cloudSize = serviceRequestForm.getPreferredClouds().size();
		if (serviceRequestForm.getPreferredClouds().isEmpty())
			cloudSize = 1; //because of the for cycle
		int systemSize = serviceRequestForm.getPreferredProviders().size();
		List<ProvidedService> preferedList = new ArrayList<ProvidedService>(); //the list of providers in the preferdlist aswell
		if (systemSize > cloudSize){
			for (int i = cloudSize -1; i<systemSize; i++){ //iterating through list got from SR
				ArrowheadSystem temp = psList.get(i).getProvider();
				if (serviceRequestForm.getPreferredProviders().contains(temp)) //if the system from the SR is prefered
						preferedList.add(psList.get(i));
			}
		}
		if (preferedList.isEmpty() == false) //if we found active and prefered systems, we return the first one
			return preferedList.get(0);
		if (psList.isEmpty() == false) //if there are no active and prefered systems but there are active systems we return the first one
			return psList.get(0);
		return null;
	}
	
	public void extAT(){
		//placeholder
	}
	
	public String generateToken(){
		return "Placeholder";
	}

}
