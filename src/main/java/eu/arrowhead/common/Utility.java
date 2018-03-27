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
import eu.arrowhead.common.database.NeighborCloud;
import eu.arrowhead.common.database.OwnCloud;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.DuplicateEntryException;
import eu.arrowhead.common.exception.ErrorMessage;
import eu.arrowhead.common.exception.UnavailableServerException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.Set;
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

  public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  private static SSLContext sslContext;

  private static final DatabaseManager dm = DatabaseManager.getInstance();
  private static final Logger log = Logger.getLogger(Utility.class.getName());
  private static final HostnameVerifier allHostsValid = (hostname, session) -> {
    // Decide whether to allow the connection...
    return true;
  };

  private Utility() throws AssertionError {
    throw new AssertionError("Utility is a non-instantiable class");
  }

  public static void setSSLContext(SSLContext context) {
    sslContext = context;
  }

  private static <T> Response sendRequest(String uri, String method, T payload, SSLContext context) {
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
        log.error("sendRequest() method throws AuthException");
        throw new AuthException(
            "SSL Context is not set, but secure request sending was invoked. An insecure module can not send requests to secure modules.",
            Status.UNAUTHORIZED.getStatusCode());
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
      throw new UnavailableServerException("Could not get any response from: " + uri, Status.SERVICE_UNAVAILABLE.getStatusCode(), e);
    }

    // If the response status code does not start with 2 the request was not successful
    if (!(response.getStatusInfo().getFamily() == Family.SUCCESSFUL)) {
      handleException(response, uri);
    }

    return response;
  }

  public static <T> Response sendRequest(String uri, String method, T payload) {
    return sendRequest(uri, method, payload, sslContext);
  }

  private static void handleException(Response response, String uri) {
    //The response body has to be extracted before the stream closes
    String errorMessageBody = toPrettyJson(null, response.getEntity());
    if (errorMessageBody == null || errorMessageBody.equals("null")) {
      response.bufferEntity();
      errorMessageBody = response.readEntity(String.class);
      if (errorMessageBody.length() > 250) {
        errorMessageBody = errorMessageBody.substring(0, 250);
      }
    }

    ErrorMessage errorMessage;
    try {
      errorMessage = response.readEntity(ErrorMessage.class);
    } catch (RuntimeException e) {
      log.error("Unknown reason for RuntimeException at the sendRequest() method.", e);
      log.info("Request failed, response status code: " + response.getStatus());
      log.info("Request failed, response body: " + errorMessageBody);
      throw new RuntimeException("Unknown error occurred at " + uri + ". Check log for possibly more information.", e);
    }
    if (errorMessage == null) {
      log.error("Unknown reason for RuntimeException at the sendRequest() method.");
      log.info("Request failed, response status code: " + response.getStatus());
      log.info("Request failed, response body: " + errorMessageBody);
      throw new RuntimeException("Unknown error occurred at " + uri + ". Check log for possibly more information.");
    } else if (errorMessage.getExceptionType() == null) {
      log.info("Request failed, response status code: " + response.getStatus());
      log.info("Request failed, response body: " + errorMessageBody);
      throw new RuntimeException("Unknown error occurred at " + uri + ". Check log for possibly more information.");
    } else {
      log.error("Request returned with " + errorMessage.getExceptionType() + ": " + errorMessage.getErrorMessage());
      switch (errorMessage.getExceptionType()) {
        case ARROWHEAD:
          throw new ArrowheadException(errorMessage.getErrorMessage(), errorMessage.getErrorCode());
        case AUTH:
          throw new AuthException(errorMessage.getErrorMessage(), errorMessage.getErrorCode());
        case BAD_METHOD:
          throw new ArrowheadException(errorMessage.getErrorMessage(), errorMessage.getErrorCode());
        case BAD_PAYLOAD:
          throw new BadPayloadException(errorMessage.getErrorMessage(), errorMessage.getErrorCode());
        case BAD_URI:
          throw new ArrowheadException(errorMessage.getErrorMessage(), errorMessage.getErrorCode());
        case DATA_NOT_FOUND:
          throw new DataNotFoundException(errorMessage.getErrorMessage(), errorMessage.getErrorCode());
        case DUPLICATE_ENTRY:
          throw new DuplicateEntryException(errorMessage.getErrorMessage(), errorMessage.getErrorCode());
        case GENERIC:
          throw new ArrowheadException(errorMessage.getErrorMessage(), errorMessage.getErrorCode());
        case JSON_MAPPING:
          throw new ArrowheadException(errorMessage.getErrorMessage(), errorMessage.getErrorCode());
        case UNAVAILABLE:
          throw new UnavailableServerException(errorMessage.getErrorMessage(), errorMessage.getErrorCode());
      }
    }
  }

  public static String getUri(String address, int port, String serviceUri, boolean isSecure, boolean serverStart) {
    if (address == null) {
      log.error("Address can not be null (Utility:getUri throws NPE)");
      throw new NullPointerException("Address can not be null (Utility:getUri throws NPE)");
    }

    UriBuilder ub = UriBuilder.fromPath("").host(address);
    if (isSecure) {
      ub.scheme("https");
    } else {
      ub.scheme("http");
    }
    if (port > 0) {
      ub.port(port);
    }
    if (serviceUri != null) {
      ub.path(serviceUri);
    }

    String url = ub.toString();
    try {
      new URI(url);
    } catch (URISyntaxException e) {
      if (serverStart) {
        throw new ServiceConfigurationError(url + " is not a valid URL to start a HTTP server! Please fix the address field in the properties file.");
      } else {
        log.error("Bad URL components passed to getUri() method");
        throw new ArrowheadException(url + " is not a valid URL!");
      }
    }

    log.info("Utility:getUri returning this: " + url);
    return url;
  }


  public static List<String> getNeighborCloudURIs() {
    List<NeighborCloud> cloudList = new ArrayList<>(dm.getAll(NeighborCloud.class, null));

    List<String> uriList = new ArrayList<>();
    for (NeighborCloud cloud : cloudList) {
      uriList.add(
          getUri(cloud.getCloud().getAddress(), cloud.getCloud().getPort(), cloud.getCloud().getGatekeeperServiceURI(), cloud.getCloud().isSecure(),
                 false));
    }

    return uriList;
  }

  public static ArrowheadCloud getOwnCloud() {
    List<OwnCloud> cloudList = dm.getAll(OwnCloud.class, null);
    if (cloudList.isEmpty()) {
      log.error("Utility:getOwnCloud not found in the database.");
      throw new DataNotFoundException("Own Cloud information not found in the database. This information is needed for the Gatekeeper System.",
                                      Status.NOT_FOUND.getStatusCode());
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

  public static String getRequestPayload(InputStream is) {
    StringBuilder sb = new StringBuilder();
    String line;
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
    } catch (UnsupportedEncodingException e) {
      log.fatal("getRequestPayload ISReader has unsupported charset set!");
      throw new AssertionError("getRequestPayload ISReader has unsupported charset set! Code needs to be changed!", e);
    } catch (IOException e) {
      log.error("IOException while reading the request payload");
      throw new RuntimeException("IOException occured while reading an incoming request payload", e);
    }

    if (!sb.toString().isEmpty()) {
      return toPrettyJson(sb.toString(), null);
    } else {
      return "";
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

  public static <T> T fromJson(String json, Class<T> parsedClass) {
    return gson.fromJson(json, parsedClass);
  }

  public static void checkProperties(Set<String> propertyNames, List<String> mandatoryProperties) {
    if (mandatoryProperties == null || mandatoryProperties.isEmpty()) {
      return;
    }
    //Arrays.asList() returns immutable lists, so we have to copy it first
    List<String> properties = new ArrayList<>(mandatoryProperties);
    if (!propertyNames.containsAll(mandatoryProperties)) {
      properties.removeIf(propertyNames::contains);
      throw new ServiceConfigurationError("Missing field(s) from app.properties file: " + properties.toString());
    }
  }

}
