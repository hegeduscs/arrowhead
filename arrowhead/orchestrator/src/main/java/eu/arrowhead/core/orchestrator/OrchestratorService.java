package eu.arrowhead.core.orchestrator;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.configuration.SysConfig;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.GSDAnswer;
import eu.arrowhead.common.model.messages.GSDRequestForm;
import eu.arrowhead.common.model.messages.GSDResult;
import eu.arrowhead.common.model.messages.ICNRequestForm;
import eu.arrowhead.common.model.messages.ICNResult;
import eu.arrowhead.common.model.messages.IntraCloudAuthRequest;
import eu.arrowhead.common.model.messages.IntraCloudAuthResponse;
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
		if (this.extMatchMaking(psList) == null)
			return null;
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
		//GSD
		GSDResult res = this.startGSD();
		//TODO: Inter-cloud matchmaking
		List<GSDAnswer> gsdList = new ArrayList<GSDAnswer>();
		gsdList = res.getResponse();
		ArrowheadCloud targetCloud = this.intercloudMatchMaking(gsdList);
		//TODO: ICN
		ICNResult icnResult = this.startICN(targetCloud);
		return icnResult.getInstructions();
	}
	
	public OrchestrationResponse overrideStoreNotSet(Boolean storeOnlyActive){
		List<OrchestrationForm> oflist = new ArrayList<OrchestrationForm>();
		String token = null;
		if (storeOnlyActive == true){
			OrchestrationStoreQueryResponse osqr = this.storeQuery(storeOnlyActive);
			oflist = this.storeToOform(osqr);
			if (oflist.isEmpty())
				return null;
			if (serviceRequestForm.getOrchestrationFlags().get("generateToken")){
				token = this.generateToken();
			}
			OrchestrationResponse or = new OrchestrationResponse(oflist);
			return or;
		}
		OrchestrationStoreQueryResponse osqr = this.storeQuery(storeOnlyActive);
		List<OrchestrationStore> orlist = new ArrayList<OrchestrationStore>();
		orlist = osqr.getEntryList();
		//creating the list of active Systems from the Service Registry Query
		ServiceQueryResult sqr = this.queryServiceRegistry();
		List<ProvidedService> pslist = new ArrayList<ProvidedService>();
		pslist = sqr.getServiceQueryData();
		List<ArrowheadSystem> srlist = new ArrayList<ArrowheadSystem>();
		for (int i=0; i<pslist.size(); i++){
			srlist.add(pslist.get(i).getProvider());
		}
		//creating the list of authorized Systems from Authorization Query
		List<ArrowheadSystem> systemfromstore = new ArrayList<ArrowheadSystem>();
		for (int i=0; i<orlist.size(); i++){
			systemfromstore.add(orlist.get(i).getProviderSystem());
		}
		IntraCloudAuthResponse icres = this.queryAuthorization(systemfromstore);
		List<ArrowheadSystem> authenticatedSystems = new ArrayList<ArrowheadSystem>();
		for(Map.Entry<ArrowheadSystem, Boolean> entry : icres.getAuthorizationMap().entrySet()){
			if(entry.getValue())
				authenticatedSystems.add(entry.getKey());
		}
		//iterating based on priority
		ArrowheadSystem temp;
		for (int i=0; i<orlist.size(); i++){
			if(orlist.get(i).getProviderCloud()!=null){
				ICNResult res = this.startICN(orlist.get(i).getProviderCloud());
				return res.getInstructions();
			}
			temp = orlist.get(i).getProviderSystem();
			if(srlist.contains(temp) && authenticatedSystems.contains(temp)){
				if (serviceRequestForm.getOrchestrationFlags().get("generateToken")){
					token = this.generateToken();
				}
				OrchestrationStore tempentry = orlist.get(i);
				OrchestrationForm orform = new OrchestrationForm(tempentry.getService(), tempentry.getProviderSystem(), tempentry.getProviderSystem().getAddress(), token);
				oflist.add(orform);
				OrchestrationResponse or = new OrchestrationResponse(oflist);
				return or;
			}	
		}
		//if we get there no authenticated and active System can be found, so error
		return null;
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
	
	
	//From this section the methods are sub-methods for the Orchestrations
	
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
	
	public GSDResult startGSD(){
		String URI = SysConfig.getGatekeeperURI();
		URI = UriBuilder.fromPath(URI).path("init_gsd").toString();
		GSDRequestForm reqform = new GSDRequestForm(serviceRequestForm.getRequestedService(), serviceRequestForm.getPreferredClouds());
		Response response = Utility.sendRequest(URI, "PUT", reqform);
		GSDResult res = response.readEntity(GSDResult.class);
		return res;
	}
	
	public ArrowheadCloud intercloudMatchMaking(List<GSDAnswer> list){
		if (list.isEmpty())
			return null;
		List<ArrowheadCloud> preferedClouds = new ArrayList<ArrowheadCloud>();
		for (int i=0; i<list.size(); i++){
			if (serviceRequestForm.getPreferredClouds().contains(list.get(i).getProviderCloud()))
				preferedClouds.add(list.get(i).getProviderCloud());
		}
		if(preferedClouds.isEmpty() == false) //if the preferedClouds isn't empty we return the first value
			return preferedClouds.get(0);
		return list.get(0).getProviderCloud(); //if the preferedClouds is empty, we return the first entry of the list we got
	}
	
	public ICNResult startICN(ArrowheadCloud target){
		Boolean onlyPreferred = serviceRequestForm.getOrchestrationFlags().get("onlyPreferred");
		String URI = SysConfig.getGatekeeperURI();
		URI = UriBuilder.fromPath(URI).path("init_icn").toString();
		ICNRequestForm reqform = new ICNRequestForm(serviceRequestForm.getRequestedService(), "Placeholder", target, serviceRequestForm.getRequesterSystem(), serviceRequestForm.getPreferredProviders(), onlyPreferred);
		Response response = Utility.sendRequest(URI, "PUT", reqform);
		ICNResult res = response.readEntity(ICNResult.class);
		return res;
	}
	
	public OrchestrationStoreQueryResponse storeQuery(Boolean onlyActive){
		String URI = SysConfig.getOrchestratorURI();
		URI = UriBuilder.fromPath(URI).path("store").toString();
		OrchestrationStoreQuery osq = new OrchestrationStoreQuery(serviceRequestForm.getRequestedService(), serviceRequestForm.getRequesterSystem(), onlyActive);
		Response response = Utility.sendRequest(URI, "PUT", osq);
		OrchestrationStoreQueryResponse osqr = response.readEntity(OrchestrationStoreQueryResponse.class);
		return osqr;
	}
	
	public List<OrchestrationForm> storeToOform(OrchestrationStoreQueryResponse osqr){
		List<OrchestrationForm> oflist = new ArrayList<OrchestrationForm>();
		List<OrchestrationStore> entryList = new ArrayList<OrchestrationStore>();
		entryList = osqr.getEntryList();
		for (OrchestrationStore oStore : entryList){
			OrchestrationForm oForm = new OrchestrationForm(oStore.getService(), oStore.getProviderSystem(), oStore.getProviderSystem().getAddress(), oStore.getProviderSystem().getAuthenticationInfo());
			oflist.add(oForm);
		}
		return oflist;
	}
	
	public IntraCloudAuthResponse queryAuthorization(List<ArrowheadSystem> systemList){
		String URI = SysConfig.getAuthorizationURI();
		String systemGroup = serviceRequestForm.getRequesterSystem().getSystemGroup();
		String systemName = serviceRequestForm.getRequesterSystem().getSystemName();
		URI = UriBuilder.fromPath(URI).path("SystemGroup").path(systemGroup).path("System").path(systemName).toString();
		IntraCloudAuthRequest iareq = new IntraCloudAuthRequest(serviceRequestForm.getRequesterSystem(), systemList, serviceRequestForm.getRequestedService(), false);
		Response response = Utility.sendRequest(URI, "PUT", iareq);
		IntraCloudAuthResponse res = response.readEntity(IntraCloudAuthResponse.class);
		return res;
	}

}
