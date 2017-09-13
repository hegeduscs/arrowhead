package eu.arrowhead.common;

import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.CoreSystem;
import eu.arrowhead.common.database.NeighborCloud;
import eu.arrowhead.common.database.OwnCloud;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.exception.ErrorMessage;
import eu.arrowhead.common.exception.UnavailableServerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

public final class Utility {

  private static Logger log = Logger.getLogger(Utility.class.getName());
  private static SSLContext sslContext = null;
  private static DatabaseManager dm = DatabaseManager.getInstance();
  private static HashMap<String, Object> restrictionMap = new HashMap<>();
  private static HostnameVerifier allHostsValid = (hostname, session) -> {
    // Decide whether to allow the connection...
    return true;
  };

  private Utility() throws AssertionError {
    throw new AssertionError("Utility is a non-instantiable class");
  }

  public static void setSSLContext(SSLContext context) {
    sslContext = context;
  }

  public static <T> Response sendRequest(String uri, String method, T payload) {
    log.info("Sending " + method + " request to: " + uri);

    boolean isSecure = false;
    if (uri.startsWith("https")) {
      isSecure = true;
    }

    ClientConfig configuration = new ClientConfig();
    configuration.property(ClientProperties.CONNECT_TIMEOUT, 30000);
    configuration.property(ClientProperties.READ_TIMEOUT, 30000);

    Client client;
    if (isSecure && Utility.sslContext != null) {
      client = ClientBuilder.newBuilder().sslContext(sslContext).withConfig(configuration).hostnameVerifier(allHostsValid).build();
    } else if (isSecure && Utility.sslContext == null) {
      log.error("sendRequest() method throws AuthenticationException");
      throw new AuthenticationException(
          "SSL Context is not set, but secure request sending was invoked. An insecure module can not send requests to secure modules.");
    } else {
      client = ClientBuilder.newClient(configuration);
    }

    Builder request = client.target(UriBuilder.fromUri(uri).build()).request().header("Content-type", "application/json");
    Response response; //will not be null after the switch-case
    try {
      switch (method) {
        case "GET":
          response = request.get();
          break;
        case "POST":
          response = request.post(Entity.json(payload));
          break;
        case "PUT":
          response = request.put(Entity.json(payload));
          break;
        case "DELETE":
          response = request.delete();
          break;
        default:
          throw new NotAllowedException("Invalid method type was given to the Utility.sendRequest() method");
      }
    } catch (ProcessingException e) {
      log.error("UnavailableServerException occurred at " + uri);
      throw new UnavailableServerException("Could not get any response from: " + uri);
    }

    //If the response status code does not start with 2 the request was not successful
    if (!(response.getStatusInfo().getFamily() == Family.SUCCESSFUL)) {
      ErrorMessage errorMessage;
      try {
        errorMessage = response.readEntity(ErrorMessage.class);
      } catch (RuntimeException e) {
        log.error("Unknown reason for RuntimeException at the sendRequest() method.", e);
        throw new RuntimeException("Unknown error occurred at " + uri + ". Check log for possibly more information.");
      }
      if (errorMessage == null) {
        log.error("Unknown reason for RuntimeException at the sendRequest() method.");
        throw new RuntimeException("Unknown error occurred at " + uri + ". Check log for possibly more information.");
      } else if (errorMessage.getExceptionType() == null) {
        log.error("Request returned with exception: " + errorMessage.getErrorMessage());
        throw new RuntimeException(errorMessage.getErrorMessage() + " (This exception was passed from another module)");
      } else {
        log.error("Request returned with " + errorMessage.getExceptionType() + ": " + errorMessage.getErrorMessage());
        throw new RuntimeException(errorMessage.getErrorMessage() + " (This " + errorMessage.getExceptionType() + " was passed from another module)");
      }
    }

    return response;
  }

  public static String getUri(String address, int port, String serviceUri, boolean isSecure) {
    if (address == null || serviceUri == null) {
      log.error("Address and serviceUri can not be null (Utility:getUri throws NPE)");
      throw new NullPointerException("Address and serviceUri can not be null (Utility:getUri throws NPE)");
    }

    UriBuilder ub = UriBuilder.fromPath("").host(address).path(serviceUri);
    if (port > 0) {
      ub.port(port);
    }
    if (isSecure) {
      ub.scheme("https");
    } else {
      ub.scheme("http");
    }

    log.info("Utility:getUri returning this: " + ub.toString());
    return ub.toString();
  }

  public static String getOrchestratorUri() {
    restrictionMap.clear();
    restrictionMap.put("systemName", "orchestrator");
    CoreSystem orchestrator = dm.get(CoreSystem.class, restrictionMap);
    if (orchestrator == null) {
      log.error("Utility:getOrchestratorUri System not found in the database!");
      throw new RuntimeException("Orchestrator Core System not found in the database!");
    }
    return getUri(orchestrator.getAddress(), orchestrator.getPort(), orchestrator.getServiceURI(), orchestrator.getIsSecure());
  }

  public static String getServiceRegistryUri() {
    restrictionMap.clear();
    restrictionMap.put("systemName", "serviceregistry");
    CoreSystem serviceRegistry = dm.get(CoreSystem.class, restrictionMap);
    if (serviceRegistry == null) {
      log.error("Utility:getServiceRegistryUri System not found in the database!");
      throw new RuntimeException("Service Registry Core System not found in the database!");
    }
    return getUri(serviceRegistry.getAddress(), serviceRegistry.getPort(), serviceRegistry.getServiceURI(), serviceRegistry.getIsSecure());
  }

  public static String getAuthorizationUri() {
    restrictionMap.clear();
    restrictionMap.put("systemName", "authorization");
    CoreSystem authorization = dm.get(CoreSystem.class, restrictionMap);
    if (authorization == null) {
      log.error("Utility:getAuthorizationUri System not found in the database!");
      throw new RuntimeException("Authorization Core System not found in the database!");
    }
    return getUri(authorization.getAddress(), authorization.getPort(), authorization.getServiceURI(), authorization.getIsSecure());
  }

  public static String getGatekeeperUri() {
    restrictionMap.clear();
    restrictionMap.put("systemName", "gatekeeper");
    CoreSystem gatekeeper = dm.get(CoreSystem.class, restrictionMap);
    if (gatekeeper == null) {
      log.error("Utility:getGatekeeperUri System not found in the database!");
      throw new RuntimeException("Gatekeeper Core System not found in the database!");
    }
    return getUri(gatekeeper.getAddress(), gatekeeper.getPort(), gatekeeper.getServiceURI(), gatekeeper.getIsSecure());
  }

  public static String getQosUri() {
    restrictionMap.clear();
    restrictionMap.put("systemName", "qos");
    CoreSystem qos = dm.get(CoreSystem.class, restrictionMap);
    if (qos == null) {
      log.error("Utility:getQosUri System not found in the database!");
      throw new RuntimeException("QoS Core System not found in the database!");
    }
    return getUri(qos.getAddress(), qos.getPort(), qos.getServiceURI(), qos.getIsSecure());
  }

  public static String getApiUri() {
    restrictionMap.clear();
    restrictionMap.put("systemName", "api");
    CoreSystem api = dm.get(CoreSystem.class, restrictionMap);
    if (api == null) {
      log.error("Utility:getApiUri System not found in the database!");
      throw new RuntimeException("API Core System not found in the database!");
    }
    return getUri(api.getAddress(), api.getPort(), api.getServiceURI(), api.getIsSecure());
  }

  public static List<String> getNeighborCloudURIs() {
    List<NeighborCloud> cloudList = new ArrayList<>();
    cloudList.addAll(dm.getAll(NeighborCloud.class, null));

    List<String> uriList = new ArrayList<>();
    for (NeighborCloud cloud : cloudList) {
      uriList.add(getUri(cloud.getCloud().getAddress(), cloud.getCloud().getPort(), cloud.getCloud().getGatekeeperServiceURI(), false));
    }

    return uriList;
  }

  public static ArrowheadCloud getOwnCloud() {
    List<OwnCloud> cloudList = dm.getAll(OwnCloud.class, null);
    if (cloudList.isEmpty()) {
      log.error("Utility:getOwnCloud not found in the database.");
      throw new RuntimeException("Own Cloud information not found in the database. This information is needed for the Gatekeeper System.");
    }
    if (cloudList.size() > 1) {
      log.warn("own_cloud table should NOT have more than 1 rows.");
    }

    return cloudList.get(0).getCloud();
  }

  public static CoreSystem getCoreSystem(String systemName) {
    restrictionMap.clear();
    restrictionMap.put("systemName", systemName);
    CoreSystem coreSystem = dm.get(CoreSystem.class, restrictionMap);
    if (coreSystem == null) {
      log.error("Utility:getCoreSystem " + systemName + " not found in the database.");
      throw new RuntimeException("Requested Core System " + "(" + systemName + ") not found in the database!");
    }

    return coreSystem;
  }

  // IMPORTANT: only use this function with RuntimeExceptions that have a public String constructor
  /*private static <T extends RuntimeException> void throwExceptionAgain(Class<T> exceptionType, String message) {
    try {
      throw exceptionType.getConstructor(String.class).newInstance(message);
    }
    // Exception is thrown if the given exception type does not have an accessible constructor which accepts a String argument.
    catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException |
        SecurityException e) {
      e.printStackTrace();
    }
  }*/

}
