package eu.arrowhead.core.orchestrator;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.ServiceMetadata;
import eu.arrowhead.common.model.messages.GSDResult;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
      return compileOrchestrationResponse(psList, srf, null);
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
  //TODO ha nem volt SRF-ben service, akkor getAllDefault mode-ban vagyunk, és minden SR-auth queryt túlélőt visszakéne adni
  //TODO meggyőződni compile hivasakor, hogy instructions.size = pslist.size, üres instructionök "" helyettesitésével
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
    Set<ArrowheadSystem> providerSystemsFromAuth = new HashSet<>();
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
     */ catch (DataNotFoundException ex) {
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
           * Setting up the SRF for the doInterCloudNegotiations method. In case of Store orchestration the preferences are the stored Cloud (and
           * System), and not what is inside the SRF payload (which should be null anyways when requesting Store orchestration).
           *
           * WARNING: Collections.singletonList creates an immutable List, any change to it will result in UnsupportedOperationException
					 */
          srf.setPreferredProviders(Collections.singletonList(new PreferredProvider(entry.getProviderSystem(), entry.getProviderCloud())));
          // Starting the ICN process
          ICNResult icnResult = OrchestratorDriver.doInterCloudNegotiations(srf, entry.getProviderCloud());

          // Use matchmaking on the ICN result. (Store orchestration will always only return 1 provider.)
          log.info("orchestrationFromStore returns with an inter-cloud Store entry");
          return icnMatchmaking(icnResult, Collections.singletonList(entry.getProviderSystem()), true);
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
   * Represents the orchestration process where the requester System only asked for Inter-Cloud servicing.
   *
   * @return OrchestrationResponse
   */
  static OrchestrationResponse triggerInterCloud(ServiceRequestForm srf) {
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
      // Getting the list of valid preferred systems from the ServiceRequestForm, which belong to the target cloud
      List<ArrowheadSystem> preferredSystems = new ArrayList<>();
      for (PreferredProvider provider : srf.getPreferredProviders()) {
        if (provider.isGlobal() && provider.getProviderCloud().equals(targetCloud) && provider.getProviderSystem() != null && provider
            .getProviderSystem().isValid()) {
          preferredSystems.add(provider.getProviderSystem());
        }
      }

      log.info("triggerInterCloud returns with 1 OrchestrationForm due to icnMatchmaking");
      return icnMatchmaking(icnResult, preferredSystems, false);
    } else {
      log.info("triggerInterCloud returns " + icnResult.getInstructions().getResponse().size() + " forms without icnMatchmaking");
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
    Map<String, Boolean> orchestrationFlags = srf.getOrchestrationFlags();

    // Querying the Service Registry to get the list of Provider Systems
    List<ProvidedService> psList = OrchestratorDriver
        .queryServiceRegistry(srf.getRequestedService(), orchestrationFlags.get("metadataSearch"), orchestrationFlags.get("pingProviders"));

    // If needed, removing the non-preferred providers from the SR response. (If needed, matchmaking is done after this at the request sender Cloud.)
    if (orchestrationFlags.get("onlyPreferred")) {
      // This SRF contains only local preferred systems, since this request came from another cloud, but the de-boxing is necessary
      Set<ArrowheadSystem> localPreferredSystems = new HashSet<>();
      for (PreferredProvider provider : srf.getPreferredProviders()) {
        if (provider.isLocal()) {
          localPreferredSystems.add(provider.getProviderSystem());
        }
      }
      psList = OrchestratorDriver.removeNonPreferred(psList, localPreferredSystems);
    }

    // Compiling the orchestration response
    log.info("externalServiceRequest finished with " + psList.size() + " service providers");
    return compileOrchestrationResponse(psList, srf, null);
  }

  /**
   * Matchmaking method for ICN results. As the last step of the inter-cloud orchestration process (if requested) we pick out 1 provider from the ICN
   * result list. Providers preferred by the consumer have higher priority. Custom matchmaking algorithm can be implemented, as of now it just returns
   * the first provider from the list.
   *
   * @return OrchestrationResponse
   */
  private static OrchestrationResponse icnMatchmaking(ICNResult icnResult, List<ArrowheadSystem> preferredSystems, boolean storeOrchestration) {
    // We first try to find a match between the preferred systems and the received providers from the ICN result.
    if (preferredSystems != null && !preferredSystems.isEmpty()) {
      for (ArrowheadSystem preferredProvider : preferredSystems) {
        for (OrchestrationForm of : icnResult.getInstructions().getResponse()) {
          if (preferredProvider.equals(of.getProvider())) {
            log.info("icnMatchmaking returns with a preferred System");
            return new OrchestrationResponse(Collections.singletonList(of));
          }
        }
      }
    }

    // Store based orchestration is "hard-wired", meaning only the stored provider System is acceptable
    if (storeOrchestration) {
      log.error("icnMatchmaking DataNotFoundException");
      throw new DataNotFoundException("The provider ArrowheadSystem from the Store entry was not found in the ICN result.");
    }
    // If it's not Store based, we just select the first OrchestrationForm, custom matchmaking algorithm can be implemented here
    else {
      log.info("icnMatchmaking returns with a not preferred System");
      return new OrchestrationResponse(Collections.singletonList(icnResult.getInstructions().getResponse().get(0)));
    }
  }

  /**
   * Compiles the OrchestrationResponse object. Potentially includes authorization token generation.
   *
   * @return OrchestrationResponse
   */
  private static OrchestrationResponse compileOrchestrationResponse(@NotNull List<ProvidedService> psList, @NotNull ServiceRequestForm srf,
                                                                    @Nullable List<String> instructions) {
    List<String> tokens = new ArrayList<>();
    List<String> signatures = new ArrayList<>();
    // Arrange token generation for every provider, if it was requested in the service metadata
    List<ServiceMetadata> metadata = srf.getRequestedService().getServiceMetadata();
    if (metadata.contains(new ServiceMetadata("security", "token"))) {
      // Getting all the provider Systems from the Service Registry entries
      List<ArrowheadSystem> providerList = new ArrayList<>();
      for (ProvidedService ps : psList) {
        providerList.add(ps.getProvider());
      }

      // Getting the Authorization token generation resource URI, compiling the request payload
      String authUri = Utility.getAuthorizationUri();
      authUri = UriBuilder.fromPath(authUri).path("token").toString();
      TokenGenerationRequest tokenRequest = new TokenGenerationRequest(srf.getRequesterSystem(), srf.getRequesterCloud(), providerList,
                                                                       srf.getRequestedService(), 0) ;
      //Sending request, parsing response
      Response authResponse = Utility.sendRequest(authUri, "PUT", tokenRequest);
      TokenGenerationResponse tokenResponse = authResponse.readEntity(TokenGenerationResponse.class);
      tokens = tokenResponse.getToken();
      signatures = tokenResponse.getSignature();
    }

    // Create an OrchestrationForm for every provider
    List<OrchestrationForm> ofList = new ArrayList<>();
    for (ProvidedService ps : psList) {
      OrchestrationForm of = new OrchestrationForm(ps.getOffered(), ps.getProvider(), ps.getServiceURI());
      ofList.add(of);
    }

    // Adding the Orchestration Store instructions (only in the case of Store orchestrations)
    if(instructions != null && instructions.size() == ofList.size()){
      for(int i = 0; i < instructions.size(); i++){
        ofList.get(i).setInstruction(instructions.get(i));
      }
    }
    // Adding the tokens and signatures, if token generation happened
    if(ofList.size() == tokens.size() && ofList.size() == signatures.size()) {
      for(int i = 0; i < tokens.size(); i++){
        ofList.get(i).setAuthorizationToken(tokens.get(i));
        ofList.get(i).setSignature(signatures.get(i));
      }
    }

    log.info("compileOrchestrationResponse creates " + ofList.size() + " orchestration form");
    return new OrchestrationResponse(ofList);
  }

}
