package eu.arrowhead.common;

import eu.arrowhead.common.database.CoreSystem;
import eu.arrowhead.common.database.NeighborCloud;
import eu.arrowhead.common.database.OwnCloud;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.exception.DataNotFoundException;
import eu.arrowhead.common.exception.UnavailableServerException;
import eu.arrowhead.common.model.ArrowheadCloud;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.ws.rs.NotAllowedException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

public final class Utility {

  private static HostnameVerifier allHostsValid = new HostnameVerifier() {
    public boolean verify(String hostname, SSLSession session) {
      // Decide whether to allow the connection...
      return true;
    }
  };
  private static Logger log = Logger.getLogger(Utility.class.getName());
  private static SSLContext sslContext = null;
  private static DatabaseManager dm = DatabaseManager.getInstance();
  private static HashMap<String, Object> restrictionMap = new HashMap<>();

  private Utility() {
  }

  public static void setSSLContext(SSLContext context) {
    sslContext = context;
  }

  public static <T> Response sendRequest(String URI, String method, T payload) {
    log.info("Sending " + method + " request to: " + URI);

    Response response = null;
    boolean isSecure = false;
    if (URI.startsWith("https")) {
      isSecure = true;
    }

    try {
      ClientConfig configuration = new ClientConfig();
      configuration.property(ClientProperties.CONNECT_TIMEOUT, 30000);
      configuration.property(ClientProperties.READ_TIMEOUT, 30000);

      Client client = null;
      if (isSecure && Utility.sslContext != null) {
        client = ClientBuilder.newBuilder().sslContext(sslContext).withConfig(configuration).hostnameVerifier(allHostsValid).build();
      } else if (isSecure && Utility.sslContext == null) {
        throw new AuthenticationException("SSL Context not set, but secure was invoked.");
      } else {
        client = ClientBuilder.newClient(configuration);
      }

      Builder request = client.target(UriBuilder.fromUri(URI).build()).request().header("Content-type", "application/json");
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

      //Internal Server Error, Not Found
      if (response == null || response.getStatus() == 500 || response.getStatus() == 404) {
        log.info("UnavailableServerException at " + URI);
        throw new UnavailableServerException("Server(s) timed out. Check logs for details.");
      }
      return response;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return Response.status(Status.NOT_FOUND).build();
  }

  /*
   * Some level of flexibility in the URI creation, in order to avoid
   * implementation mistakes.
   */
  //TODO Niki kódjával lehet ezt szebben is valszeg
  public static String getURI(String address, String port, String serviceURI, boolean isSecure) {
    if (address == null || serviceURI == null) {
      log.info("Address and serviceURI can not be null (Utility:getURI throws NPE)");
      throw new NullPointerException("Address and serviceURI can not be null.");
    }

    String baseURI = null;
    if (isSecure) {
      baseURI = "https://";
    } else {
      baseURI = "http://";
    }

    UriBuilder ub = null;
    if (address.startsWith(baseURI)) {
      if (port != null) {
        ub = UriBuilder.fromPath(address + ":" + port);
      } else {
        ub = UriBuilder.fromPath(address);
      }
    } else {
      if (port != null) {
        ub = UriBuilder.fromPath(baseURI).path(address + ":" + port);
      } else {
        ub = UriBuilder.fromPath(baseURI).path(address);
      }
    }
    ub.path(serviceURI);

    log.info("Utility:getURI returning this: " + ub.toString());
    return ub.toString();
  }

  public static String getOrchestratorURI() {
    restrictionMap.clear();
    restrictionMap.put("systemName", "orchestrator");
    CoreSystem orchestrator = dm.get(CoreSystem.class, restrictionMap);
    if (orchestrator == null) {
      log.info("Utility:getOrchestratorURI DNFException");
      throw new DataNotFoundException("Orchestration Core System not found in the database!");
    }
    return getURI(orchestrator.getAddress(), orchestrator.getPort(), orchestrator.getServiceURI(), orchestrator.getIsSecure());
  }

  public static String getServiceRegistryURI() {
    restrictionMap.clear();
    restrictionMap.put("systemName", "serviceregistry");
    CoreSystem serviceRegistry = dm.get(CoreSystem.class, restrictionMap);
    if (serviceRegistry == null) {
      log.info("Utility:getServiceRegistryURI DNFException");
      throw new DataNotFoundException("Service Registry Core System not found in the database!");
    }
    return getURI(serviceRegistry.getAddress(), serviceRegistry.getPort(), serviceRegistry.getServiceURI(), serviceRegistry.getIsSecure());
  }

  public static String getAuthorizationURI() {
    restrictionMap.clear();
    restrictionMap.put("systemName", "authorization");
    CoreSystem authorization = dm.get(CoreSystem.class, restrictionMap);
    if (authorization == null) {
      log.info("Utility:getAuthorizationURI DNFException");
      throw new DataNotFoundException("Authoriaztion Core System not found in the database!");
    }
    return getURI(authorization.getAddress(), authorization.getPort(), authorization.getServiceURI(), authorization.getIsSecure());
  }

  public static String getGatekeeperURI() {
    System.out.println("sajt");
    restrictionMap.clear();
    restrictionMap.put("systemName", "gatekeeper");
    CoreSystem gatekeeper = dm.get(CoreSystem.class, restrictionMap);
    if (gatekeeper == null) {
      log.info("Utility:getGatekeeperURI DNFException");
      throw new DataNotFoundException("Gatekeeper Core System not found in the database!");
    }
    return getURI(gatekeeper.getAddress(), gatekeeper.getPort(), gatekeeper.getServiceURI(), gatekeeper.getIsSecure());
  }

  public static String getQoSURI() {
    restrictionMap.clear();
    restrictionMap.put("systemName", "qos");
    CoreSystem QoS = dm.get(CoreSystem.class, restrictionMap);
    if (QoS == null) {
      log.info("Utility:getQoSURI DNFException");
      throw new DataNotFoundException("QoS Core System not found in the database!");
    } else {
      System.out.println("sajt");
    }
    return getURI(QoS.getAddress(), QoS.getPort(), QoS.getServiceURI(), QoS.getIsSecure());
  }

  public static String getApiURI() {
    restrictionMap.clear();
    restrictionMap.put("systemName", "api");
    CoreSystem api = dm.get(CoreSystem.class, restrictionMap);
    if (api == null) {
      log.info("Utility:getApiURI DNFException");
      throw new DataNotFoundException("API Core System not found in the database!");
    }
    return getURI(api.getAddress(), api.getPort(), api.getServiceURI(), api.getIsSecure());
  }

  public static List<String> getNeighborCloudURIs() {
    restrictionMap.clear();
    List<NeighborCloud> cloudList = new ArrayList<>();
    cloudList.addAll(dm.getAll(NeighborCloud.class, restrictionMap));

    List<String> URIList = new ArrayList<>();
    for (NeighborCloud cloud : cloudList) {
      URIList.add(getURI(cloud.getCloud().getAddress(), cloud.getCloud().getPort(), cloud.getCloud().getGatekeeperServiceURI(), false));
    }

    return URIList;
  }

  public static ArrowheadCloud getOwnCloud() {
    restrictionMap.clear();
    List<OwnCloud> cloudList = new ArrayList<>();
    cloudList = dm.getAll(OwnCloud.class, restrictionMap);
    if (cloudList.isEmpty()) {
      log.info("Utility:getOwnCloud DNFException");
      throw new DataNotFoundException(
          "No 'Own Cloud' entry in the configuration database." + "Please make sure to enter one in the 'own_cloud' table."
              + "This information is needed for the Gatekeeper System.");
    }

    return new ArrowheadCloud(cloudList.get(0));
  }

  public static CoreSystem getCoreSystem(String systemName) {
    restrictionMap.clear();
    restrictionMap.put("systemName", systemName);
    CoreSystem coreSystem = dm.get(CoreSystem.class, restrictionMap);
    if (coreSystem == null) {
      log.info("Utility:getCoreSystem DNFException");
      throw new DataNotFoundException("Requested Core System " + "(" + systemName + ") not found in the database!");
    }

    return coreSystem;
  }


}
