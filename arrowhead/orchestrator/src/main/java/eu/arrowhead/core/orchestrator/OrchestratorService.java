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
	
	public OrchestrationResponse externalRequest(){
		//TODO: Query Service Registry
		//TODO: Matchmaking
		//TODO: Additional Tasks
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

}
