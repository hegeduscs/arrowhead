package eu.arrowhead.core.orchestrator;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.ArrowheadCloud;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.GSDRequestForm;
import eu.arrowhead.common.model.messages.GSDResult;
import eu.arrowhead.common.model.messages.IntraCloudAuthRequest;
import eu.arrowhead.common.model.messages.IntraCloudAuthResponse;
import eu.arrowhead.common.model.messages.ProvidedService;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    String srUri = UriBuilder.fromPath(Utility.getServiceRegistryUri()).path(service.getServiceGroup()).path(service.getServiceDefinition())
        .toString();
    String tsigKey = Utility.getCoreSystem("serviceregistry").getAuthenticationInfo();
    ServiceQueryForm queryForm = new ServiceQueryForm(service.getServiceMetadata(), service.getInterfaces(), pingProviders, metadataSearch, tsigKey);

    // Sending the request, parsing the returned result
    Response srResponse = Utility.sendRequest(srUri, "PUT", queryForm);
    ServiceQueryResult serviceQueryResult = srResponse.readEntity(ServiceQueryResult.class);
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
   * @param providerList The list of <tt>ArrowheadSystem</tt> objects representing the potential provider systems
   *
   * @return list of the authorized provider <tt>ArrowheadSystem</tt>s
   *
   * @throws DataNotFoundException if none of the provider <tt>ArrowheadSystem</tt>s are authorized for this servicing
   */
  static List<ArrowheadSystem> queryAuthorization(ArrowheadSystem consumer, ArrowheadService service, List<ArrowheadSystem> providerList) {
    // Compiling the URI and the request payload
    String uri = UriBuilder.fromPath(Utility.getAuthorizationUri()).path("intracloud").toString();
    IntraCloudAuthRequest request = new IntraCloudAuthRequest(consumer, providerList, service, false);

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
   * Filters out all the entries of the given ProvidedService list, which does not contain a preferred provider. This method is called when the
   * <i>onlyPreferred</i> orchestration flag is set to true.
   *
   * @param psList The list of <tt>ProvidedService</tt>s still being considered (after SR query and possibly Auth query)
   * @param preferredLocalProviders A list of local <tt>ArrowheadSystem</tt>s preferred by the requester <tt>ArrowheadSystem</tt>. This is a
   *     sublist of the <i>preferredProviders</i> list from the {@link eu.arrowhead.common.model.messages.ServiceRequestForm}, which can also contain
   *     not local <tt>ArrowheadSystem</tt>s.
   *
   * @return a list of <tt>ProvidedService</tt>s which have preferred provider <tt>ArrowheadSystem</tt>s
   *
   * @throws DataNotFoundException if none of the <tt>ProvidedService</tt>s from the given list contain a preferred provider
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
   * @param requestedService The <tt>ArrowheadService</tt> object representing the service for which the Gatekeeper will try to find a provider system
   * @param preferredClouds A list of <tt>ArrowheadCloud</tt>s which are preferred by the requester system for service consumption. If this list is
   * empty, the Gatekeeper will send GSD poll requests to the <tt>NeighborCloud</tt>s instead.
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

}
