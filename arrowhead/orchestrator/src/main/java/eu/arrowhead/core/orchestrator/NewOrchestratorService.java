package eu.arrowhead.core.orchestrator;

import java.util.ArrayList;
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
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.GSDAnswer;
import eu.arrowhead.common.model.messages.GSDRequestForm;
import eu.arrowhead.common.model.messages.GSDResult;
import eu.arrowhead.common.model.messages.ICNRequestForm;
import eu.arrowhead.common.model.messages.ICNResult;
import eu.arrowhead.common.model.messages.OrchestrationForm;
import eu.arrowhead.common.model.messages.OrchestrationResponse;
import eu.arrowhead.common.model.messages.ProvidedService;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;
import eu.arrowhead.common.model.messages.ServiceRequestForm;

public final class NewOrchestratorService {
	
	private static Logger log = Logger.getLogger(NewOrchestratorService.class.getName());

	//TODO srf paraméter lecserélése csak azokra amit felhasználtam a public függvényekben
	//(ha csabival szükségesnek gondoljuk)
	/**
	 * 
	 * @param srf
	 * @return
	 */
	public static OrchestrationResponse externalServiceRequest(ServiceRequestForm srf){
		log.info("Entered the externalServiceRequest method.");
		
		Map<String, Boolean> orchestrationFlags = new HashMap<String, Boolean>();
		orchestrationFlags = srf.getOrchestrationFlags();
		
		//Querying the Service Registry
		ServiceQueryResult serviceQueryResult = queryServiceRegistry(srf.getRequestedService(), 
				orchestrationFlags.get("metadataSearch"), orchestrationFlags.get("pingProviders"));
		
		//If needed, removing the non-preferred providers from the SR response
		List<ProvidedService> psList = new ArrayList<ProvidedService>();
		psList = serviceQueryResult.getServiceQueryData();
		if(orchestrationFlags.get("onlyPreferred")){
			log.info("Only preferred matchmaking is requested.");
			psList = removeNonPreferred(psList, srf.getPreferredProviders());
		}
		
		//Do additional tasks here if needed (e.g. configuring the Network Manager)
		
		
		//Compiling the Orchestration Response
		return compileOrchestrationResponse(srf.getRequestedService(), 
				psList, orchestrationFlags.get("generateToken"));
	}
	
	/**
	 * 
	 * @param srf
	 * @return
	 */
	//TODO add more logging
	public static OrchestrationResponse triggerInterCloud(ServiceRequestForm srf){
		log.info("Entered the triggerInterCloud method.");
		
		Map<String, Boolean> orchestrationFlags = new HashMap<String, Boolean>();
		orchestrationFlags = srf.getOrchestrationFlags();
		
		//Telling the Gatekeeper to do a GSD
		GSDResult result = startGSD(srf.getRequestedService(), srf.getPreferredClouds());
		
		//Picking a target Cloud from the ones that responded to the GSD poll.
		ArrowheadCloud targetCloud = interCloudMatchmaking(result, srf.getPreferredClouds(), 
				orchestrationFlags.get("onlyPreferred"));
		
		ICNRequestForm requestForm = new ICNRequestForm(srf.getRequestedService(), "PLACEHOLDER AUTH INFO",
				targetCloud, srf.getRequesterSystem(), srf.getPreferredProviders(), 
				orchestrationFlags.get("onlyPreferred"));
		
		ICNResult icnResult = startICN(requestForm);
		return icnResult.getInstructions();
	}
	
	/**
	 * 
	 * @param service
	 * @param metadataSearch
	 * @param pingProviders
	 * @return
	 */
	private static ServiceQueryResult queryServiceRegistry(ArrowheadService service,
			boolean metadataSearch, boolean pingProviders){
		log.info("Entered the queryServiceRegistry method.");
		
		//Compiling the URI and the request payload
		String srURI = SysConfig.getServiceRegistryURI();
		srURI = UriBuilder.fromPath(srURI).path(service.getServiceGroup())
				.path(service.getServiceDefinition()).toString();
		String tsig_key = SysConfig.getCoreSystem("serviceregistry").getAuthenticationInfo();
		ServiceQueryForm queryForm = new ServiceQueryForm(service.getServiceMetadata(), 
				service.getInterfaces(), pingProviders, metadataSearch, tsig_key);
		
		//Sending the query, returning the result
		log.info("Querying ServiceRegistry for requested Service: " + service.getServiceDefinition());
		Response srResponse = Utility.sendRequest(srURI, "PUT", queryForm);
		ServiceQueryResult serviceQueryResult = srResponse.readEntity(ServiceQueryResult.class);
		
		if(serviceQueryResult.isPayloadEmpty()){
			log.info("ServiceRegistry query came back empty.");
			throw new DataNotFoundException("ServiceRegistry query came back empty.");
		}
		log.info("ServiceRegistry query successful. Number of providers: " 
				+ serviceQueryResult.getServiceQueryData().size());
		
		return serviceQueryResult;
	}
	
	/**
	 * 
	 * @param psList
	 * @param onlyPreferred
	 * @param preferredList
	 * @param notLocalSystems
	 * @return
	 */
	private static ProvidedService intraCloudMatchmaking(List<ProvidedService> psList, 
			boolean onlyPreferred, List<ArrowheadSystem> preferredList, int notLocalSystems){
		log.info("Entered the intraCloudMatchmaking method.");
		
		if(psList.size() == 0){
			log.info("IntraCloudMatchmaking received an empty ProvidedService list.");
			throw new BadPayloadException("ProvidedService list is empty, Intra-Cloud matchmaking is"
					+ "not possible in the Orchestration process.");
		}
		
		//We delete all the preferredProviders from the list which belong to another cloud
		preferredList.subList(0, notLocalSystems).clear();
		log.info(notLocalSystems + " not local Systems deleted from the preferred list.");
		
		if(!preferredList.isEmpty()){	
			/*
			 * We iterate through both ArrowheadSystem list, and return with the proper ProvidedService
			 * if we find a match.
			 */
			for(ArrowheadSystem system : preferredList){
				for(ProvidedService ps : psList){
					if(system.equals(ps.getProvider())){
						log.info("Preferred local System found in the Service Registry response. "
								+ "Intra-Cloud matchmaking finished.");
						return ps;
					}
				}
			}
			
			//No match found, return the first ProvidedService entry if it is allowed.
			if(onlyPreferred){
				log.info("No preferred local System found in the Service Registry response. "
						+ "Intra-Cloud matchmaking failed.");
				throw new DataNotFoundException("No preferred local System found in the "
						+ "Service Registry response. Intra-Cloud matchmaking failed");
			}
			else{
				//Implement custom matchmaking algorithm here
				log.info("No preferred local System found in the Service Registry response. "
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
	
	//this is only used on the externalServiceRequest branch
	//TODO decide if this is necessary. IF YES review it, do logging, etc
	private static List<ProvidedService> removeNonPreferred(List<ProvidedService> psList, 
			List<ArrowheadSystem> preferredProviders){
		if(psList.isEmpty() || preferredProviders.isEmpty()){
			throw new BadPayloadException("ProvidedService or PreferredProviders list is empty.");
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
			throw new DataNotFoundException("No preferred local System found in "
					+ "the Service Registry response.");
		}
		
		return preferredList;
	}
	
	/**
	 * 
	 * @param service
	 * @param psList
	 * @param generateToken
	 * @return
	 */
	private static OrchestrationResponse compileOrchestrationResponse(ArrowheadService service, 
			List<ProvidedService> psList, boolean generateToken){
		log.info("Entered the compileOrchestrationResponse method.");
		
		String token = null;
		List<OrchestrationForm> ofList = new ArrayList<OrchestrationForm>();
		for(ProvidedService ps : psList){
			if(generateToken){
				//placeholder, call should be made to the AuthorizationResource
			}
			
			OrchestrationForm of = new OrchestrationForm(service, ps.getProvider(), 
					ps.getServiceURI(), token);
			ofList.add(of);
		}
		log.info("OrchestrationForm created for " + psList.size() + " providers.");
		
		return new OrchestrationResponse(ofList);
	}
	
	/**
	 * 
	 * @param requestedService
	 * @param preferredClouds
	 * @return
	 */
	private static GSDResult startGSD(ArrowheadService requestedService, 
			List<ArrowheadCloud> preferredClouds){
		log.info("Entered the startGSD method.");
		
		String URI = SysConfig.getGatekeeperURI();
		URI = UriBuilder.fromPath(URI).path("init_gsd").toString();
		GSDRequestForm requestForm = new GSDRequestForm(requestedService, preferredClouds);
		
		Response response = Utility.sendRequest(URI, "PUT", requestForm);
		GSDResult result = response.readEntity(GSDResult.class);
		
		if(!result.isPayloadUsable()){
			log.info("GlobalServiceDiscovery yielded no result.");
			throw new DataNotFoundException("GlobalServiceDiscovery yielded no result.");
		}
		
		log.info(result.getResponse().size() + " gatekeeper(s) answered to the GSD poll.");
		return result;
	}
	
	/**
	 * 
	 * @param result
	 * @param preferredClouds
	 * @param onlyPreferred
	 * @return
	 */
	//TODO simplification might be possible, cause only preferredClouds respond to the
	//GSD if we have preferredClouds in the SRF/ICN reqform
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
		log.info("Partner cloud #: " + partnerClouds.size() + ", preferred cloud #: " + prefClouds.size());
		
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
	 * 
	 * @param requestForm
	 * @return
	 */
	private static ICNResult startICN(ICNRequestForm requestForm){
		log.info("Entered the startICN method.");
		
		String URI = SysConfig.getGatekeeperURI();
		URI = UriBuilder.fromPath(URI).path("init_icn").toString();
		Response response = Utility.sendRequest(URI, "PUT", requestForm);
		ICNResult result = response.readEntity(ICNResult.class);
		
		if(!result.isPayloadUsable()){
			log.info("ICN yielded no result.");
			throw new DataNotFoundException("ICN yielded no result.");
		}
		
		log.info(result.getInstructions().getResponse().size() + " possible providers in the ICN result.");
		return result;
	}
	
}
