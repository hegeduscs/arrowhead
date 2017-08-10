package eu.arrowhead.core.orchestrator;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.model.ArrowheadService;
import eu.arrowhead.common.model.ArrowheadSystem;
import eu.arrowhead.common.model.messages.IntraCloudAuthRequest;
import eu.arrowhead.common.model.messages.IntraCloudAuthResponse;
import eu.arrowhead.common.model.messages.ProvidedService;
import eu.arrowhead.common.model.messages.ServiceQueryForm;
import eu.arrowhead.common.model.messages.ServiceQueryResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;

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
   * @return List of potential service providers with their offered services (interfaces, metadata, service URI)
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
      log.info("queryServiceRegistry DataNotFoundException");
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

    log.info("ServiceRegistry query was successful. Number of potential providers for" + service.toString() + " is " + serviceQueryResult
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
   * @return a List of the authorized provider <tt>ArrowheadSystem</tt>s
   *
   * @throws DataNotFoundException if none of the provider <tt>ArrowheadSystem</tt>s are authorized for this servicing
   */
  static List<ArrowheadSystem> queryAuthorization(ArrowheadSystem consumer, ArrowheadService service, List<ArrowheadSystem> providerList) {
    // Compiling the URI and the request payload
    String URI = UriBuilder.fromPath(Utility.getAuthorizationUri()).path("intracloud").toString();
    IntraCloudAuthRequest request = new IntraCloudAuthRequest(consumer, providerList, service, false);

    // Sending the request, parsing the returned result
    Response response = Utility.sendRequest(URI, "PUT", request);
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
      log.info("queryAuthorization DataNotFoundException");
      throw new DataNotFoundException("The consumer system is not authorized to receive servicing from any of the provider systems.");
    }

    log.info("Authorization query is done, sending back the authorized Systems (" + authorizedSystems.size() + ")");
    return authorizedSystems;
  }

}
