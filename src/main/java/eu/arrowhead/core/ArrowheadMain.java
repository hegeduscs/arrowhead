package eu.arrowhead.core;

import eu.arrowhead.common.Utility;
import eu.arrowhead.core.authorization.AuthorizationApi;
import eu.arrowhead.core.gatekeeper.GatekeeperApi;
import eu.arrowhead.core.gatekeeper.GatekeeperResource;
import eu.arrowhead.core.gateway.GatewayApi;
import eu.arrowhead.core.orchestrator.CommonApi;
import eu.arrowhead.core.orchestrator.OrchestratorResource;
import eu.arrowhead.core.orchestrator.StoreApi;
import eu.arrowhead.core.serviceregistry.ServiceRegistryApi;
import eu.arrowhead.core.serviceregistry.ServiceRegistryResource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.security.PrivateKey;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class ArrowheadMain {

  public static boolean DEBUG_MODE;

  public static final String GK_BASE_URI = getProp().getProperty("gatekeeper_base_uri", "http://127.0.0.1:8446/");
  public static final String GK_BASE_URI_SECURED = getProp().getProperty("gatekeeper_base_uri_secured", "https://127.0.0.1:8447/");
  public static final String ORCH_BASE_URI = getProp().getProperty("orch_base_uri", "http://127.0.0.1:8446/");
  public static final String ORCH_BASE_URI_SECURED = getProp().getProperty("orch_base_uri_secured", "https://127.0.0.1:8447/");
  public static final String SR_BASE_URI = getProp().getProperty("sr_base_uri", "http://127.0.0.1:8446/");
  public static final String SR_BASE_URI_SECURED = getProp().getProperty("sr_base_uri_secured", "https://127.0.0.1:8447/");

  private static PrivateKey privateKey;
  private static HttpServer gkServer;
  private static HttpServer gkSecureServer;
  private static HttpServer orchServer;
  private static HttpServer orchSecureServer;
  private static HttpServer srServer;
  private static HttpServer srSecureServer;
  private static Properties prop;
  private static final Logger log = Logger.getLogger(ArrowheadMain.class.getName());

  private enum CoreSystemType {GATEKEEPER, ORCHESTRATOR, SERVICE_REGISTRY}

  ;

  public static void main(String[] args) throws IOException {
    PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
    System.out.println("Working directory: " + System.getProperty("user.dir"));
    Utility.isUrlValid(GK_BASE_URI, false);
    Utility.isUrlValid(GK_BASE_URI_SECURED, true);
    Utility.isUrlValid(ORCH_BASE_URI, false);
    Utility.isUrlValid(ORCH_BASE_URI_SECURED, true);
    Utility.isUrlValid(SR_BASE_URI, false);
    Utility.isUrlValid(SR_BASE_URI_SECURED, true);
  }

  private static HttpServer startServer(final String url, final CoreSystemType type) throws IOException {
    final ResourceConfig config = new ResourceConfig();
    switch (type) {
      case GATEKEEPER:
        config.registerClasses(GatekeeperApi.class, GatekeeperResource.class);
        break;
      case ORCHESTRATOR:
        config.registerClasses(AuthorizationApi.class, GatewayApi.class, CommonApi.class, OrchestratorResource.class, StoreApi.class);
        break;
      case SERVICE_REGISTRY:
        config.registerClasses(ServiceRegistryApi.class, ServiceRegistryResource.class);
        break;
    }
    config.packages("eu.arrowhead.common");

    URI uri = UriBuilder.fromUri(url).build();
    try {
      final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);
      server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
      server.start();

      log.info("Started " + type.toString() + " server at: " + url);
      System.out.println("Started " + type.toString() + " server at: " + url);
      return server;
    } catch (ProcessingException e) {
      throw new ServiceConfigurationError(
          "Make sure you gave a valid address in the app.properties file! (Assignable to this JVM and not in use already)", e);
    }
  }

  public static synchronized Properties getProp() {
    try {
      if (prop == null) {
        prop = new Properties();
        File file = new File("config" + File.separator + "app.properties");
        FileInputStream inputStream = new FileInputStream(file);
        prop.load(inputStream);
      }
    } catch (FileNotFoundException ex) {
      throw new ServiceConfigurationError("App.properties file not found, make sure you have the correct working directory set! (directory where "
                                              + "the config folder can be found)", ex);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return prop;
  }

}
