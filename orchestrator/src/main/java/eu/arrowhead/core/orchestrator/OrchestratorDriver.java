package eu.arrowhead.core.orchestrator;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.exception.DataNotFoundException;
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
import eu.arrowhead.common.model.messages.PreferredProvider;
import eu.arrowhead.common.model.messages.ProvidedService;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;
import eu.arrowhead.common.model.messages.ServiceRequestForm;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Contains miscellaneous helper functions for the Orchestration process. The main functions of the Orchestration process used by the REST resource
 * are in {@link eu.arrowhead.core.orchestrator.OrchestratorService}.
 *
 * @author Umlauf Zoltán
 */
final class OrchestratorDriver {

  private static Logger log = Logger.getLogger(OrchestratorService.class.getName());

  private OrchestratorDriver() throws AssertionError {
    throw new AssertionError("OrchestratorDriver is a non-instantiable class");
  }

  /**
   * Queries the Service Registry Core System for a specific <tt>ArrowheadService</tt>.
   *
   * @param service The <tt>ArrowheadService</tt> object for which the list of potential <tt>ArrowheadSystem</tt> providers are needed
   * @param metadataSearch If true, the stored <tt>ArrowheadService</tt>s have to have the same metadata to be returned in the response.
   * @param pingProviders If true, the Service Registry is asked to ping the service provider <tt>ArrowheadSystem</tt> (where the service is
   *     offered) to check if a TCP connection can be established or not. Normally providers have to remove their offered services from the Service
   *     Registry before going offline, but this feature can be used to ensure offline providers are filtered out.
   *
   * @return list of potential service providers with their offered services (interfaces, metadata, service URI)
   *
   * @throws DataNotFoundException if the Service Registry response list is empty
   */
  static List<ProvidedService> queryServiceRegistry(ArrowheadService service, boolean metadataSearch, boolean pingProviders) {
    // Compiling the URI and the request payload
    //TODO probably changed
    String srUri = UriBuilder.fromPath(Utility.getServiceRegistryUri()).path(service.getServiceGroup()).path(service.getServiceDefinition())
        .toString();
    String tsigKey = Utility.getCoreSystem("serviceregistry").getAuthenticationInfo();
    ServiceQueryForm queryForm = new ServiceQueryForm(service, pingProviders, metadataSearch, tsigKey);

    // Sending the request, parsing the returned result
    Response srResponse = Utility.sendRequest(srUri, "PUT", queryForm);
    ServiceQueryResult serviceQueryResult = srResponse.readEntity(ServiceQueryResult.class);
    //TODO probably cant be null anymore?
    if (serviceQueryResult == null || serviceQueryResult.isValid()) {
      log.error("queryServiceRegistry DataNotFoundException");
      throw new DataNotFoundException("ServiceRegistry query came back empty for " + service.toString());
    }

    // If there are non-valid entries in the Service Registry response, we filter those out
    List<ProvidedService> temp = new ArrayList<>();
    for (ProvidedService ps : serviceQueryResult.getServiceQueryData()) {
      if (!ps.isValid()) {
        temp.add(ps);
      }
    }
    serviceQueryResult.getServiceQueryData().removeAll(temp);

    log.info("queryServiceRegistry was successful, number of potential providers for" + service.toString() + " is " + serviceQueryResult
        .getServiceQueryData().size());
    return serviceQueryResult.getServiceQueryData();
  }

  /**
   * Queries the Authorization Core System to see which provider <tt>ArrowheadSystem</tt>s are authorized to offer their services to the consumer.
   *
   * @param consumer The <tt>ArrowheadSystem</tt> object representing the consumer system
   * @param service The <tt>ArrowheadService</tt> object representing the service to be consumed
   * @param providerSet The set of <tt>ArrowheadSystem</tt> objects representing the potential provider systems
   *
   * @return list of the authorized provider <tt>ArrowheadSystem</tt>s
   *
   * @throws DataNotFoundException if none of the provider <tt>ArrowheadSystem</tt>s are authorized for this servicing
   */
  static List<ArrowheadSystem> queryAuthorization(ArrowheadSystem consumer, ArrowheadService service, Set<ArrowheadSystem> providerSet) {
    // Compiling the URI and the request payload
    String uri = UriBuilder.fromPath(Utility.getAuthorizationUri()).path("intracloud").toString();
    IntraCloudAuthRequest request = new IntraCloudAuthRequest(consumer, providerSet, service);

    // Sending the request, parsing the returned result
    Response response = Utility.sendRequest(uri, "PUT", request);
    IntraCloudAuthResponse authResponse = response.readEntity(IntraCloudAuthResponse.class);
    List<ArrowheadSystem> authorizedSystems = new ArrayList<>();
    // Set view of HashMap ensures there are no duplicates between the keys (systems)
    for (Map.Entry<ArrowheadSystem, Boolean> entry : authResponse.getAuthorizationMap().entrySet()) {
      if (entry.getValue()) {
        authorizedSystems.add(entry.getKey());
      }
    }

    // Throwing exception if none of the providers are authorized for this consumer/service pair.
    if (authorizedSystems.isEmpty()) {
      log.error("queryAuthorization DataNotFoundException");
      throw new DataNotFoundException("The consumer system is not authorized to receive servicing from any of the provider systems.");
    }

    log.info("queryAuthorization is done, sending back " + authorizedSystems.size() + " authorized Systems");
    return authorizedSystems;
  }

  /**
   * Filters out all the entries of the given ProvidedService list, which does not contain a preferred local <tt>ArrowheadSystem</tt>. This method is
   * called when the <i>onlyPreferred</i> orchestration flag is set to true.
   *
   * @param psList The list of <tt>ProvidedService</tt>s still being considered (after SR query and possibly Auth query)
   * @param preferredLocalProviders A list of local <tt>ArrowheadSystem</tt>s preferred by the requester <tt>ArrowheadSystem</tt>. This is a
   *     sublist of the <i>preferredProviders</i> list from the {@link eu.arrowhead.common.model.messages.ServiceRequestForm}, which can also contain
   *     not local <tt>ArrowheadSystem</tt>s.
   *
   * @return a list of <tt>ProvidedService</tt>s which have preferred provider <tt>ArrowheadSystem</tt>s
   *
   * @throws DataNotFoundException if none of the <tt>ProvidedService</tt>s from the given list contain a preferred <tt>ArrowheadSystem</tt>
   */
  static List<ProvidedService> removeNonPreferred(List<ProvidedService> psList, List<ArrowheadSystem> preferredLocalProviders) {
    // Using a simple nested for-loop for the filtering
    List<ProvidedService> preferredList = new ArrayList<>();
    for (ArrowheadSystem system : preferredLocalProviders) {
      for (ProvidedService ps : psList) {
        if (system.equals(ps.getProvider())) {
          preferredList.add(ps);
        }
      }
    }

    if (preferredList.isEmpty()) {
      log.error("removeNonPreferred DataNotFoundException");
      throw new DataNotFoundException("No preferred local System was found in the the list of potential provider Systems.");
    }

    log.info("removeNonPreferred returns with " + preferredList.size() + " ProvidedServices.");
    return preferredList;
  }

  static List<ProvidedService> doQoSVerification(List<ProvidedService> psList) {
    //placeholder for actual implementation
    return psList;
  }

  /**
   * As the last step of the local orchestration process (if requested with the <i>matchmaking</i> orchestration flag) we pick out 1 provider from the
   * remaining list. Providers preferred by the consumer have higher priority. Custom matchmaking algorithm can be implemented here, as of now it just
   * returns the first (preferred) provider from the list.
   * <p>
   * If the <i>onlyPreferred</i> orchestration flag is set to true, then it is guaranteed there will be at least 1 preferred provider to choose from,
   * since this method is called after {@link #removeNonPreferred(List, List)}, where a {@link eu.arrowhead.common.exception.DataNotFoundException} is
   * thrown if no preferred provider was found.
   *
   * @param psList The list of <tt>ProvidedService</tt>s still being considered
   * @param preferredLocalProviders The list of <tt>ArrowheadSystem</tt>s in this Local Cloud preferred by the requester system
   *
   * @return the chosen ProvidedService object, containing the necessary <tt>ArrowheadSystem</tt> and <tt>String</tt> serviceUri information to
   *     contact the provider
   */
  static ProvidedService intraCloudMatchmaking(List<ProvidedService> psList, List<ArrowheadSystem> preferredLocalProviders) {
    // If there are no preferred providers, just return the first ProvidedService
    if (preferredLocalProviders.isEmpty()) {
      log.info("intraCloudMatchmaking: no preferred local providers given, returning first ProvidedService");
      return psList.get(0);
    } else { // Otherwise try to find a preferred provider first
      for (ArrowheadSystem system : preferredLocalProviders) {
        for (ProvidedService ps : psList) {
          if (system.equals(ps.getProvider())) {
            log.info("intraCloudMatchmaking: returning the first ProvidedService found with preferred provider");
            return ps;
          }
        }
      }
      log.info("intraCloudMatchmaking: no match was found between preferred providers, returning the first ProvidedService");
      // And only return the first ProvidedService, when no preferred provider was found
      return psList.get(0);
    }
  }

  static List<ProvidedService> doQosReservation(List<ProvidedService> psList) {
    //placeholder for actual implementation
    return psList;
  }

  /**
   * Queries the Orchestration Store database table for a consumer <tt>ArrowheadSystem</tt>. The Orchestration Store holds <i>hardwired</i>
   * <tt>ArrowheadService</tt>s between consumer and provider <tt>ArrowheadSystem</tt>s. The provider system can be local or part of another cloud.
   * For more information see {@link eu.arrowhead.common.database.OrchestrationStore}.
   *
   * @param consumer The <tt>ArrowheadSystem</tt> object representing the consumer system (mandatory)
   * @param service The <tt>ArrowheadService</tt> object representing the service to be consumed (optional)
   *
   * @return a list of <tt>OrchestrationStore</tt> objects matching the query criteria
   *
   * @throws DataNotFoundException if the Store query yielded no results
   */
  static List<OrchestrationStore> queryOrchestrationStore(@NotNull ArrowheadSystem consumer, @Nullable ArrowheadService service) {
    List<OrchestrationStore> retrievedList;

    //If the service is null, we return all the default store entries.
    if (service == null) {
      retrievedList = StoreService.getDefaultStoreEntries(consumer);
    }
    //If not, we return all the Orchestration Store entries specified by the consumer and the service.
    else {
      retrievedList = StoreService.getStoreEntries(consumer, service);
    }

    if (retrievedList.isEmpty()) {
      log.error("queryOrchestrationStore DataNotFoundException");
      throw new DataNotFoundException("No Orchestration Store entries were found for consumer " + consumer.toString());
    } else {
      // Sorting the store entries based on their int priority field
      Collections.sort(retrievedList);
      log.info("queryOrchestrationStore: returning " + retrievedList.size() + " orchestration store entries matching the criteria");
      return retrievedList;
    }
  }

  /**
   * Initiates the Global Service Discovery process by sending a request to the Gatekeeper Core System.
   *
   * @param requestedService The <tt>ArrowheadService</tt> object representing the service for which the Gatekeeper will try to find a provider
   *     system
   * @param preferredClouds A list of <tt>ArrowheadCloud</tt>s which are preferred by the requester system for service consumption. If this list
   *     is empty, the Gatekeeper will send GSD poll requests to the <tt>NeighborCloud</tt>s instead.
   *
   * @return the GSD result from the Gatekeeper Core System
   *
   * @throws DataNotFoundException if none of the discovered <tt>ArrowheadCloud</tt>s returned back positive result
   */
  static GSDResult doGlobalServiceDiscovery(ArrowheadService requestedService, List<ArrowheadCloud> preferredClouds) {
    // Compiling the URI and the request payload
    String uri = Utility.getGatekeeperUri();
    uri = UriBuilder.fromPath(uri).path("init_gsd").toString();
    GSDRequestForm requestForm = new GSDRequestForm(requestedService, preferredClouds);

    // Sending the request, sanity check on the returned result
    Response response = Utility.sendRequest(uri, "PUT", requestForm);
    GSDResult result = response.readEntity(GSDResult.class);
    if (!result.isValid()) {
      log.error("doGlobalServiceDiscovery DataNotFoundException");
      throw new DataNotFoundException("GlobalServiceDiscovery yielded no result.");
    }

    log.info("doGlobalServiceDiscovery returns with " + result.getResponse().size() + " GSDAnswers");
    return result;
  }

  /**
   * Inter-Cloud matchmaking is mandatory for picking out a target Cloud to do ICN with. Clouds preferred by the consumer have higher priority. Custom
   * matchmaking algorithm can be implemented, as of now it just returns the first Cloud from the list.
   *
   * @param result The <tt>GSDResult</tt> object contains the <tt>ArrowheadCloud</tt>s which responded positively to the GSD polling.
   * @param preferredClouds The <tt>ArrowheadCloud</tt>s preferred by the requester <tt>ArrowheadSystem</tt>.
   * @param onlyPreferred An orchestration flags, indicating whether or not the requester <tt>ArrowheadSystem</tt> only wants to consume the
   *     <tt>ArrowheadService</tt> from a preferred provider.
   *
   * @return the target <tt>ArrowheadCloud</tt> for the ICN process
   *
   * @throws DataNotFoundException if there is no preferred provider Cloud available while <i>onlyPreferred</i> is set to true
   */
  static ArrowheadCloud interCloudMatchmaking(GSDResult result, List<ArrowheadCloud> preferredClouds, boolean onlyPreferred) {
    // Extracting the valid ArrowheadClouds from the GSDResult
    List<ArrowheadCloud> partnerClouds = new ArrayList<>();
    for (GSDAnswer answer : result.getResponse()) {
      if (answer.getProviderCloud().isValid()) {
        partnerClouds.add(answer.getProviderCloud());
      }
    }

    // partnerClouds.isEmpty() can only be true here if the other Gatekeepers returned not valid ArrowheadCloud objects
    if (!partnerClouds.isEmpty() && !preferredClouds.isEmpty()) {
      // We iterate through both ArrowheadCloud list, and return with 1 if we find a match.
      for (ArrowheadCloud preferredCloud : preferredClouds) {
        for (ArrowheadCloud partnerCloud : partnerClouds) {
          if (preferredCloud.equals(partnerCloud)) {
            log.info("Preferred Cloud found in the GSDResult. interCloudMatchmaking() finished.");
            return partnerCloud;
          }
        }
      }
    }

    // No match was found, return the first ArrowheadCloud from the GSDResult if it is allowed by the orchestration flag.
    if (onlyPreferred) {
      log.error("interCloudMatchmaking() DataNotFoundException, preferredClouds size: " + preferredClouds.size());
      throw new DataNotFoundException(
          "No preferred Cloud found in the GSD response. Inter-Cloud matchmaking failed, since only preferred providers are allowed.");
    }

    log.info("No usable preferred Clouds were given, interCloudMatchmaking() returns the first partner Cloud entry.");
    return partnerClouds.get(0);
  }

  /**
   * Compiles an ICNRequestForm from the given parameters to start the ICN process.
   * TODO javadoc when finalized
   *
   * @return ICNRequestForm
   */
  static ICNRequestForm compileICNRequestForm(ServiceRequestForm srf, ArrowheadCloud targetCloud) {
    // Getting the list of valid preferred systems, which belong to the target cloud
    List<ArrowheadSystem> preferredSystems = new ArrayList<>();
    for (PreferredProvider provider : srf.getPreferredProviders()) {
      if (provider.isGlobal() && provider.getProviderCloud().equals(targetCloud) && provider.getProviderSystem() != null && provider
          .getProviderSystem().isValid()) {
        preferredSystems.add(provider.getProviderSystem());
      }
    }

    // Passing through the relevant orchestration flags
    Map<String, Boolean> negotiationFlags = new HashMap<>();
    negotiationFlags.put("metadataSearch", srf.getOrchestrationFlags().get("metadataSearch"));
    negotiationFlags.put("pingProviders", srf.getOrchestrationFlags().get("pingProviders"));
    negotiationFlags.put("onlyPreferred", srf.getOrchestrationFlags().get("onlyPreferred"));
    negotiationFlags.put("externalServiceRequest", true);

    log.info("compileICNRequestForm() returns with " + preferredSystems.size() + " preferred systems");
    return new ICNRequestForm(srf.getRequestedService(), targetCloud, srf.getRequesterSystem(), preferredSystems, negotiationFlags, null);
  }

  /**
   * Initiates the Inter Cloud Negotiations process by sending a request to the Gatekeeper Core System.
   *
   * @param requestForm Complex object containing all the necessary information to create a <tt>ServiceRequestForm</tt> at the remote cloud.
   *
   * @return a boxed {@link eu.arrowhead.common.model.messages.OrchestrationResponse} object
   *
   * @throws DataNotFoundException if the ICN failed with the remote cloud for some reason
   */
  static ICNResult doInterCloudNegotiations(ICNRequestForm requestForm) {
    // Compiling the URI, sending the request, doing sanity check on the returned result
    String uri = Utility.getGatekeeperUri();
    uri = UriBuilder.fromPath(uri).path("init_icn").toString();
    Response response = Utility.sendRequest(uri, "PUT", requestForm);
    ICNResult result = response.readEntity(ICNResult.class);
    if (!result.isValid()) {
      log.error("doInterCloudNegotiations() DataNotFoundException");
      throw new DataNotFoundException("ICN failed with the remote cloud.");
    }

    log.info("doInterCloudNegotiations() returns with " + result.getInstructions().getResponse().size() + " possible providers");
    return result;
  }

}
