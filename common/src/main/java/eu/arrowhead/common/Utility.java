/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.arrowhead.common.database.ArrowheadCloud;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.NeighborCloud;
import eu.arrowhead.common.database.OwnCloud;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.DuplicateEntryException;
import eu.arrowhead.common.exception.ErrorMessage;
import eu.arrowhead.common.exception.UnavailableServerException;
import eu.arrowhead.common.messages.ServiceQueryForm;
import eu.arrowhead.common.messages.ServiceQueryResult;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

  private static SSLContext sslContext;
  private static String SERVICE_REGISTRY_URI;

  public static final String ORCH_SERVICE = "OrchestrationService";
  public static final String AUTH_CONTROL_SERVICE = "AuthorizationControl";
  public static final String TOKEN_GEN_SERVICE = "TokenGeneration";
  public static final String GSD_SERVICE = "GlobalServiceDiscovery";
  public static final String ICN_SERVICE = "InterCloudNegotiations";
  public static final String GW_CONSUMER_SERVICE = "ConnectToConsumer";
  public static final String GW_PROVIDER_SERVICE = "ConnectToProvider";
  public static final String GW_SESSION_MGMT = "SessionManagement";
  public static final Map<String, String> secureServerMetadata = Collections.singletonMap("security", "certificate");

  private static final String AUTH_EXCEPTION = "eu.arrowhead.common.exception.AuthenticationException";
  private static final String BAD_PAYLOAD_EXCEPTION = "eu.arrowhead.common.exception.BadPayloadException";
  private static final String NOT_FOUND_EXCEPTION = "eu.arrowhead.common.exception.DataNotFoundException";
  private static final String DUPLICATE_EXCEPTION = "eu.arrowhead.common.exception.DuplicateEntryException";
  private static final String UNAVAILABLE_EXCEPTION = "eu.arrowhead.common.exception.UnavailableServerException";
  private static final DatabaseManager dm = DatabaseManager.getInstance();
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

  public static void setServiceRegistryUri(String serviceRegistryUri) {
    SERVICE_REGISTRY_URI = serviceRegistryUri;
    SERVICE_REGISTRY_URI = UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("query").build().toString();
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
            "SSL Context is not set, but secure request sending was invoked. An insecure module can not send requests to secure modules.", Status
            .UNAUTHORIZED.getStatusCode(), AuthenticationException.class.getName(), Utility.class.toString());
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
      log.error("UnavailableServerException occurred at " + uri, e);
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
          throw new AuthenticationException(errorMessage.getErrorMessage(), errorMessage.getErrorCode(), errorMessage.getExceptionType(), errorMessage.getOrigin());
        case BAD_PAYLOAD_EXCEPTION:
          throw new BadPayloadException(errorMessage.getErrorMessage(), errorMessage.getErrorCode(), errorMessage.getExceptionType(), errorMessage.getOrigin());
        case NOT_FOUND_EXCEPTION:
          throw new DataNotFoundException(errorMessage.getErrorMessage(), errorMessage.getErrorCode(), errorMessage.getExceptionType(), errorMessage.getOrigin());
        case DUPLICATE_EXCEPTION:
          throw new DuplicateEntryException(errorMessage.getErrorMessage(), errorMessage.getErrorCode(), errorMessage.getExceptionType(), errorMessage.getOrigin());
        case UNAVAILABLE_EXCEPTION:
          throw new UnavailableServerException(errorMessage.getErrorMessage(), errorMessage.getErrorCode(), errorMessage.getExceptionType(),
                                               errorMessage.getOrigin());
        default:
          throw new ArrowheadException(errorMessage.getErrorMessage(), errorMessage.getErrorCode(), errorMessage.getExceptionType(), errorMessage.getOrigin());
      }
    }
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

  public static String[] getServiceInfo(String serviceId) {
    ArrowheadService service = sslContext == null ? new ArrowheadService(createSD(serviceId, false), Collections.singletonList("JSON"), null) : new ArrowheadService(createSD(serviceId, true), Collections.singletonList("JSON"), secureServerMetadata);
    ServiceQueryForm sqf = new ServiceQueryForm(service, true, false);
    Response response = sendRequest(SERVICE_REGISTRY_URI, "PUT", sqf, sslContext);
    ServiceQueryResult result = response.readEntity(ServiceQueryResult.class);
    if (result != null && result.isValid()) {
      ServiceRegistryEntry entry = result.getServiceQueryData().get(0);
      ArrowheadSystem coreSystem = entry.getProvider();
      boolean isSecure = false;
      if (entry.getMetadata() != null) {
        isSecure = entry.getMetadata().contains("security");
      }
      String serviceUri = getUri(coreSystem.getAddress(), entry.getPort(), entry.getServiceURI(), isSecure);
      if (serviceId.equals(GW_CONSUMER_SERVICE) || serviceId.equals(GW_PROVIDER_SERVICE)) {
        return new String[]{serviceUri, coreSystem.getSystemName(), coreSystem.getAddress(), coreSystem.getAuthenticationInfo()};
      }
      return new String[]{serviceUri};
    } else {
      log.fatal("getServiceInfo: SR query came back empty for: " + serviceId);
      throw new ServiceConfigurationError(serviceId + " (service) not found in the Service Registry!");
    }
  }

  public static List<String> getNeighborCloudURIs() {
    List<NeighborCloud> cloudList = new ArrayList<>(dm.getAll(NeighborCloud.class, null));

    List<String> uriList = new ArrayList<>();
    for (NeighborCloud cloud : cloudList) {
      uriList.add(getUri(cloud.getCloud().getAddress(), cloud.getCloud().getPort(), cloud.getCloud().getGatekeeperServiceURI(), cloud.getCloud().isSecure()));
    }

    return uriList;
  }

  public static ArrowheadCloud getOwnCloud() {
    List<OwnCloud> cloudList = dm.getAll(OwnCloud.class, null);
    if (cloudList.isEmpty()) {
      log.error("Utility:getOwnCloud not found in the database.");
      throw new DataNotFoundException("Own Cloud information not found in the database. This information is needed for the Gatekeeper System.", Status.NOT_FOUND.getStatusCode(), DataNotFoundException.class.getName(), Utility.class.toString());
    }
    if (cloudList.size() > 1) {
      log.warn("own_cloud table should NOT have more than 1 rows.");
    }

    return cloudList.get(0).getCloud();
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
        throw new ServiceConfigurationError("Secure URIs should use the HTTPS protocol and insecure URIs should use the HTTP protocol. Please fix " + "the following URL accordingly in the properties file: " + url);
      }
    } catch (URISyntaxException e) {
      throw new ServiceConfigurationError(url + errorMessage);
    }
  }

  public static String toPrettyJson(String jsonString, Object obj) {
    if (jsonString != null) {
      jsonString = jsonString.trim();
      JsonParser parser = new JsonParser();
      if (jsonString.startsWith("{")) {
        JsonObject json = parser.parse(jsonString).getAsJsonObject();
        return gson.toJson(json);
      } else {
        JsonArray json = parser.parse(jsonString).getAsJsonArray();
        return gson.toJson(json);
      }
    }
    if (obj != null) {
      return gson.toJson(obj);
    }
    return null;
  }

  public static String createSD(String baseSD, boolean isSecure) {
    if (isSecure) {
      return "Secure" + baseSD;
    } else {
      return "Insecure" + baseSD;
    }
  }

}
