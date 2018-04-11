/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.orchestrator;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.OrchestrationStore;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.messages.GSDAnswer;
import eu.arrowhead.common.messages.GSDRequestForm;
import eu.arrowhead.common.messages.GSDResult;
import eu.arrowhead.common.messages.ICNRequestForm;
import eu.arrowhead.common.messages.ICNResult;
import eu.arrowhead.common.messages.IntraCloudAuthRequest;
import eu.arrowhead.common.messages.IntraCloudAuthResponse;
import eu.arrowhead.common.messages.OrchestrationForm;
import eu.arrowhead.common.messages.OrchestrationResponse;
import eu.arrowhead.common.messages.PreferredProvider;
import eu.arrowhead.common.messages.ServiceQueryForm;
import eu.arrowhead.common.messages.ServiceQueryResult;
import eu.arrowhead.common.messages.ServiceRequestForm;
import eu.arrowhead.common.messages.TokenGenHelper;
import eu.arrowhead.common.messages.TokenGenerationRequest;
import eu.arrowhead.common.messages.TokenGenerationResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;

/**
 * Contains miscellaneous helper functions for the Orchestration process. The main functions of the Orchestration process used by the REST resource
 * are in {@link eu.arrowhead.core.orchestrator.OrchestratorService}.
 *
 * @author Umlauf Zoltán
 */
final class OrchestratorDriver {

  private static final Logger log = Logger.getLogger(OrchestratorService.class.getName());

  private OrchestratorDriver() throws AssertionError {
    throw new AssertionError("OrchestratorDriver is a non-instantiable class");
  }

  /**
   * Queries the Service Registry Core System for a specific <tt>ArrowheadService</tt>.
   *
   * @param service The <tt>ArrowheadService</tt> object for which the list of potential <tt>ArrowheadSystem</tt> providers are needed
   * @param metadataSearch If true, the stored <tt>ArrowheadService</tt>s have to have the same metadata to be returned in the response.
   * @param pingProviders If true, the Service Registry is asked to ping the service provider <tt>ArrowheadSystem</tt> (where the service is offered)
   *     to check if a connection can be established or not. Normally providers have to remove their offered services from the Service Registry before
   *     going offline, but this feature can be used to ensure offline providers are filtered out.
   *
   * @return list of potential service providers with their offered services (interfaces, metadata, service URI)
   *
   * @throws DataNotFoundException if the Service Registry response list is empty
   */
  static List<ServiceRegistryEntry> queryServiceRegistry(ArrowheadService service, boolean metadataSearch, boolean pingProviders) {
    // Compiling the URI and the request payload
    String srUri = UriBuilder.fromPath(OrchestratorMain.SR_BASE_URI).path("query").toString();
    ServiceQueryForm queryForm = new ServiceQueryForm(service, pingProviders, metadataSearch);

    // Sending the request, parsing the returned result
    Response srResponse = Utility.sendRequest(srUri, "PUT", queryForm);
    ServiceQueryResult serviceQueryResult = srResponse.readEntity(ServiceQueryResult.class);

    // If there are non-valid entries in the Service Registry response, we filter those out
    List<ServiceRegistryEntry> temp = new ArrayList<>();
    for (ServiceRegistryEntry entry : serviceQueryResult.getServiceQueryData()) {
      if (!entry.missingFields(false, false, new HashSet<>(Arrays.asList("interfaces", "address"))).isEmpty()) {
        temp.add(entry);
      }
      if (!StoreService.hasMatchingInterfaces(service, entry.getProvidedService())) {
        temp.add(entry);
      }
    }
    serviceQueryResult.getServiceQueryData().removeAll(temp);
    if (temp.size() > 0) {
      log.info(temp.size() + " not valid OR incompatible (0 common service interface) SR entries removed from the response");
    }
    if (!serviceQueryResult.isValid()) {
      log.error("queryServiceRegistry DataNotFoundException");
      throw new DataNotFoundException("ServiceRegistry query came back empty for " + service.toString(), Status.NOT_FOUND.getStatusCode());
    }

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

  static Set<ArrowheadSystem> queryAuthorization(ArrowheadSystem consumer, ArrowheadService service, Set<ArrowheadSystem> providerSet) {
    // Compiling the URI and the request payload
    String uri = UriBuilder.fromPath(OrchestratorMain.AUTH_CONTROL_URI).path("intracloud").toString();
    IntraCloudAuthRequest request = new IntraCloudAuthRequest(consumer, providerSet, service);

    // Sending the request, parsing the returned result
    Response response = Utility.sendRequest(uri, "PUT", request);
    IntraCloudAuthResponse authResponse = response.readEntity(IntraCloudAuthResponse.class);
    Set<ArrowheadSystem> authorizedSystems = new HashSet<>();
    // Set view of HashMap ensures there are no duplicates between the keys (systems)
    for (Map.Entry<ArrowheadSystem, Boolean> entry : authResponse.getAuthorizationMap().entrySet()) {
      if (entry.getValue()) {
        authorizedSystems.add(entry.getKey());
      }
    }

    // Throwing exception if none of the providers are authorized for this consumer/service pair.
    if (authorizedSystems.isEmpty()) {
      log.error("queryAuthorization DataNotFoundException");
      throw new DataNotFoundException("The consumer system is not authorized to receive servicing from any of the provider systems.",
                                      Status.NOT_FOUND.getStatusCode());
    }

    log.info("queryAuthorization is done, sending back " + authorizedSystems.size() + " authorized Systems");
    return authorizedSystems;
  }

  /**
   * Filters out all the entries of the given <tt>ServiceRegistryEntry</tt> list, which does not contain a preferred local <tt>ArrowheadSystem</tt>.
   * This method is called when the <i>onlyPreferred</i> orchestration flag is set to true.
   *
   * @param srList The list of <tt>ServiceRegistryEntry</tt>s still being considered (after SR query and possibly Auth query)
   * @param preferredLocalProviders A set of local <tt>ArrowheadSystem</tt>s preferred by the requester <tt>ArrowheadSystem</tt>. This is a subset of
   *     the <i>preferredProviders</i> list from the {@link ServiceRequestForm}, which can also contain not local <tt>ArrowheadSystem</tt>s.
   *
   * @return a list of <tt>ServiceRegistryEntry</tt>s which have preferred provider <tt>ArrowheadSystem</tt>s
   *
   * @throws DataNotFoundException if none of the <tt>ServiceRegistryEntry</tt>s from the given list contain a preferred <tt>ArrowheadSystem</tt>
   */

  static List<ServiceRegistryEntry> removeNonPreferred(List<ServiceRegistryEntry> srList, Set<ArrowheadSystem> preferredLocalProviders) {
    // Using a simple nested for-loop for the filtering
    List<ServiceRegistryEntry> preferredList = new ArrayList<>();
    for (ArrowheadSystem system : preferredLocalProviders) {
      for (ServiceRegistryEntry entry : srList) {
        if (system.equals(entry.getProvider())) {
          preferredList.add(entry);
        }
      }
    }

    if (preferredList.isEmpty()) {
      log.error("removeNonPreferred DataNotFoundException");
      throw new DataNotFoundException("No preferred local System was found in the the list of potential provider Systems.",
                                      Status.NOT_FOUND.getStatusCode());
    }

    log.info("removeNonPreferred returns with " + preferredList.size() + " ServiceRegistryEntries.");
    return preferredList;
  }

  static List<ServiceRegistryEntry> doQoSVerification(List<ServiceRegistryEntry> srList) {
    //placeholder for actual implementation
    return srList;
  }

  /**
   * As the last step of the local orchestration process (if requested with the <i>matchmaking</i> orchestration flag) we pick out 1 provider from the
   * remaining list. Providers preferred by the consumer have higher priority. Custom matchmaking algorithm can be implemented here, as of now it just
   * returns the first (preferred) provider from the list. <p> If the <i>onlyPreferred</i> orchestration flag is set to true, then it is guaranteed
   * there will be at least 1 preferred provider to choose from, since this method is called after {@link #removeNonPreferred(List, Set)}, where a
   * {@link eu.arrowhead.common.exception.DataNotFoundException} is thrown if no preferred provider was found.
   *
   * @param srList The list of <tt>ServiceRegistryEntry</tt>s still being considered
   * @param preferredLocalProviders The set of <tt>ArrowheadSystem</tt>s in this Local Cloud preferred by the requester system
   *
   * @return the chosen ServiceRegistryEntry object, containing the necessary <tt>ArrowheadSystem</tt> and <tt>String</tt> serviceUri information to
   *     contact the provider
   */
  static ServiceRegistryEntry intraCloudMatchmaking(List<ServiceRegistryEntry> srList, Set<ArrowheadSystem> preferredLocalProviders) {
    // If there are no preferred providers, just return the first ServiceRegistryEntry
    if (preferredLocalProviders.isEmpty()) {
      log.info("intraCloudMatchmaking: no preferred local providers given, returning first ServiceRegistryEntry");
      return srList.get(0);
    } else { // Otherwise try to find a preferred provider first
      for (ArrowheadSystem system : preferredLocalProviders) {
        for (ServiceRegistryEntry entry : srList) {
          if (system.equals(entry.getProvider())) {
            log.info("intraCloudMatchmaking: returning the first ServiceRegistryEntry found with preferred provider");
            return entry;
          }
        }
      }
      log.info("intraCloudMatchmaking: no match was found between preferred providers, returning the first ServiceRegistryEntry");
      // And only return the first ServiceRegistryEntry, when no preferred provider was found
      return srList.get(0);
    }
  }

  static List<ServiceRegistryEntry> doQosReservation(List<ServiceRegistryEntry> srList) {
    //placeholder for actual implementation
    return srList;
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
  static List<OrchestrationStore> queryOrchestrationStore(ArrowheadSystem consumer, ArrowheadService service) {
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
      throw new DataNotFoundException("No Orchestration Store entries were found for consumer " + consumer.getSystemName(),
                                      Status.NOT_FOUND.getStatusCode());
    } else {
      // Removing non-valid Store entries from the results
      List<OrchestrationStore> temp = new ArrayList<>();
      for (OrchestrationStore entry : retrievedList) {
        if (!entry.missingFields(false, new HashSet<>(Collections.singleton("address"))).isEmpty()) {
          temp.add(entry);
        }
      }
      retrievedList.removeAll(temp);

      // Sorting the store entries based on their int priority field
      Collections.sort(retrievedList);
      log.info("queryOrchestrationStore returns " + retrievedList.size() + " orchestration store entries matching the criteria");
      return retrievedList;
    }
  }

  /**
   * Cross-checks the query results from the <i>Orchestration Store</i> with the <i>Service Registry</i> and <i>Authorization</i>. A provider
   * <tt>ArrowheadSystem</tt> has to be registered into the <i>Service Registry</i> at the time of the servicing request while being authorized too.
   *
   * @param srf The <tt>ServiceRequestForm</tt> from the requester <tt>ArrowheadSystem</tt>
   * @param entryList Result of the <i>Orchestration Store</i> query. All the entries matching the criteria provided by the
   *     <tt>ServiceRequestForm</tt>
   *
   * @return the list of <tt>OrchestrationStore</tt> objects which remained from the query after the cross-check
   */

  static List<OrchestrationStore> crossCheckStoreEntries(ServiceRequestForm srf, List<OrchestrationStore> entryList) {
    Map<String, Boolean> orchestrationFlags = srf.getOrchestrationFlags();
    List<ServiceRegistryEntry> srList = new ArrayList<>();
    List<OrchestrationStore> toRemove = new ArrayList<>();
    Set<ArrowheadSystem> providerSystemsFromSR = new HashSet<>();
    Set<ArrowheadSystem> providerSystemsFromAuth;

    // If true, the Orchestration Store was queried for default entries, meaning the service is different for each store entry
    if (srf.getRequestedService() == null) {
      for (OrchestrationStore entry : entryList) {
        try {
          // Querying the Service Registry for the current service
          List<ServiceRegistryEntry> serviceList = OrchestratorDriver
              .queryServiceRegistry(entry.getService(), orchestrationFlags.get("metadataSearch"), orchestrationFlags.get("pingProviders"));
          // Compiling the systems that provide the current service + filtering service list based on providers (to set port and metadata later)
          for (ServiceRegistryEntry srEntry : serviceList) {
            providerSystemsFromSR.add(srEntry.getProvider());
            if (srEntry.getProvider().equals(entry.getProviderSystem())) {
              srList.add(srEntry);
            }
          }

          // Querying the Authorization to see if the provider system is authorized for this servicing or not
          providerSystemsFromAuth = OrchestratorDriver
              .queryAuthorization(entry.getConsumer(), entry.getService(), Collections.singleton(entry.getProviderSystem()));

          // Remove the Store entry from the list, if the SR or Auth crosscheck fails
          if (!providerSystemsFromSR.contains(entry.getProviderSystem()) || !providerSystemsFromAuth.contains(entry.getProviderSystem())) {
            toRemove.add(entry);
          }
        } catch (DataNotFoundException e) {
          toRemove.add(entry);
        }
      }
      entryList.removeAll(toRemove);
    }
    // Otherwise the service is fixed and we only need 1 SR and Auth query
    else {
      try {
        // Querying the Service Registry for the service
        srList = OrchestratorDriver
            .queryServiceRegistry(srf.getRequestedService(), orchestrationFlags.get("metadataSearch"), orchestrationFlags.get("pingProviders"));
        // Compiling the systems that provide the service
        for (ServiceRegistryEntry srEntry : srList) {
          providerSystemsFromSR.add(srEntry.getProvider());
        }

        //Compiling the list of intra-cloud provider systems from the store list for the auth query
        Set<ArrowheadSystem> localProviderSystems = new HashSet<>();
        for (OrchestrationStore entry : entryList) {
          if (entry.getProviderCloud() == null) {
            localProviderSystems.add(entry.getProviderSystem());
          }
        }
        // Querying the Authorization
        providerSystemsFromAuth = OrchestratorDriver.queryAuthorization(srf.getRequesterSystem(), srf.getRequestedService(), localProviderSystems);

        // Loop over the store entries and remove an entry, if the SR or Auth crosscheck fails
        for (OrchestrationStore entry : entryList) {
          if (entry.getProviderCloud() == null && (!providerSystemsFromSR.contains(entry.getProviderSystem()) || !providerSystemsFromAuth
              .contains(entry.getProviderSystem()))) {
            toRemove.add(entry);
          }
        }
        entryList.removeAll(toRemove);
      }
      /*
       * The SR or Auth query can throw DataNotFoundException, which has to be caught, in case there are inter-cloud store entries from the Store
       * query to check. Default store entries can only be intra-cloud, so the try/catch is only needed on the else branch.
       */ catch (DataNotFoundException e) {
        log.info("crossCheckStoreEntries catches DataNotFoundException from SR/Auth query");
        for (OrchestrationStore entry : entryList) {
          if (entry.getProviderCloud() == null) {
            toRemove.add(entry);
          }
        }
        entryList.removeAll(toRemove);
        return entryList;
      }
    }

    for (OrchestrationStore storeEntry : entryList) {
      for (ServiceRegistryEntry srEntry : srList) {
        if (storeEntry.getService().equals(srEntry.getProvidedService()) && storeEntry.getProviderSystem().equals(srEntry.getProvider())) {
          //This will include the service metadata and port, which is only stored in the Service Registry
          storeEntry.setService(srEntry.getProvidedService());
          storeEntry.setProviderSystem(srEntry.getProvider());
          storeEntry.setServiceURI(srEntry.getServiceURI());
        }
      }
    }
    log.info("crossCheckStoreEntries returns " + entryList.size() + " orchestration store entries");
    return entryList;
  }

  /**
   * Initiates the Global Service Discovery process by sending a request to the Gatekeeper Core System.
   *
   * @param requestedService The <tt>ArrowheadService</tt> object representing the service for which the Gatekeeper will try to find a provider
   *     system
   * @param preferredClouds A list of <tt>ArrowheadCloud</tt>s which are preferred by the requester system for service consumption. If this list is
   *     empty, the Gatekeeper will send GSD poll requests to the <tt>NeighborCloud</tt>s instead.
   *
   * @return the GSD result from the Gatekeeper Core System
   *
   * @throws DataNotFoundException if none of the discovered <tt>ArrowheadCloud</tt>s returned back positive result
   */
  static GSDResult doGlobalServiceDiscovery(ArrowheadService requestedService, List<ArrowheadCloud> preferredClouds,
                                            Map<String, Boolean> registryFlags) {
    // Compiling the request payload
    GSDRequestForm requestForm = new GSDRequestForm(requestedService, preferredClouds, registryFlags);

    // Sending the request, sanity check on the returned result
    Response response = Utility.sendRequest(OrchestratorMain.GSD_SERVICE_URI, "PUT", requestForm);
    GSDResult result = response.readEntity(GSDResult.class);
    if (!result.isValid()) {
      log.error("doGlobalServiceDiscovery DataNotFoundException");
      throw new DataNotFoundException("GlobalServiceDiscovery yielded no result.", Status.NOT_FOUND.getStatusCode());
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
      // Gatekeeper verified that the providerCloud payload is valid
      partnerClouds.add(answer.getProviderCloud());
    }

    if (!preferredClouds.isEmpty()) {
      // We iterate through both ArrowheadCloud list, and return with 1 if we find a match.
      for (ArrowheadCloud preferredCloud : preferredClouds) {
        for (ArrowheadCloud partnerCloud : partnerClouds) {
          if (preferredCloud.equals(partnerCloud)) {
            log.info("interCloudMatchmaking: preferred Cloud found in the GSDResult");
            return partnerCloud;
          }
        }
      }
    }

    // No match was found, return the first ArrowheadCloud from the GSDResult if it is allowed by the orchestration flag.
    if (onlyPreferred) {
      log.error("interCloudMatchmaking DataNotFoundException, preferredClouds size: " + preferredClouds.size());
      throw new DataNotFoundException(
          "No preferred Cloud found in the GSD response. Inter-Cloud matchmaking failed, since only preferred providers are allowed.",
          Status.NOT_FOUND.getStatusCode());
    }

    log.info("interCloudMatchmaking returns the first Cloud entry from the GSD results");
    return partnerClouds.get(0);
  }

  /**
   * Compiles an <tt>ICNRequestForm</tt> from the given parameters and initiates the Inter Cloud Negotiations process by sending a request to the
   * Gatekeeper Core System. The <tt>ICNRequestForm</tt> is a complex object containing all the necessary information to create a
   * <tt>ServiceRequestForm</tt> at the remote cloud.
   *
   * @param srf The <tt>ServiceRequestForm</tt> sent in by the requester <tt>ArrowheadSystem</tt>. 4 different fields of it is used in this method.
   * @param targetCloud The <tt>ArrowheadCloud</tt> entity this local cloud chose to do ICN with.
   *
   * @return a boxed {@link OrchestrationResponse} object from the remote cloud
   *
   * @throws DataNotFoundException if the ICN failed with the remote cloud for some reason
   */
  static ICNResult doInterCloudNegotiations(ServiceRequestForm srf, ArrowheadCloud targetCloud) {
    // Getting the list of valid preferred systems from the ServiceRequestForm, which belong to the target cloud
    List<ArrowheadSystem> preferredSystems = new ArrayList<>();
    for (PreferredProvider provider : srf.getPreferredProviders()) {
      boolean validProviderSystem = provider.getProviderSystem().missingFields(false, new HashSet<>(Collections.singleton("address"))).isEmpty();
      if (provider.isGlobal() && provider.getProviderCloud().equals(targetCloud) && provider.getProviderSystem() != null && validProviderSystem) {
        preferredSystems.add(provider.getProviderSystem());
      }
    }

    // Passing through the relevant orchestration flags to the ICNRequestForm
    Map<String, Boolean> negotiationFlags = new HashMap<>();
    negotiationFlags.put("metadataSearch", srf.getOrchestrationFlags().get("metadataSearch"));
    negotiationFlags.put("pingProviders", srf.getOrchestrationFlags().get("pingProviders"));
    negotiationFlags.put("onlyPreferred", srf.getOrchestrationFlags().get("onlyPreferred"));
    negotiationFlags.put("externalServiceRequest", true);

    // Creating the ICNRequestForm object, which is the payload of the request sent to the Gatekeeper
    ICNRequestForm requestForm = new ICNRequestForm(srf.getRequestedService(), targetCloud, srf.getRequesterSystem(), preferredSystems,
                                                    negotiationFlags);

    // Sending the request, doing sanity check on the returned result
    Response response = Utility.sendRequest(OrchestratorMain.ICN_SERVICE_URI, "PUT", requestForm);
    ICNResult result = response.readEntity(ICNResult.class);
    if (!result.isValid()) {
      log.error("doInterCloudNegotiations DataNotFoundException");
      throw new DataNotFoundException("ICN failed with the remote cloud.", Status.NOT_FOUND.getStatusCode());
    }

    log.info("doInterCloudNegotiations returns with " + result.getOrchResponse().getResponse().size() + " possible providers");
    return result;
  }

  /**
   * Matchmaking method for ICN results. As the last step of the inter-cloud orchestration process (if requested) we pick out 1 provider from the ICN
   * result list. Providers preferred by the consumer have higher priority. Custom matchmaking algorithm can be implemented, as of now it just returns
   * the first provider from the list.
   *
   * @throws DataNotFoundException in case of Store orchestration, and the provider system from the database is not a match according to the remote
   *     cloud
   */
  static OrchestrationResponse icnMatchmaking(ICNResult icnResult, List<ArrowheadSystem> preferredSystems, boolean storeOrchestration) {
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
      throw new DataNotFoundException("The provider ArrowheadSystem from the Store entry was not found in the ICN result.",
                                      Status.NOT_FOUND.getStatusCode());
    }
    // If it's not Store based, we just select the first OrchestrationForm, custom matchmaking algorithm can be implemented here
    else {
      log.info("icnMatchmaking returns with a not preferred System");
      return new OrchestrationResponse(Collections.singletonList(icnResult.getOrchResponse().getResponse().get(0)));
    }
  }

  /**
   * Requests <tt>ArrowheadToken</tt> generation from the Authorization Core System for <tt>ArrowheadService</tt>s, where the metadata contains the
   * "security-token" key-pair. The consumer <tt>ArrowheadSystem</tt>s will use these credentials to contact the provider <tt>ArrowheadSystem</tt>s
   * (if the providers are operating in a secure manner).
   *
   * @param srf The <tt>ServiceRequestForm</tt> sent in by the requester <tt>ArrowheadSystem</tt>. 3 different fields of it is used in this method.
   * @param ofList The <tt>OrchestrationForm</tt> list the Orchestrator will send back.
   *
   * @return the same <tt>OrchestrationForm</tt> list supplemented with the generated <tt>ArrowheadToken</tt>s for providers
   */
  static List<OrchestrationForm> generateAuthTokens(ServiceRequestForm srf, List<OrchestrationForm> ofList) {
    int tokenCount = 0;
    /* Getting a list of service - providers pairs, where the service contains the security - token metadata. This ensures that token generation is
       invoked the minimum amount of times */
    List<TokenGenHelper> tokenGenHelpers = TokenGenHelper.convertOfList(ofList);
    for (TokenGenHelper helper : tokenGenHelpers) {
      // Compiling the request payload
      TokenGenerationRequest tokenRequest = new TokenGenerationRequest(srf.getRequesterSystem(), srf.getRequesterCloud(), helper.getProviders(),
                                                                       helper.getService(), 0);
      // Sending the token generation request, parsing the response
      Response authResponse = Utility.sendRequest(OrchestratorMain.TOKEN_GEN_URI, "PUT", tokenRequest);
      TokenGenerationResponse tokenResponse = authResponse.readEntity(TokenGenerationResponse.class);

      if (tokenResponse != null && tokenResponse.getTokenData() != null && tokenResponse.getTokenData().size() > 0) {
        TokenGenHelper.updateFormsWithTokens(ofList, tokenResponse.getTokenData());
        tokenCount = tokenResponse.getTokenData().size();
      }
    }

    if (tokenCount > 0) {
      log.info("generateAuthTokens successfully returns with " + tokenCount + " tokens");
    }
    return ofList;
  }

}
