package eu.arrowhead.core.orchestrator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.configuration.SysConfig;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.ErrorMessage;
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

/**
 * @author umlaufz
 */
public final class OrchestratorService {
	
	private static Logger log = Logger.getLogger(OrchestratorService.class.getName());
	
	/** 
	 * This method represents the regular orchestration process where the requester System 
	 * is in the local Cloud.
	 * In this process the Orchestration Store is ignored, and the Orchestrator first tries to find
	 * a provider in the local Cloud. If that fails but the enableInterCloud flag is set to true,
	 * the Orchestrator tries to find a provider in other Clouds.
	 * 
	 * @param ServiceRequestForm srf
	 * @return OrchestrationResponse
	 * @throws BadPayloadException, DataNotFoundException
	 */
	public static OrchestrationResponse regularOrchestration(ServiceRequestForm srf){
		log.info("Entered the regularOrchestration method.");
		
		Map<String, Boolean> orchestrationFlags = new HashMap<String, Boolean>();
		orchestrationFlags = srf.getOrchestrationFlags();
		
		try{
			//Querying the Service Registry
			List<ProvidedService> psList = new ArrayList<ProvidedService>();
			psList = queryServiceRegistry(srf.getRequestedService(), orchestrationFlags.get("metadataSearch"), 
					orchestrationFlags.get("pingProviders"));
			
			//Cross-checking the SR response with the Authorization
			List<ArrowheadSystem> providerSystems = new ArrayList<ArrowheadSystem>();
			for(ProvidedService service : psList){
				providerSystems.add(service.getProvider());
			}
			providerSystems = queryAuthorization(srf.getRequesterSystem(), srf.getRequestedService(), 
					providerSystems);
			
			/*
			 * The Authorization check only returns the provider systems where the 
			 * requester system is authorized to consume the service. We filter out the 
			 * non-authorized systems from the SR response.
			 */
			List<ProvidedService> temp = new ArrayList<ProvidedService>();
			for(ProvidedService service : psList){
				if(!providerSystems.contains(service.getProvider())){
					temp.add(service);
				}
			}
			psList.removeAll(temp);
			
			//If needed, removing the non-preferred providers from the remaining list
			if(orchestrationFlags.get("onlyPreferred")){
				psList = removeNonPreferred(psList, srf.getPreferredProviders());
			}
			
			/*
			 * If matchmaking is requested, we pick out 1 ProvidedService entity from the list.
			 * If only preferred Providers are allowed, matchmaking might not be possible.
			 */
			if(orchestrationFlags.get("matchmaking")){
				ProvidedService ps = intraCloudMatchmaking(psList, orchestrationFlags.get("onlyPreferred"), 
						srf.getPreferredProviders(), srf.getPreferredClouds().size());
				psList.clear();
				psList.add(ps);
			}
			
			//All the filtering is done, need to compile the response
			return compileOrchestrationResponse(srf.getRequestedService(), psList, 
					orchestrationFlags.get("generateToken"));
		}
		/*
		 * If the Intra-Cloud orchestration fails somewhere (SR, Auth, filtering, matchmaking) 
		 * we catch the exception, because Inter-Cloud orchestration might be allowed. If not, 
		 * we throw the same exception again.
		 */
		catch(DataNotFoundException ex){
			if(!orchestrationFlags.get("enableInterCloud")){
				log.info("Intra-Cloud orchestration failed with DNFException, but Inter-Cloud is not allowed.");
				throw new DataNotFoundException(ex.getMessage());
			}
		}
		catch(BadPayloadException ex){
			if(!orchestrationFlags.get("enableInterCloud")){
				log.info("Intra-Cloud orchestration failed with BPException, but Inter-Cloud is not allowed.");
				throw new BadPayloadException(ex.getMessage());
			}
		}
		/*
		 * If the code reaches this part, that means the Intra-Cloud orchestration failed, 
		 * but the Inter-Cloud orchestration is allowed, so we try that too.
		 */
		
		return triggerInterCloud(srf);
	}

	
	/**
	 * This method represents the orchestration process where the requester System is NOT in the local Cloud.
	 * This means that the Gatekeeper made sure that this request from the remote Orchestrator can be
	 * satisfied in this Cloud. (Gatekeeper polled the Service Registry and Authorization Systems.)
	 * 
	 * @param ServiceRequestForm srf
	 * @return OrchestrationResponse
	 */
	public static OrchestrationResponse externalServiceRequest(ServiceRequestForm srf){
		log.info("Entered the externalServiceRequest method.");
		
		Map<String, Boolean> orchestrationFlags = new HashMap<String, Boolean>();
		orchestrationFlags = srf.getOrchestrationFlags();
		
		//Querying the Service Registry to get the list of Provider Systems
		List<ProvidedService> psList = new ArrayList<ProvidedService>();
		psList = queryServiceRegistry(srf.getRequestedService(), orchestrationFlags.get("metadataSearch"), 
				orchestrationFlags.get("pingProviders"));
		
		/*
		 * If needed, removing the non-preferred providers from the SR response. 
		 * (If needed, matchmaking is done at the request sender Cloud.)
		 */
		if(orchestrationFlags.get("onlyPreferred")){
			log.info("Only preferred matchmaking is requested.");
			psList = removeNonPreferred(psList, srf.getPreferredProviders());
		}

		//Compiling the orchestration response
		return compileOrchestrationResponse(srf.getRequestedService(), 
				psList, orchestrationFlags.get("generateToken"));
	}
	
	/**
	 * This method represents the orchestration process where the requester System only asked for 
	 * Inter-Cloud servicing.
	 * 
	 * @param ServiceRequestForm srf
	 * @return OrchestrationResponse
	 */
	public static OrchestrationResponse triggerInterCloud(ServiceRequestForm srf){
		log.info("Entered the triggerInterCloud method.");
		
		Map<String, Boolean> orchestrationFlags = new HashMap<String, Boolean>();
		orchestrationFlags = srf.getOrchestrationFlags();
		
		//Telling the Gatekeeper to do a Global Service Discovery
		GSDResult result = startGSD(srf.getRequestedService(), srf.getPreferredClouds());
		
		//Picking a target Cloud from the ones that responded to the GSD poll
		ArrowheadCloud targetCloud = interCloudMatchmaking(result, srf.getPreferredClouds(), 
				orchestrationFlags.get("onlyPreferred"));
		
		//Telling the Gatekeeper to start the Inter-Cloud Negotiations process
		ICNRequestForm icnRequestForm = compileICNRequestForm(srf,targetCloud);
		ICNResult icnResult = startICN(icnRequestForm);
		
		//If matchmaking is requested, we pick one provider from the ICN result
		if(orchestrationFlags.get("matchmaking")){
			return icnMatchmaking(icnResult, icnRequestForm.getPreferredProviders());
		}
		else{
			return icnResult.getInstructions();
		}
	}
	
	/**
	 * This method represents the orchestration process where the Orchestration Store database
	 * is used to see if there is a provider for the requester System. The Orchestration Store 
	 * contains preset orchestration information, which should not change in runtime.
	 * 
	 * @param ServiceRequestForm srf
	 * @return OrchestrationResponse
	 * @throws DataNotFoundException
	 */
	public static OrchestrationResponse orchestrationFromStore(ServiceRequestForm srf){
		log.info("Entered the orchestrationFromStore method.");
		
		Map<String, Boolean> orchestrationFlags = new HashMap<String, Boolean>();
		orchestrationFlags = srf.getOrchestrationFlags();
		
		//Querying the Orchestration Store for matching entries
		List<OrchestrationStore> entryList = new ArrayList<OrchestrationStore>();
		entryList = queryOrchestrationStore(srf.getRequestedService(), srf.getRequesterSystem(), 
				orchestrationFlags.get("storeOnlyActive"));
		
		//Legacy behavior handled differently, returning all "active" entries belonging to the consumer
		if(orchestrationFlags.get("storeOnlyActive")){
			if(!entryList.isEmpty()){
				return compileOrchestrationResponse(entryList, orchestrationFlags.get("generateToken"));
			}
			else{
				log.info("No active store entry were found for this consumer System. "
						+ "(OrchestrationService:orchestrationFromStore DataNotFoundException)");
				throw new DataNotFoundException("No active store entry were found for this consumer System: "
						+ srf.getRequesterSystem().toString());
			}
		}
		
		//We pick out the intra-cloud Store entries
		List<OrchestrationStore> intraStoreList = new ArrayList<OrchestrationStore>();
		for(OrchestrationStore entry : entryList){
			if(entry.getProviderCloud() == null){
				intraStoreList.add(entry);
			}
		}
		
		/*
		 * Before we iterate through the entry list to pick out a provider, we have to poll the 
		 * Service Registry and Authorization Systems, so we have these 2 other ArrowheadSystem
		 * provider lists to cross-check the entry list with.
		 */
		List<ProvidedService> psList = new ArrayList<ProvidedService>();
		List<ArrowheadSystem> serviceProviders = new ArrayList<ArrowheadSystem>();
		List<ArrowheadSystem> intraProviders = new ArrayList<ArrowheadSystem>();
		List<ArrowheadSystem> authorizedIntraProviders = new ArrayList<ArrowheadSystem>();
		try{
			//Querying the Service Registry with the intra-cloud Store entries
			psList = queryServiceRegistry(srf.getRequestedService(), orchestrationFlags.get("metadataSearch"), 
					orchestrationFlags.get("pingProviders"));
			
			//Compile the list of providers which are in the Service Registry
			for(ProvidedService ps : psList){
				serviceProviders.add(ps.getProvider());
			}
			
			/*
			 * If the Store entry did not had a providerCloud, it must have had a providerSystem.
			 * We have to query the Authorization System with these providers.
			 */
			for(OrchestrationStore entry : intraStoreList){
				intraProviders.add(entry.getProviderSystem());
			}
			
			//Querying the Authorization System
			authorizedIntraProviders = queryAuthorization(srf.getRequesterSystem(), 
					srf.getRequestedService(), intraProviders);
		}
		/*
		 * If the SR or Authorization query throws DNFException, we have to catch it,
		 * because the inter-cloud store entries can still be viable options.
		 */
		catch(DataNotFoundException ex){
		}
		
		//Checking for viable providers in the Store entry list
		for(OrchestrationStore entry : entryList){
			//If the entry does not have a provider Cloud then it is an intra-cloud entry.
			if(entry.getProviderCloud() == null){
				/*
				 * Both of the provider lists (from SR and Auth query) need to contain the provider
				 * of the Store entry. We return with a provider if it fills this requirement.
				 */
				if(serviceProviders.contains(entry.getProviderSystem()) && 
						authorizedIntraProviders.contains(entry.getProviderSystem())){
					List<OrchestrationStore> tempList = new ArrayList<>(Arrays.asList(entry));
					return compileOrchestrationResponse(tempList, orchestrationFlags.get("generateToken"));
				}
			}
			/*
			 * Intra-Cloud store entries must be handlend inside the for loop, since every 
			 * provider Cloud means a different ICN process.
			 */
			else{
				try{
					ICNRequestForm icnRequestForm = compileICNRequestForm(srf, entry.getProviderCloud());
					ICNResult icnResult = startICN(icnRequestForm);
					//Use matchmaking on the ICN result if requested
					if(orchestrationFlags.get("matchmaking")){
						return icnMatchmaking(icnResult, icnRequestForm.getPreferredProviders());
					}
					else{
						return icnResult.getInstructions();
					}
				}
				/*
				 * If the ICN process failed on this store entry, we catch the exception
				 * and go to the next Store entry in the foor loop.
				 */
				catch(DataNotFoundException ex){
				}
			}
		}
		
		//If the foor loop finished but we still could not return a result, we throw a DNFException.
		throw new DataNotFoundException("OrchestrationFromStore failed.");
	}
	
	
	/**
	 * This method compiles the OrchestrationResponse object. 
	 * The regularOrchestration and externalServiceRequest processes use this version of the method. 
	 * (The triggerInterCloud method gets back the same response from an externalServiceRequest 
	 * at a remote Cloud.)
	 * 
	 * @param ArrowheadService service
	 * @param List<ProvidedService> psList
	 * @param boolean generateToken
	 * @return OrchestrationResponse
	 */
	private static OrchestrationResponse compileOrchestrationResponse(ArrowheadService service, 
			List<ProvidedService> psList, boolean generateToken){
		log.info("Entered the (first) compileOrchestrationResponse method.");
		
		String token = null;
		List<OrchestrationForm> ofList = new ArrayList<OrchestrationForm>();
		//We create an OrchestrationForm for every provider
		for(ProvidedService ps : psList){
			if(generateToken){
				//placeholder for token generation, call should be made to the AuthorizationResource
			}
			
			//Returning only those service interfaces that were found in the SR entry
			service.setInterfaces(ps.getServiceInterface());
			OrchestrationForm of = new OrchestrationForm(service, ps.getProvider(), 
					ps.getServiceURI(), token, null);
			ofList.add(of);
		}
		log.info("OrchestrationForm created for " + psList.size() + " providers.");
		
		//The OrchestrationResponse contains a list of OrchestrationForms
		return new OrchestrationResponse(ofList);
	}
	
	/**
	 * This method compiles the OrchestrationResponse object.
	 * Only the orchestrationFromStore method uses this version of the method.
	 * 
	 * @param List<OrchestrationStore> entryList
	 * @param boolean generateToken
	 * @return OrchestrationResponse
	 */
	private static OrchestrationResponse compileOrchestrationResponse(List<OrchestrationStore> entryList, 
			boolean generateToken){
		log.info("Entered the (second) compileOrchestrationResponse method.");
				
		String token = null;
		List<OrchestrationForm> ofList = new ArrayList<OrchestrationForm>();
		//We create an OrchestrationForm for every Store entry
		for(OrchestrationStore entry : entryList){
			if(generateToken){
				//placeholder for token generation, call should be made to the AuthorizationResource
			}
			
			OrchestrationForm of = new OrchestrationForm(entry.getService(), entry.getProviderSystem(), 
					null, token, entry.getOrchestrationRule());
			ofList.add(of);
		}
		log.info("OrchestrationForm created for " + entryList.size() + " providers.");
		
		//The OrchestrationResponse contains a list of OrchestrationForms
		return new OrchestrationResponse(ofList);
	}
	
	/**
	 * This method queries the Service Registry core system for a specific ArrowheadService.
	 * The returned list consists of possible service providers.
	 * 
	 * @param ArrowheadService service
	 * @param boolean metadataSearch
	 * @param boolean pingProviders
	 * @return List<ProvidedService>
	 */
	private static List<ProvidedService> queryServiceRegistry(ArrowheadService service,
			boolean metadataSearch, boolean pingProviders){
		log.info("Entered the queryServiceRegistry method.");
		
		//Compiling the URI and the request payload
		String srURI = SysConfig.getServiceRegistryURI();
		srURI = UriBuilder.fromPath(srURI).path(service.getServiceGroup())
				.path(service.getServiceDefinition()).toString();
		String tsig_key = SysConfig.getCoreSystem("serviceregistry").getAuthenticationInfo();
		ServiceQueryForm queryForm = new ServiceQueryForm(service.getServiceMetadata(), 
				service.getInterfaces(), pingProviders, metadataSearch, tsig_key);
		
		//Sending the request, parsing the returned result
		log.info("Querying ServiceRegistry for requested Service: " + service.toString());
		Response srResponse = Utility.sendRequest(srURI, "PUT", queryForm);
		ServiceQueryResult serviceQueryResult = srResponse.readEntity(ServiceQueryResult.class);
		if(serviceQueryResult == null){
			log.info("ServiceRegistry query came back empty. "
					+ "(OrchestratorService:queryServiceRegistry DataNotFoundException)");
			throw new DataNotFoundException("ServiceRegistry query came back empty for " 
					+ service.toString() + " (Interfaces field for service can not be empty)");
		}
		//If there are non-valid entries in the Service Registry response, we filter those out
		List<ProvidedService> temp = new ArrayList<ProvidedService>();
		for(ProvidedService ps: serviceQueryResult.getServiceQueryData()){
			if(!ps.isPayloadUsable()){
				temp.add(ps);
			}
		}
		serviceQueryResult.getServiceQueryData().removeAll(temp);
		
		if(serviceQueryResult.isPayloadEmpty()){
			log.info("ServiceRegistry query came back empty. "
					+ "(OrchestratorService:queryServiceRegistry DataNotFoundException)");
			throw new DataNotFoundException("ServiceRegistry query came back empty for service " 
					+ service.toString());
		}
		log.info("ServiceRegistry query successful. Number of providers: " 
				+ serviceQueryResult.getServiceQueryData().size());
		
		return serviceQueryResult.getServiceQueryData();
	}
	
	/**
	 * Intra-Cloud matchmaking method. As the last step of the local orchestration process
	 * (if requested) we pick out 1 provider from the remaining list. Providers preferred
	 * by the consumer have higher priority. Custom matchmaking algorithm can be implemented,
	 * as of now it just returns the first provider from the list.
	 * 
	 * @param List<ProvidedService> psList
	 * @param boolean onlyPreferred
	 * @param List<ArrowheadSystem> preferredList
	 * @param int notLocalSystems
	 * @return ProvidedService
	 */
	private static ProvidedService intraCloudMatchmaking(List<ProvidedService> psList, 
			boolean onlyPreferred, List<ArrowheadSystem> preferredList, int notLocalSystems){
		log.info("Entered the intraCloudMatchmaking method. psList size: " + psList.size());
		
		if(psList.isEmpty()){
			log.info("IntraCloudMatchmaking received an empty ProvidedService list. "
					+ "(OrchestratorService:intraCloudMatchmaking BadPayloadException)");
			throw new BadPayloadException("ProvidedService list is empty, Intra-Cloud matchmaking is "
					+ "not possible in the Orchestration process.");
		}
		
		//We delete all the preferredProviders from the list which belong to a remote cloud
		preferredList.subList(0, notLocalSystems).clear();
		log.info(notLocalSystems + " not local Systems deleted from the preferred list. "
				+ "Remaining providers: " + preferredList.size());
		
		//First we try to return with a preferred provider
		if(!preferredList.isEmpty()){	
			/*
			 * We iterate through both ArrowheadSystem list, and return with the proper ProvidedService
			 * object if we find a match.
			 */
			for(ArrowheadSystem system : preferredList){
				for(ProvidedService ps : psList){
					if(system.equals(ps.getProvider())){
						log.info("Preferred local System found in the list of ProvidedServices. "
								+ "Intra-Cloud matchmaking finished.");
						return ps;
					}
				}
			}
			
			//No match found, return the first ProvidedService entry if it is allowed.
			if(onlyPreferred){
				log.info("No preferred local System found in the list of ProvidedServices. "
						+ "Intra-Cloud matchmaking failed.");
				throw new DataNotFoundException("No preferred local System found in the "
						+ "list of ProvidedServices. Intra-Cloud matchmaking failed");
			}
			else{
				//Implement custom matchmaking algorithm here
				log.info("No preferred local System found in the list of ProvidedServices. "
						+ "Returning the first ProvidedService entry.");
				return psList.get(0);
			}
		}
		else{
			if(onlyPreferred){
				log.info("Bad request sent to the IntraCloudMatchmaking.");
				throw new BadPayloadException("Bad request sent to the Intra-Cloud matchmaking."
						+ "(onlyPreferred flag is true, but no local preferredProviders)");
			}
			else{
				/*
				 * If there are no preferences we return with the first possible choice by default.
				 * Custom matchmaking algorithm can be implemented here.
				 */
				log.info("No preferred providers were given, returning the first ProvidedService entry.");
				return psList.get(0);
			}
		}
	}
	
	/**
	 * This method filters out all the entries of the given ProvidedService list, which does not
	 * have a preferred provider.
	 * 
	 * @param List<ProvidedService> psList
	 * @param List<ArrowheadSystem> preferredProviders
	 * @return List<ProvidedService>
	 */
	private static List<ProvidedService> removeNonPreferred(List<ProvidedService> psList, 
			List<ArrowheadSystem> preferredProviders){
		log.info("Entered the removeNonPreferred method.");
		
		if(psList.isEmpty() || preferredProviders.isEmpty()){
			log.info("OrchestratorService:removeNonPreferred BadPayloadException");
			throw new BadPayloadException("ProvidedService or PreferredProviders list is empty. "
					+ "(OrchestrationService:removeNonPreferred BadPayloadException)");
		}
		
		List<ProvidedService> preferredList = new ArrayList<ProvidedService>();
		for(ArrowheadSystem system : preferredProviders){
			for(ProvidedService ps : psList){
				if(system.equals(ps.getProvider())){
					preferredList.add(ps);
				}
			}
		}
		
		if(preferredList.isEmpty()){
			log.info("OrchestratorService:removeNonPreferred DataNotFoundException");
			throw new DataNotFoundException("No preferred local System found in the the list of provider Systems. "
					+ "(OrchestrationService:removeNonPreferred DataNotFoundException)");
		}
		
		log.info("removeNonPreferred returns with " + preferredList.size() + " ProvidedServices.");
		return preferredList;
	}
	
	/**
	 * This method initiates the GSD process by sending a request to the Gatekeeper core system.
	 * 
	 * @param ArrowheadService requestedService
	 * @param List<ArrowheadCloud> preferredClouds
	 * @return GSDResult
	 */
	private static GSDResult startGSD(ArrowheadService requestedService, 
			List<ArrowheadCloud> preferredClouds){
		log.info("Entered the startGSD method.");
		
		//Compiling the URI and the request payload
		String URI = SysConfig.getGatekeeperURI();
		URI = UriBuilder.fromPath(URI).path("init_gsd").toString();
		GSDRequestForm requestForm = new GSDRequestForm(requestedService, preferredClouds);
		
		//Sending the request, do sanity check on the returned result
		Response response = Utility.sendRequest(URI, "PUT", requestForm);
		GSDResult result = response.readEntity(GSDResult.class);
		if(!result.isPayloadUsable()){
			log.info("GlobalServiceDiscovery yielded no result. "
					+ "(OrchestratorService:startGSD DataNotFoundException)");
			throw new DataNotFoundException("GlobalServiceDiscovery yielded no result.");
		}
		
		log.info(result.getResponse().size() + " gatekeeper(s) answered to the GSD poll.");
		return result;
	}
	
	/**
	 * Inter-Cloud matchmaking is mandaroty for picking out a target Cloud to do ICN with.
	 * Clouds preferred by the consumer have higher priority. Custom matchmaking algorithm 
	 * can be implemented, as of now it just returns the first Cloud from the list.
	 * 
	 * @param GSDResult result
	 * @param List<ArrowheadCloud> preferredClouds
	 * @param boolean onlyPreferred
	 * @return ArrowheadCloud
	 */
	private static ArrowheadCloud interCloudMatchmaking(GSDResult result, 
			List<ArrowheadCloud> preferredClouds, boolean onlyPreferred){
		log.info("Entered the interCloudMatchmaking method.");
		
		//Extracting the valid ArrowheadClouds from the GSDResult
		List<ArrowheadCloud> partnerClouds = new ArrayList<ArrowheadCloud>();
		for(GSDAnswer answer : result.getResponse()){
			if(answer.getProviderCloud().isValid()){
				partnerClouds.add(answer.getProviderCloud());
			}
		}
		
		//Using a set to remove duplicate entries from the preferredClouds list
		Set<ArrowheadCloud> prefClouds = new LinkedHashSet<>(preferredClouds);
		log.info("Number of partner Clouds from GSD: " + partnerClouds.size() + 
				", number of preferred Clouds from SRF: " + prefClouds.size());
		
		if(!prefClouds.isEmpty()){
			//We iterate through both ArrowheadCloud list, and return with 1 if we find a match.
			for(ArrowheadCloud preferredCloud : prefClouds){
				for(ArrowheadCloud partnerCloud : partnerClouds){
					if(preferredCloud.equals(partnerCloud)){
						log.info("Preferred Cloud found in the GSD response. "
								+ "Inter-Cloud matchmaking finished.");
						return partnerCloud;
					}
				}
			}
			
			//No match found, return the first ArrowheadCloud from the GSDResult if it is allowed.
			if(onlyPreferred){
				log.info("No preferred Cloud found in the GSD response. Inter-Cloud matchmaking failed.");
				throw new DataNotFoundException("No preferred Cloud found in the GSD response. "
						+ "Inter-Cloud matchmaking failed.");
			}
			else{
				//Implement custom matchmaking algorithm here
				log.info("No preferred Cloud found in the partner Clouds. "
						+ "Returning the first ProvidedService entry.");
				return partnerClouds.get(0);
			}
		}
		else{
			if(onlyPreferred){
				log.info("Bad request sent to the InterCloudMatchmaking.");
				throw new BadPayloadException("Bad request sent to the Inter-Cloud matchmaking."
						+ "(onlyPreferred flag is true, but no preferredClouds)");
			}
			else{
				/*
				 * If there are no preferences we return with the first possible choice by default.
				 * Custom matchmaking algorithm can be implemented here.
				 */
				log.info("No preferred Clouds were given, returning the first partner Cloud entry.");
				return partnerClouds.get(0);
			}
		}
	}
	
	/**
	 * This method initiates the ICN process by sending a request to the Gatekeeper core system.
	 * 
	 * @param ICNRequestForm requestForm
	 * @return ICNResult
	 */
	private static ICNResult startICN(ICNRequestForm requestForm){
		log.info("Entered the startICN method.");
		
		//Compiling the URI, sending the request, do sanity check on the returned result
		String URI = SysConfig.getGatekeeperURI();
		URI = UriBuilder.fromPath(URI).path("init_icn").toString();
		Response response = Utility.sendRequest(URI, "PUT", requestForm);
		ICNResult result = response.readEntity(ICNResult.class);
		if(!result.isPayloadUsable()){
			log.info("ICN yielded no result. (OrchestratorService:startICN DataNotFoundException)");
			throw new DataNotFoundException("ICN yielded no result.");
		}
		
		log.info(result.getInstructions().getResponse().size() + " possible providers in the ICN result.");
		return result;
	}
	
	/**
	 * This method queries the Authorization core system with a consumer/service/providerList triplet.
	 * The returned list only contains the authorized providers.
	 * 
	 * @param ArrowheadSystem consumer
	 * @param ArrowheadService service
	 * @param List<ArrowheadSystem> providerList
	 * @return List<ArrowheadSystem>
	 */
	private static List<ArrowheadSystem> queryAuthorization(ArrowheadSystem consumer, 
			ArrowheadService service, List<ArrowheadSystem> providerList) {
		log.info("Entered the queryAuthorization method.");
		
		//Compiling the URI and the request payload
		String URI = SysConfig.getAuthorizationURI();
		URI = UriBuilder.fromPath(URI).path("intracloud").toString();
		IntraCloudAuthRequest request = new IntraCloudAuthRequest(consumer, providerList, 
				service, false);
		log.info("Intra-Cloud authorization request ready to send to: " + URI);
		
		//Extracting the useful payload from the response, sending back the authorized Systems
		Response response = Utility.sendRequest(URI, "PUT", request);
		IntraCloudAuthResponse authResponse = response.readEntity(IntraCloudAuthResponse.class);
		List<ArrowheadSystem> authorizedSystems = new ArrayList<ArrowheadSystem>();
		for(Map.Entry<ArrowheadSystem, Boolean> entry : authResponse.getAuthorizationMap().entrySet()){
			if(entry.getValue())
				authorizedSystems.add(entry.getKey());
		}
		
		//Throwing exception if none of the providers are authorized for this consumer/service pair.
		if(authorizedSystems.isEmpty()){
			log.info("OrchestratorService:queryAuthorization throws DataNotFoundException");
			throw new DataNotFoundException("The consumer system is not authorized to receive servicing "
					+ "from any of the provider systems.");
		}
		
		log.info("Authorization query is done, sending back the authorized Systems. "
				+ authorizedSystems.size());
		return authorizedSystems;
	}
	
	/**
	 * Matchmaking method for ICN results. As the last step of the inter-cloud 
	 * orchestration process (if requested) we pick out 1 provider from the ICN result list. 
	 * Providers preferred by the consumer have higher priority. Custom matchmaking algorithm 
	 * can be implemented, as of now it just returns the first provider from the list.
	 * 
	 * @param ICNResult icnResult
	 * @return OrchestrationResponse
	 */
	private static OrchestrationResponse icnMatchmaking(ICNResult icnResult, 
			List<ArrowheadSystem> preferredProviders){
		log.info("Entered the icnMatchmaking method.");
		
		/*
		 * We first try to find a match between preferredProviders and the received
		 * providers from the ICN result.
		 */
		List<OrchestrationForm> ofList = new ArrayList<OrchestrationForm>();
		for(ArrowheadSystem preferredProvider : preferredProviders){
			for(OrchestrationForm of : icnResult.getInstructions().getResponse()){
				if(preferredProvider.equals(of.getProvider())){
					ofList.add(of);
					icnResult.getInstructions().setResponse(ofList);
					log.info("Preferred provider System found in the ICNResult, "
							+ "ICN matchmaking finished.");
					return icnResult.getInstructions();
				}
			}
		}
		
		//If that fails, we just select the first OrchestrationForm
		//Implement custom matchmaking algorithm here
		ofList.add(icnResult.getInstructions().getResponse().get(0));
		icnResult.getInstructions().setResponse(ofList);
		log.info("No preferred provider System was found in the ICNResult, "
				+ "returning the first OrchestrationForm entry.");
		return icnResult.getInstructions();
	}
	
	/**
	 * This method queries the Orchestration Store for entries where the consumer System
	 * our requester System from the ServiceRequestForm. The other 2 paramteres can further
	 * narrow down this list.
	 * 
	 * @param ArrowheadService consumer
	 * @param ArrowheadSystem service
	 * @param boolean onlyActive
	 * @return List<OrchestrationStore>
	 */
	private static List<OrchestrationStore> queryOrchestrationStore(ArrowheadService service,
			ArrowheadSystem consumer, boolean onlyActive){
		log.info("Entered the queryOrchestrationStore method.");
		
		//Compiling the URI and the request payload
		String URI = SysConfig.getOrchestratorURI();
		URI = UriBuilder.fromPath(URI).path("store").toString();
		OrchestrationStoreQuery query = new OrchestrationStoreQuery(service, consumer, onlyActive);
		
		//Sending the request, do sanity check on the returned result
		Response response = Utility.sendRequest(URI, "PUT", query);
		if(response.getStatus() == 404){
			ErrorMessage error = response.readEntity(ErrorMessage.class);
			throw new DataNotFoundException(error.getErrorMessage());
		}
		OrchestrationStoreQueryResponse storeResponse = 
				response.readEntity(OrchestrationStoreQueryResponse.class);
		
		log.info("Successfull Orchestration Store query, returning a list of " 
				+ storeResponse.getEntryList().size());
		return storeResponse.getEntryList();
	}
	
	/**
	 * From the given parameteres this method compiles an ICNRequestForm to start the ICN process.
	 * 
	 * @param ServiceRequestForm srf
	 * @param ArrowheadCloud targetCloud
	 * @return ICNRequestForm
	 */
	private static ICNRequestForm compileICNRequestForm(ServiceRequestForm srf, ArrowheadCloud targetCloud){
		log.info("Entered the compileICNRequestForm method.");
		
		List<ArrowheadSystem> preferredProviders = new ArrayList<ArrowheadSystem>();
		//Getting the preferred Providers which belong to the preferred Cloud
		for(int i = 0; i < srf.getPreferredClouds().size(); i++){
			if(srf.getPreferredClouds().get(i).equals(targetCloud)){
				//We might have a preferred Cloud but no preferred Provider inside the Cloud
				if(srf.getPreferredProviders().get(i) != null){
					preferredProviders.add(srf.getPreferredProviders().get(i));
				}
			}
		}
		log.info(preferredProviders.size() + " preferred providers selected for this Cloud.");
		
		//Compiling the payload
		Map<String, Boolean> negotiationFlags = new HashMap<String, Boolean>();
		negotiationFlags.put("onlyPreferred", srf.getOrchestrationFlags().get("onlyPreferred"));
		negotiationFlags.put("generateToken", srf.getOrchestrationFlags().get("generateToken"));
		ICNRequestForm requestForm = new ICNRequestForm(srf.getRequestedService(), null,
				targetCloud, srf.getRequesterSystem(), preferredProviders, negotiationFlags);
		
		return requestForm;
	}
	
	
}
