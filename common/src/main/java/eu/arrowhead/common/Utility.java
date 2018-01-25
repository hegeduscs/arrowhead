package eu.arrowhead.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.CoreSystem;
import eu.arrowhead.common.database.NeighborCloud;
import eu.arrowhead.common.database.OwnCloud;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.DuplicateEntryException;
import eu.arrowhead.common.exception.ErrorMessage;
import eu.arrowhead.common.exception.UnavailableServerException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ServiceConfigurationError;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

public final class Utility {

  private static final String ARROWHEAD_EXCEPTION = "eu.arrowhead.common.exception.ArrowheadException";
  private static final String AUTH_EXCEPTION = "eu.arrowhead.common.exception.AuthenticationException";
  private static final String BAD_PAYLOAD_EXCEPTION = "eu.arrowhead.common.exception.BadPayloadException";
  private static final String NOT_FOUND_EXCEPTION = "eu.arrowhead.common.exception.DataNotFoundException";
  private static final String DUPLICATE_EXCEPTION = "eu.arrowhead.common.exception.DuplicateEntryException";
  private static final String UNAVAILABLE_EXCEPTION = "eu.arrowhead.common.exception.UnavailableServerException";

  private static SSLContext sslContext;
  private static final DatabaseManager dm = DatabaseManager.getInstance();
  private static final HashMap<String, Object> restrictionMap = new HashMap<>();
  private static final Logger log = Logger.getLogger(Utility.class.getName());
  private static final HostnameVerifier allHostsValid = (hostname, session) -> {
    // Decide whether to allow the connection...
    return true;
  };
  public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private Utility() throws AssertionError {
    throw new AssertionError("Utility is a non-instantiable class");
  }

  public static void setSSLContext(SSLContext context) {
    sslContext = context;
  }

  public static <T> Response sendRequest(String uri, String method, T payload, SSLContext context) {
    log.info("Sending " + method + " request to: " + uri);

    boolean isSecure = false;
    if (uri.startsWith("https")) {
      isSecure = true;
    }

    ClientConfig configuration = new ClientConfig();
    configuration.property(ClientProperties.CONNECT_TIMEOUT, 30000);
    configuration.property(ClientProperties.READ_TIMEOUT, 30000);

    Client client;
    if (isSecure) {
      if (context != null) {
        client = ClientBuilder.newBuilder().sslContext(context).withConfig(configuration).hostnameVerifier(allHostsValid).build();
      } else if (Utility.sslContext != null) {
        client = ClientBuilder.newBuilder().sslContext(sslContext).withConfig(configuration).hostnameVerifier(allHostsValid).build();
      } else {
        log.error("sendRequest() method throws AuthenticationException");
        throw new AuthenticationException(
            "SSL Context is not set, but secure request sending was invoked. An insecure module can not send requests to secure modules.",
            Status.UNAUTHORIZED.getStatusCode(), AuthenticationException.class.getName(), Utility.class.toString());
      }
    } else {
      client = ClientBuilder.newClient(configuration);
    }

    Builder request = client.target(UriBuilder.fromUri(uri).build()).request().header("Content-type", "application/json");
    Response response; // will not be null after the switch-case
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
      throw new UnavailableServerException("Could not get any response from: " + uri, Status.SERVICE_UNAVAILABLE.getStatusCode(),
                                           UnavailableServerException.class.getName(), Utility.class.toString(), e);
    }

    // If the response status code does not start with 2 the request was not successful
    if (!(response.getStatusInfo().getFamily() == Family.SUCCESSFUL)) {
      handleException(response, uri);
    }

    return response;
  }

  public static <T> Response sendRequest(String uri, String method, T payload) {
    return sendRequest(uri, method, payload, null);
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
      throw new DataNotFoundException("Orchestrator Core System not found in the database!", Status.NOT_FOUND.getStatusCode(),
                                      DataNotFoundException.class.getName(), Utility.class.toString());
    }
    return getUri(orchestrator.getAddress(), orchestrator.getPort(), orchestrator.getServiceURI(), orchestrator.getIsSecure());
  }

  public static String getServiceRegistryUri() {
    restrictionMap.clear();
    restrictionMap.put("systemName", "serviceregistry");
    CoreSystem serviceRegistry = dm.get(CoreSystem.class, restrictionMap);
    if (serviceRegistry == null) {
      log.error("Utility:getServiceRegistryUri System not found in the database!");
      throw new DataNotFoundException("Service Registry Core System not found in the database!", Status.NOT_FOUND.getStatusCode(),
                                      DataNotFoundException.class.getName(), Utility.class.toString());
    }
    return getUri(serviceRegistry.getAddress(), serviceRegistry.getPort(), serviceRegistry.getServiceURI(), serviceRegistry.getIsSecure());
  }

  public static String getAuthorizationUri() {
    restrictionMap.clear();
    restrictionMap.put("systemName", "authorization");
    CoreSystem authorization = dm.get(CoreSystem.class, restrictionMap);
    if (authorization == null) {
      log.error("Utility:getAuthorizationUri System not found in the database!");
      throw new DataNotFoundException("Authorization Core System not found in the database!", Status.NOT_FOUND.getStatusCode(),
                                      DataNotFoundException.class.getName(), Utility.class.toString());
    }
    return getUri(authorization.getAddress(), authorization.getPort(), authorization.getServiceURI(), authorization.getIsSecure());
  }

  public static String getGatekeeperUri() {
    restrictionMap.clear();
    restrictionMap.put("systemName", "gatekeeper");
    CoreSystem gatekeeper = dm.get(CoreSystem.class, restrictionMap);
    if (gatekeeper == null) {
      log.error("Utility:getGatekeeperUri System not found in the database!");
      throw new DataNotFoundException("Gatekeeper Core System not found in the database!", Status.NOT_FOUND.getStatusCode(),
                                      DataNotFoundException.class.getName(), Utility.class.toString());
    }
    return getUri(gatekeeper.getAddress(), gatekeeper.getPort(), gatekeeper.getServiceURI(), gatekeeper.getIsSecure());
  }

  public static String getGatewayUri() {
    restrictionMap.clear();
    restrictionMap.put("systemName", "gateway");
    CoreSystem gateway = dm.get(CoreSystem.class, restrictionMap);
    if (gateway == null) {
      log.error("Utility:getGatewayUri System not found in the database!");
      throw new DataNotFoundException("Gateway Core System not found in the database!", Status.NOT_FOUND.getStatusCode(),
                                      DataNotFoundException.class.getName(), Utility.class.toString());
    }
    return getUri(gateway.getAddress(), gateway.getPort(), gateway.getServiceURI(), gateway.getIsSecure());
  }

  public static String getQosUri() {
    restrictionMap.clear();
    restrictionMap.put("systemName", "qos");
    CoreSystem qos = dm.get(CoreSystem.class, restrictionMap);
    if (qos == null) {
      log.error("Utility:getQosUri System not found in the database!");
      throw new DataNotFoundException("QoS Core System not found in the database!", Status.NOT_FOUND.getStatusCode(),
                                      DataNotFoundException.class.getName(), Utility.class.toString());
    }
    return getUri(qos.getAddress(), qos.getPort(), qos.getServiceURI(), qos.getIsSecure());
  }

  public static List<String> getNeighborCloudURIs() {
    List<NeighborCloud> cloudList = new ArrayList<>(dm.getAll(NeighborCloud.class, null));

    List<String> uriList = new ArrayList<>();
    for (NeighborCloud cloud : cloudList) {
      uriList.add(
          getUri(cloud.getCloud().getAddress(), cloud.getCloud().getPort(), cloud.getCloud().getGatekeeperServiceURI(), cloud.getCloud().isSecure()));
    }

    return uriList;
  }

  public static ArrowheadCloud getOwnCloud() {
    List<OwnCloud> cloudList = dm.getAll(OwnCloud.class, null);
    if (cloudList.isEmpty()) {
      log.error("Utility:getOwnCloud not found in the database.");
      throw new DataNotFoundException("Own Cloud information not found in the database. This information is needed for the Gatekeeper System.",
                                      Status.NOT_FOUND.getStatusCode(), DataNotFoundException.class.getName(), Utility.class.toString());
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
      throw new DataNotFoundException("Requested Core System " + "(" + systemName + ") not found in the database!", Status.NOT_FOUND.getStatusCode(),
                                      DataNotFoundException.class.getName(), Utility.class.toString());
    }

    return coreSystem;
  }


  public static String stripEndSlash(String uri) {
    if (uri != null && uri.endsWith("/")) {
      return uri.substring(0, uri.length() - 1);
    }
    return uri;
  }

  public static void isUrlValid(String url, boolean isSecure) {
    String errorMessage = " is not a valid URL to start a HTTP server! Please fix the URL in the properties file.";
    try {
      URI uri = new URI(url);

      if ("mailto".equals(uri.getScheme())) {
        throw new ServiceConfigurationError(url + errorMessage);
      }
      if (uri.getHost() == null) {
        throw new ServiceConfigurationError(url + errorMessage);
      }
      if ((isSecure && "http".equals(uri.getScheme())) || (!isSecure && "https".equals(uri.getScheme()))) {
        throw new ServiceConfigurationError("Secure URIs should use the HTTPS protocol and insecure URIs should use the HTTP protocol. Please fix "
                                                + "the following URL accordingly in the properties file: " + url);
      }
    } catch (URISyntaxException e) {
      throw new ServiceConfigurationError(url + errorMessage);
    }

  }


  public static String toPrettyJson(String jsonString, Object obj) {
    if (jsonString != null) {
      JsonParser parser = new JsonParser();
      JsonObject json = parser.parse(jsonString).getAsJsonObject();
      return gson.toJson(json);
    }
    if (obj != null) {
      return gson.toJson(obj);
    }
    return null;
  }

  private static void handleException(Response response, String uri) {
    //The response body has to be extracted before the stream closes
    String errorMessageBody = toPrettyJson(null, response.getEntity());
    ErrorMessage errorMessage;
    try {
      errorMessage = response.readEntity(ErrorMessage.class);
    } catch (RuntimeException e) {
      log.error("Unknown reason for RuntimeException at the sendRequest() method.", e);
      log.info("Request failed, response status code: " + response.getStatus());
      log.info("Request failed, response body: " + errorMessageBody);
      throw new ArrowheadException("Unknown error occurred at " + uri + ". Check log for possibly more information.", e);
    }
    if (errorMessage == null) {
      log.error("Unknown reason for RuntimeException at the sendRequest() method.");
      log.info("Request failed, response status code: " + response.getStatus());
      log.info("Request failed, response body: " + errorMessageBody);
      throw new ArrowheadException("Unknown error occurred at " + uri + ". Check log for possibly more information.");
    } else if (errorMessage.getExceptionType() == null) {
      log.info("Request failed, response status code: " + response.getStatus());
      log.info("Request failed, response body: " + errorMessageBody);
      throw new ArrowheadException("Unknown error occurred at " + uri + ". Check log for possibly more information.");
    } else {
      log.error("Request returned with " + errorMessage.getExceptionType() + ": " + errorMessage.getErrorMessage());
      switch (errorMessage.getExceptionType()) {
        case AUTH_EXCEPTION:
          throw new AuthenticationException(errorMessage.getErrorMessage(), errorMessage.getErrorCode(), errorMessage.getExceptionType(),
                                            errorMessage.getOrigin());
        case BAD_PAYLOAD_EXCEPTION:
          throw new BadPayloadException(errorMessage.getErrorMessage(), errorMessage.getErrorCode(), errorMessage.getExceptionType(),
                                        errorMessage.getOrigin());
        case NOT_FOUND_EXCEPTION:
          throw new DataNotFoundException(errorMessage.getErrorMessage(), errorMessage.getErrorCode(), errorMessage.getExceptionType(),
                                          errorMessage.getOrigin());
        case DUPLICATE_EXCEPTION:
          throw new DuplicateEntryException(errorMessage.getErrorMessage(), errorMessage.getErrorCode(), errorMessage.getExceptionType(),
                                            errorMessage.getOrigin());
        case UNAVAILABLE_EXCEPTION:
          throw new UnavailableServerException(errorMessage.getErrorMessage(), errorMessage.getErrorCode(), errorMessage.getExceptionType(),
                                               errorMessage.getOrigin());
        default:
          throw new ArrowheadException(errorMessage.getErrorMessage(), errorMessage.getErrorCode(), errorMessage.getExceptionType(),
                  errorMessage.getOrigin());
      }
    }
  }

  // IMPORTANT: only use this function with RuntimeExceptions that have a public String constructor
  /*
   * private static <T extends RuntimeException> void throwExceptionAgain(Class<T>
   * exceptionType, String message) { try { throw
   * exceptionType.getConstructor(String.class).newInstance(message); } //
   * Exception is thrown if the given exception type does not have an accessible
   * constructor which accepts a String argument. catch (InstantiationException |
   * IllegalAccessException | IllegalArgumentException | InvocationTargetException
   * | NoSuchMethodException | SecurityException e) { e.printStackTrace(); } }
   */

}
