package eu.arrowhead.core.orchestrator;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.messages.GSDResult;
import eu.arrowhead.common.messages.ICNResult;
import eu.arrowhead.common.messages.OrchestrationForm;
import eu.arrowhead.common.messages.OrchestrationResponse;
import eu.arrowhead.common.messages.PreferredProvider;
import eu.arrowhead.common.messages.ServiceRequestForm;
import eu.arrowhead.common.messages.TokenData;
import eu.arrowhead.common.messages.TokenGenerationRequest;
import eu.arrowhead.common.messages.TokenGenerationResponse;
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
 * The package-private methods of this class represent the 4 different types of the orchestration process, while the 2 private methods are compiling
 * (or forwarding) the <tt>OrchestrationResponse</tt> which is sent back to the requester <tt>ArrowheadSystem</tt>.
 *
 * @author Umlauf Zoltán
 */
final class OrchestratorService {

  private static final Logger log = Logger.getLogger(OrchestratorService.class.getName());

  private OrchestratorService() throws AssertionError {
    throw new AssertionError("OrchestratorService is a non-instantiable class");
  }

  /**
   * Represents the regular orchestration process where the requester <tt>ArrowheadSystem</tt> is in the local Cloud. In this process the
   * <i>Orchestration Store</i> is ignored, and the Orchestrator first tries to find a provider for the requested service in the local Cloud. If that
   * fails but the <i>enableInterCloud</i> flag is set to true, the Orchestrator tries to find a provider in other Clouds.
   *
   * @throws DataNotFoundException if no local provider <tt>ArrowheadSystem</tt> is found and <i>enableInterCloud</i> is false
   */
  static OrchestrationResponse dynamicOrchestration(ServiceRequestForm srf) {
    Map<String, Boolean> orchestrationFlags = srf.getOrchestrationFlags();

    try {
      // Querying the Service Registry
      List<ServiceRegistryEntry> srList = OrchestratorDriver
          .queryServiceRegistry(srf.getRequestedService(), orchestrationFlags.get("metadataSearch"), orchestrationFlags.get("pingProviders"));

      // Cross-checking the SR response with the Authorization
      Set<ArrowheadSystem> providerSystems = new HashSet<>();
      for (ServiceRegistryEntry entry : srList) {
        providerSystems.add(entry.getProvider());
      }
      providerSystems = OrchestratorDriver.queryAuthorization(srf.getRequesterSystem(), srf.getRequestedService(), providerSystems);

      /*
       * The Authorization cross-check only returns the provider systems where the requester system is authorized to consume the service. We filter
       * out the non-authorized systems from the SR response (ServiceRegistryEntry list).
       */
      List<ServiceRegistryEntry> temp = new ArrayList<>();
      for (ServiceRegistryEntry entry : srList) {
        if (!providerSystems.contains(entry.getProvider())) {
          temp.add(entry);
        }
      }
      srList.removeAll(temp);

      // If needed, remove the non-preferred providers from the remaining list
      providerSystems.clear(); //providerSystems set is reused
      for (PreferredProvider provider : srf.getPreferredProviders()) {
        if (provider.isLocal()) {
          providerSystems.add(provider.getProviderSystem());
        }
      }
      if (orchestrationFlags.get("onlyPreferred")) {
        srList = OrchestratorDriver.removeNonPreferred(srList, providerSystems);
      }

      //placeholder step
      if (orchestrationFlags.get("enableQoS")) {
        srList = OrchestratorDriver.doQoSVerification(srList);
      }

      // If matchmaking is requested, we pick out 1 ServiceRegistryEntry entity from the list. Preferred Systems (2nd arg) have higher priority
      if (orchestrationFlags.get("matchmaking")) {
        ServiceRegistryEntry entry = OrchestratorDriver.intraCloudMatchmaking(srList, providerSystems);
        srList.clear();
        srList.add(entry);
      }

      //placeholder step
      if (orchestrationFlags.get("enableQoS")) {
        srList = OrchestratorDriver.doQosReservation(srList);
      }

      // All the filtering is done, need to compile the response
      log.info("dynamicOrchestration finished with " + srList.size() + " service providers");
      return compileOrchestrationResponse(srList, srf, null);
    }
    /*
     * If the Intra-Cloud orchestration fails somewhere (SR, Auth, filtering, matchmaking) we catch the exception, because Inter-Cloud
     * orchestration might be allowed. If not, we throw the same exception again.
     */ catch (ArrowheadException ex) {
      if (ex.getExceptionType().contains("DataNotFoundException") && !orchestrationFlags.get("enableInterCloud")) {
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
   * @throws DataNotFoundException if all the queried Orchestration Store entry options were exhausted and none were found operational
   */

  static OrchestrationResponse orchestrationFromStore(ServiceRequestForm srf) {
    // Querying the Orchestration Store for matching entries
    List<OrchestrationStore> entryList = OrchestratorDriver.queryOrchestrationStore(srf.getRequesterSystem(), srf.getRequestedService());

    // Cross-checking the results with the Service Registry and Authorization
    entryList = OrchestratorDriver.crossCheckStoreEntries(srf, entryList);

    // In case of default store orchestration, we return all the remaining Store entries (all intra-cloud, 1 provider/service)
    if (srf.getRequestedService() == null) {
      List<ServiceRegistryEntry> srList = new ArrayList<>();
      List<String> instructions = new ArrayList<>();
      for (OrchestrationStore entry : entryList) {
        srList.add(new ServiceRegistryEntry(entry.getService(), entry.getProviderSystem(), entry.getServiceURI()));
        instructions.add(entry.getInstruction());
      }

      return compileOrchestrationResponse(srList, srf, instructions);
    }
    // In case of non-default store orchestration (service is fixed), we go one by one on the entries until we find one operational
    else {
      for (OrchestrationStore entry : entryList) {
        // If the entry is intra-cloud, we can return with it, since it already passed the SR/Auth cross-checking
        if (entry.getProviderCloud() == null) {
          ServiceRegistryEntry service = new ServiceRegistryEntry(entry.getService(), entry.getProviderSystem(), entry.getServiceURI());
          return compileOrchestrationResponse(Collections.singletonList(service), srf, Collections.singletonList(entry.getInstruction()));
        } else {
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

            // Use matchmaking on the ICN result. (Non-default Store orchestration will always only return 1 provider.)
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
  }

  /**
   * Represents the orchestration process where the requester System only asked for Inter-Cloud servicing.
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
    for (OrchestrationForm of : icnResult.getOrchResponse().getResponse()) {
      of.setInstruction("This provider is from another cloud!");
    }

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
      log.info("triggerInterCloud returns " + icnResult.getOrchResponse().getResponse().size() + " forms without icnMatchmaking");
      return icnResult.getOrchResponse();
    }
  }

  /**
   * This method represents the orchestration process where the requester System is NOT in the local Cloud. This means that the Gatekeeper made sure
   * that this request from the remote Orchestrator can be satisfied in this Cloud. (Gatekeeper polled the Service Registry and Authorization
   * Systems.)
   */
  static OrchestrationResponse externalServiceRequest(ServiceRequestForm srf) {
    Map<String, Boolean> orchestrationFlags = srf.getOrchestrationFlags();

    // Querying the Service Registry to get the list of Provider Systems
    List<ServiceRegistryEntry> srList = OrchestratorDriver
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
      srList = OrchestratorDriver.removeNonPreferred(srList, localPreferredSystems);
    }

    // Compiling the orchestration response
    log.info("externalServiceRequest finished with " + srList.size() + " service providers");
    return compileOrchestrationResponse(srList, srf, null);
  }

  /**
   * Matchmaking method for ICN results. As the last step of the inter-cloud orchestration process (if requested) we pick out 1 provider from the ICN
   * result list. Providers preferred by the consumer have higher priority. Custom matchmaking algorithm can be implemented, as of now it just returns
   * the first provider from the list.
   *
   * @throws DataNotFoundException in case of Store orchestration, and the provider system from the database is not a match according to the
   *     remote cloud
   */
  private static OrchestrationResponse icnMatchmaking(ICNResult icnResult, List<ArrowheadSystem> preferredSystems, boolean storeOrchestration) {
    // We first try to find a match between the preferred systems and the received providers from the ICN result.
    if (preferredSystems != null && !preferredSystems.isEmpty()) {
      for (ArrowheadSystem preferredProvider : preferredSystems) {
        for (OrchestrationForm of : icnResult.getOrchResponse().getResponse()) {
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
      return new OrchestrationResponse(Collections.singletonList(icnResult.getOrchResponse().getResponse().get(0)));
    }
  }

  /**
   * Compiles the OrchestrationResponse object and returns it. Potentially includes token generation for authorization purposes.
   *
   * @param srList Service Registry entries, each containing a suitable provider <tt>ArrowheadSystem</tt>.
   * @param srf The <tt>ServiceRequestForm</tt> from the requester <tt>ArrowheadSystem</tt>, which is needed in case of token generation is
   *     requested.
   * @param instructions Optional additional information, which can be passed back to the requester <tt>ArrowheadSystem</tt>
   */
  private static OrchestrationResponse compileOrchestrationResponse(List<ServiceRegistryEntry> srList, ServiceRequestForm srf,
                                                                    List<String> instructions) {
    // Arrange token generation for every provider, if it was requested in the service metadata
    Map<String, String> metadata = srf.getRequestedService().getServiceMetadata();
    TokenGenerationResponse tokenResponse = null;
    if (metadata.containsKey("security") && metadata.get("security").equals("token")) {
      // Getting all the provider Systems from the Service Registry entries
      List<ArrowheadSystem> providerList = new ArrayList<>();
      for (ServiceRegistryEntry entry : srList) {
        providerList.add(entry.getProvider());
      }

      // Getting the Authorization token generation resource URI, compiling the request payload
      String authUri = Utility.getAuthorizationUri();
      authUri = UriBuilder.fromPath(authUri).path("token").toString();
      TokenGenerationRequest tokenRequest = new TokenGenerationRequest(srf.getRequesterSystem(), srf.getRequesterCloud(), providerList,
                                                                       srf.getRequestedService(), 0);
      //Sending request, parsing response
      Response authResponse = Utility.sendRequest(authUri, "PUT", tokenRequest);
      tokenResponse = authResponse.readEntity(TokenGenerationResponse.class);
    }

    // Create an OrchestrationForm for every provider
    List<OrchestrationForm> ofList = new ArrayList<>();
    for (ServiceRegistryEntry entry : srList) {
      OrchestrationForm of = new OrchestrationForm(entry.getProvidedService(), entry.getProvider(), entry.getServiceURI());
      ofList.add(of);
    }

    // Adding the Orchestration Store instructions (only in the case of Store orchestrations)
    if (instructions != null && instructions.size() == ofList.size()) {
      for (int i = 0; i < instructions.size(); i++) {
        ofList.get(i).setInstruction(instructions.get(i));
      }
    }
    // Adding the tokens and signatures, if token generation happened
    if (tokenResponse != null) {
      for (TokenData data : tokenResponse.getTokenData()) {
        for (OrchestrationForm of : ofList) {
          if (data.getSystem().equals(of.getProvider())) {
            of.setAuthorizationToken(data.getToken());
            of.setSignature(data.getSignature());
          }
        }
      }
    }

    for (OrchestrationForm of : ofList) {
      log.debug("Service: " + of.getService().toString() + " System: " + of.getProvider().toStringLog() + " ServiceURI: " + of.getServiceURI()
                    + " Instruction: " + of.getInstruction() + " Token: " + of.getAuthorizationToken() + " Signature: " + of.getSignature());
    }
    log.info("compileOrchestrationResponse creates " + ofList.size() + " orchestration form");
    return new OrchestrationResponse(ofList);
  }

}
