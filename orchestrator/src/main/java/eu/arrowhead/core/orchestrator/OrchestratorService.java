package eu.arrowhead.core.orchestrator;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.ServiceMetadata;
import eu.arrowhead.common.model.messages.GSDResult;
import eu.arrowhead.common.model.messages.ICNRequestForm;
import eu.arrowhead.common.model.messages.ICNResult;
import eu.arrowhead.common.model.messages.OrchestrationForm;
import eu.arrowhead.common.model.messages.OrchestrationResponse;
import eu.arrowhead.common.model.messages.PreferredProvider;
import eu.arrowhead.common.model.messages.ProvidedService;
import eu.arrowhead.common.model.messages.ServiceRequestForm;
import eu.arrowhead.common.model.messages.TokenGenerationRequest;
import eu.arrowhead.common.model.messages.TokenGenerationResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;

/**
 * TODO
 *
 * @author Umlauf Zoltán
 */
final class OrchestratorService {

  private static Logger log = Logger.getLogger(OrchestratorService.class.getName());

  private OrchestratorService() throws AssertionError {
    throw new AssertionError("OrchestratorService is a non-instantiable class");
  }

  /**
   * Represents the regular orchestration process where the requester <tt>ArrowheadSystem</tt> is in the local Cloud. In this process the
   * <i>Orchestration Store</i> is ignored, and the Orchestrator first tries to find a provider in the local Cloud. If that fails but the
   * <i>enableInterCloud</i> flag is set to true, the Orchestrator tries to find a provider in other Clouds.
   *
   * @return OrchestrationResponse
   */
  static OrchestrationResponse dynamicOrchestration(ServiceRequestForm srf) {
    Map<String, Boolean> orchestrationFlags = srf.getOrchestrationFlags();

    try {
      // Querying the Service Registry
      List<ProvidedService> psList = OrchestratorDriver
          .queryServiceRegistry(srf.getRequestedService(), orchestrationFlags.get("metadataSearch"), orchestrationFlags.get("pingProviders"));

      // Cross-checking the SR response with the Authorization
      Set<ArrowheadSystem> providerSystems = new HashSet<>();
      for (ProvidedService service : psList) {
        providerSystems.add(service.getProvider());
      }
      providerSystems = OrchestratorDriver.queryAuthorization(srf.getRequesterSystem(), srf.getRequestedService(), providerSystems);

      /*
       * The Authorization cross-check only returns the provider systems where the requester system is authorized to consume the service. We filter
       * out the non-authorized systems from the SR response (ProvidedService list).
       */
      List<ProvidedService> temp = new ArrayList<>();
      for (ProvidedService service : psList) {
        if (!providerSystems.contains(service.getProvider())) {
          temp.add(service);
        }
      }
      psList.removeAll(temp);

      // If needed, remove the non-preferred providers from the remaining list
      providerSystems.clear(); //providerSystems set is reused
      for (PreferredProvider provider : srf.getPreferredProviders()) {
        if (provider.isLocal()) {
          providerSystems.add(provider.getProviderSystem());
        }
      }
      if (orchestrationFlags.get("onlyPreferred")) {
        psList = OrchestratorDriver.removeNonPreferred(psList, providerSystems);
      }

      //placeholder step
      if (orchestrationFlags.get("enableQoS")) {
        psList = OrchestratorDriver.doQoSVerification(psList);
      }

      // If matchmaking is requested, we pick out 1 ProvidedService entity from the list. Preferred Systems (2nd arg) have higher priority
      if (orchestrationFlags.get("matchmaking")) {
        ProvidedService ps = OrchestratorDriver.intraCloudMatchmaking(psList, providerSystems);
        psList.clear();
        psList.add(ps);
      }

      //placeholder step
      if (orchestrationFlags.get("enableQoS")) {
        psList = OrchestratorDriver.doQosReservation(psList);
      }

      // All the filtering is done, need to compile the response
      log.info("dynamicOrchestration finished with " + psList.size() + " service providers");
      return compileOrchestrationResponse(psList, srf.getRequesterSystem(), srf.getRequesterCloud(), srf.getRequestedService());
    }
    /*
     * If the Intra-Cloud orchestration fails somewhere (SR, Auth, filtering, matchmaking) we catch the exception, because Inter-Cloud
     * orchestration might be allowed. If not, we throw the same exception again.
     */ catch (DataNotFoundException ex) {
      if (!orchestrationFlags.get("enableInterCloud")) {
        log.error("dynamicOrchestration: Intra-Cloud orchestration failed with DataNotFoundException, Inter-Cloud is not allowed.");
        throw new DataNotFoundException(ex.getMessage());
      }
    }

    /*
     * If the code reaches this part, that means the Intra-Cloud orchestration failed, but the Inter-Cloud orchestration is allowed, so we try that
     * too.
		 */
    log.info("dynamicOrchestration: Intra-Cloud orchestration failed, moving to Inter-Cloud orchestration.");
    return triggerInterCloud(srf);
  }

  /**
   * Represents the orchestration process where the <i>Orchestration Store</i> database is used to see if there is a provider for the requester
   * <tt>ArrowheadSystem</tt>. The <i>Orchestration Store</i> contains preset orchestration information, which should not change in runtime.
   *
   * @return OrchestrationResponse
   */
  static OrchestrationResponse orchestrationFromStore(ServiceRequestForm srf) {
    // Querying the Orchestration Store for matching entries
    List<OrchestrationStore> entryList = OrchestratorDriver.queryOrchestrationStore(srf.getRequesterSystem(), srf.getRequestedService());

		/*
     * Before we iterate through the entry list to pick out a provider, we have to poll the Service Registry and Authorization Systems, so we have
     * these 2 other ArrowheadSystem provider lists to cross-check the entry list with. The try/catch block is needed, since inter-cloud
     * orchestration can still be a possibility, even if the local SR or Auth query comes back empty.
		 */
    Map<String, Boolean> orchestrationFlags = srf.getOrchestrationFlags();
    Set<ArrowheadSystem> providerSystemsFromSR = new HashSet<>();
    List<ArrowheadSystem> providerSystemsFromAuth = new ArrayList<>();
    try {
      // Querying the Service Registry for the intra-cloud Store entries
      List<ProvidedService> psList = OrchestratorDriver
          .queryServiceRegistry(srf.getRequestedService(), orchestrationFlags.get("metadataSearch"), orchestrationFlags.get("pingProviders"));

      // Compile the list of provider systems which are in the Service Registry

      for (ProvidedService ps : psList) {
        providerSystemsFromSR.add(ps.getProvider());
      }

			/*
       * If the Store entry did not had a providerCloud, it must have had a providerSystem. We have to query the Authorization System with these
       * local providers systems.
			 */
      Set<ArrowheadSystem> localProviderSystems = new HashSet<>();
      for (OrchestrationStore entry : entryList) {
        if (entry.getProviderCloud() == null) {
          localProviderSystems.add(entry.getProviderSystem());
        }
      }

      // Querying the Authorization System
      providerSystemsFromAuth = OrchestratorDriver.queryAuthorization(srf.getRequesterSystem(), srf.getRequestedService(), localProviderSystems);

      //TODO do qosVerification here
    }
    /*
     * If the SR or Authorization query throws DataNotFoundException, we have to catch it, because the inter-cloud store entries can still be
     * viable options.
     */
    catch (DataNotFoundException ex) {
      log.info("orchestrationFromStore catches DataNotFoundException at local SR/Auth query");
    }

    // Checking for viable providers in the Store entry list
    for (OrchestrationStore entry : entryList) {
      // If the entry does not have a provider Cloud then it is an intra-cloud entry.
      if (entry.getProviderCloud() == null) {
        /*
         * Both of the provider lists (from SR and Auth query) need to contain the provider of the Store entry. We return with a provider if it
         * fills this requirement. (Store orchestration will always only return 1 provider.)
				 */
        if (providerSystemsFromSR.contains(entry.getProviderSystem()) && providerSystemsFromAuth.contains(entry.getProviderSystem())) {
          //TODO do qosReserve here
          log.info("orchestrationFromStore returns with a local Store entry");
          return compileOrchestrationResponse(entry);
        }
      }
      // Inter-Cloud store entries must be handled inside the for loop, since every provider Cloud means a different ICN process.
      else {
        try {
          /*
           * Setting up the SRF for the compileICNRequestForm method. In case of Store orchestration the preferences are the stored Cloud (and
           * System), and not what is inside the SRF payload (which should be null anyways when requesting Store orchestration).
           *
           * WARNING: Collections.singletonList creates an immutable List, any change to it will result in UnsupportedOperationException
					 */
          srf.setPreferredProviders(Collections.singletonList(new PreferredProvider(entry.getProviderSystem(), entry.getProviderCloud())));
          ICNRequestForm icnRequestForm = OrchestratorDriver.compileICNRequestForm(srf, entry.getProviderCloud());

          // Starting the ICN process
          ICNResult icnResult = OrchestratorDriver.doInterCloudNegotiations(icnRequestForm);

          // Use matchmaking on the ICN result. (Store orchestration will always only return 1 provider.)
          log.info("orchestrationFromStore returns with an inter-cloud Store entry");
          return icnMatchmaking(icnResult, entry);
        }
        // If the ICN process failed on this store entry, we catch the exception and go to the next Store entry in the for-loop.
        catch (DataNotFoundException ex) {
          log.info("orchestrationFromStore catches DataNotFoundException at ICN process, going to the next Store entry");
        }
      }
    }

    // If the for-loop finished but we still could not return a result, we throw a DataNotFoundException.
    log.error("orchestrationFromStore throws final DataNotFoundException");
    throw new DataNotFoundException("OrchestrationFromStore failed with all " + entryList.size() + " queried Store entries.");
  }

  /**
   * This method represents the orchestration process where the requester System only asked for Inter-Cloud servicing.
   *
   * @return OrchestrationResponse
   */
  static OrchestrationResponse triggerInterCloud(ServiceRequestForm srf) {
    log.info("Entered the triggerInterCloud method.");

    Map<String, Boolean> orchestrationFlags = srf.getOrchestrationFlags();

    // Extracting the valid and unique ArrowheadClouds from the preferred providers
    List<ArrowheadCloud> preferredClouds = new ArrayList<>();
    for (PreferredProvider provider : srf.getPreferredProviders()) {
      if (provider.isGlobal() && !preferredClouds.contains(provider.getProviderCloud())) {
        preferredClouds.add(provider.getProviderCloud());
      }
    }

    // Telling the Gatekeeper to do a Global Service Discovery
    GSDResult result = OrchestratorDriver.doGlobalServiceDiscovery(srf.getRequestedService(), preferredClouds);

    // Picking a target Cloud from the ones that responded to the GSD poll
    ArrowheadCloud targetCloud = OrchestratorDriver.interCloudMatchmaking(result, preferredClouds, orchestrationFlags.get("onlyPreferred"));

    // Telling the Gatekeeper to start the Inter-Cloud Negotiations process
    ICNResult icnResult = OrchestratorDriver.doInterCloudNegotiations(srf, targetCloud);

    // If matchmaking is requested, we pick one provider from the ICN result
    if (orchestrationFlags.get("matchmaking")) {
      return icnMatchmaking(icnResult, icnRequestForm.getPreferredProviders());
    } else {
      return icnResult.getInstructions();
    }
  }

  /**
   * This method represents the orchestration process where the requester System is NOT in the local Cloud. This means that the Gatekeeper made sure
   * that this request from the remote Orchestrator can be satisfied in this Cloud. (Gatekeeper polled the Service Registry and Authorization
   * Systems.)
   *
   * @return OrchestrationResponse
   */
  static OrchestrationResponse externalServiceRequest(ServiceRequestForm srf) {
    log.info("Entered the externalServiceRequest method.");
    Map<String, Boolean> orchestrationFlags = srf.getOrchestrationFlags();

    // Querying the Service Registry to get the list of Provider Systems
    List<ProvidedService> psList = OrchestratorDriver
        .queryServiceRegistry(srf.getRequestedService(), orchestrationFlags.get("metadataSearch"), orchestrationFlags.get("pingProviders"));

		/*
     * If needed, removing the non-preferred providers from the SR response.
		 * (If needed, matchmaking is done at the request sender Cloud.)
		 */
    if (orchestrationFlags.get("onlyPreferred")) {
      log.info("Only preferred matchmaking is requested.");
      // This SRF contains only local preferred systems, since this request came from another cloud, but the de-boxing is necessary
      List<ArrowheadSystem> localPreferredSystems = new ArrayList<>();
      for (PreferredProvider provider : srf.getPreferredProviders()) {
        if (provider.isLocal()) {
          localPreferredSystems.add(provider.getProviderSystem());
        }
      }
      psList = OrchestratorDriver.removeNonPreferred(psList, localPreferredSystems);
    }

    // Compiling the orchestration response
    return compileOrchestrationResponse(psList, true, srf);
  }

  /**
   * Matchmaking method for ICN results. As the last step of the inter-cloud orchestration process (if requested) we pick out 1 provider from the ICN
   * result list. Providers preferred by the consumer have higher priority. Custom matchmaking algorithm can be implemented, as of now it just returns
   * the first provider from the list.
   *
   * @return OrchestrationResponse
   */
  private static OrchestrationResponse icnMatchmaking(ICNResult icnResult, List<ArrowheadSystem> preferredProviders) {
    log.info("Entered the (first) icnMatchmaking method.");

		/*
     * We first try to find a match between preferredProviders and the
		 * received providers from the ICN result.
		 */
    List<OrchestrationForm> ofList = new ArrayList<>();
    if (preferredProviders != null && !preferredProviders.isEmpty()) {
      for (ArrowheadSystem preferredProvider : preferredProviders) {
        for (OrchestrationForm of : icnResult.getInstructions().getResponse()) {
          if (preferredProvider.equals(of.getProvider())) {
            ofList.add(of);
            icnResult.getInstructions().setResponse(ofList);
            log.info("Preferred provider System found in the ICNResult, " + "ICN matchmaking finished.");
            return icnResult.getInstructions();
          }
        }
      }
    }

    // If that fails, we just select the first OrchestrationForm
    // Implement custom matchmaking algorithm here
    ofList.add(icnResult.getInstructions().getResponse().get(0));
    icnResult.getInstructions().setResponse(ofList);
    log.info("No preferred provider System was found in the ICNResult, " + "returning the first OrchestrationForm entry.");
    return icnResult.getInstructions();
  }

  /**
   * Matchmaking method for ICN results. This version of the method is used by the orchestrationFromStore method. The method searches for the provider
   * (which was given in the Store entry) in the ICN result.
   *
   * @return OrchestrationResponse
   */
  //TODO összevonható a másikkal
  private static OrchestrationResponse icnMatchmaking(ICNResult icnResult, OrchestrationStore entry) {
    log.info("Entered the (second) icnMatchmaking method.");

    List<OrchestrationForm> ofList = new ArrayList<>();
    for (OrchestrationForm of : icnResult.getInstructions().getResponse()) {
      if (entry.getProviderSystem().equals(of.getProvider())) {
        ofList.add(of);
        icnResult.getInstructions().setResponse(ofList);
        log.info("Preferred provider System found in the ICNResult, " + "ICN matchmaking finished.");
        return icnResult.getInstructions();
      }
    }

    log.info("Second icnMatchmaking method throws DataNotFoundException");
    throw new DataNotFoundException("The given provider in the Store " + "entry was not found in the ICN result.");
  }

  /**
   * This method compiles the OrchestrationResponse object. The regularOrchestration and externalServiceRequest processes use this version of the
   * method. (The triggerInterCloud method gets back the same response from an externalServiceRequest at a remote Cloud.)
   *
   * @return OrchestrationResponse
   */
  //TODO generate token megszüntetése, srf.service metadata (security - token ) alapján kell generálásról dönteni
  private static OrchestrationResponse compileOrchestrationResponse(List<ProvidedService> psList, ArrowheadSystem requesterSystem,
                                                                    ArrowheadCloud requesterCloud, ArrowheadService requestedService) {
    List<OrchestrationForm> ofList = new ArrayList<>();
    List<ArrowheadSystem> providerList = new ArrayList<>();

    for (ProvidedService ps : psList) {
      providerList.add(ps.getProvider());
    }

    TokenGenerationResponse tokenResponse = null;
    List<String> tokens = new ArrayList<>();
    List<String> signatures = new ArrayList<>();
    List<ServiceMetadata> metadata = requestedService.getServiceMetadata();
    if (metadata.contains(new ServiceMetadata("security", "token"))) {
      String authURI = Utility.getAuthorizationUri();
      authURI = UriBuilder.fromPath(authURI).path("token").toString();
      TokenGenerationRequest tokenRequest = new TokenGenerationRequest(requesterSystem, requesterCloud, providerList, requestedService, 0);

      Response authResponse = Utility.sendRequest(authURI, "PUT", tokenRequest);
      tokenResponse = authResponse.readEntity(TokenGenerationResponse.class);
      tokens = tokenResponse.getToken();
      signatures = tokenResponse.getSignature();
    }

    // We create an OrchestrationForm for every provider
    for (int i = 0; i < psList.size(); i++) {
      OrchestrationForm of = new OrchestrationForm(psList.get(i).getOffered(), psList.get(i).getProvider(), psList.get(i).getServiceURI(),
                                                   tokens.get(i), null, signatures.get(i));
      ofList.add(of);
    }

    log.info("OrchestrationForm created for " + psList.size() + " providers.");

    // The OrchestrationResponse contains a list of OrchestrationForms
    return new OrchestrationResponse(ofList);
  }

  /**
   * This method compiles the OrchestrationResponse object. Only the orchestrationFromStore method uses this version of the method.
   *
   * @return OrchestrationResponse
   */
  private static OrchestrationResponse compileOrchestrationResponse(OrchestrationStore entry) {
    log.info("Entered the (second) compileOrchestrationResponse method.");

    String token = null;
    List<OrchestrationForm> ofList = new ArrayList<>();
    // We create an OrchestrationForm for every Store entry
    for (OrchestrationStore entry : entryList) {
      if (generateToken) {
        // placeholder for token generation, call should be made to the
        // AuthorizationResource
      }

      //OrchestrationForm of = new OrchestrationForm(entry.getService(), entry.getProviderSystem(), null, token,entry.getOrchestrationRule());
      OrchestrationForm of = new OrchestrationForm();
      ofList.add(of);
    }
    log.info("OrchestrationForm created for " + entryList.size() + " providers.");

    // The OrchestrationResponse contains a list of OrchestrationForms
    return new OrchestrationResponse(ofList);
  }

}
