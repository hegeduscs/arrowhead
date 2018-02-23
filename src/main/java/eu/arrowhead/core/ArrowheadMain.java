package eu.arrowhead.core;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.security.SecurityUtils;
import eu.arrowhead.core.authorization.AuthorizationApi;
import eu.arrowhead.core.gatekeeper.GatekeeperApi;
import eu.arrowhead.core.gatekeeper.GatekeeperResource;
import eu.arrowhead.core.gateway.GatewayApi;
import eu.arrowhead.core.orchestrator.CommonApi;
import eu.arrowhead.core.orchestrator.OrchestratorResource;
import eu.arrowhead.core.orchestrator.StoreApi;
import eu.arrowhead.core.serviceregistry.ServiceRegistryApi;
import eu.arrowhead.core.serviceregistry.ServiceRegistryResource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import javax.net.ssl.SSLContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class ArrowheadMain {

  public static boolean DEBUG_MODE;
  public static String serverAddress;

  public static final boolean USE_GATEWAY = Boolean.valueOf(getProp().getProperty("use_gateway", "false"));

  private static PrivateKey privateKey;
  private static HttpServer gkServer;
  private static HttpServer gkSecureServer;
  private static HttpServer orchServer;
  private static HttpServer orchSecureServer;
  private static HttpServer srServer;
  private static HttpServer srSecureServer;
  private static Properties prop;

  private static final String GK_BASE_URI = getProp().getProperty("gatekeeper_base_uri", "http://127.0.0.1:8446/");
  private static final String GK_BASE_URI_SECURED = getProp().getProperty("gatekeeper_base_uri_secured", "https://127.0.0.1:8447/");
  private static final String ORCH_BASE_URI = getProp().getProperty("orch_base_uri", "http://127.0.0.1:8440/");
  private static final String ORCH_BASE_URI_SECURED = getProp().getProperty("orch_base_uri_secured", "https://127.0.0.1:8441/");
  private static final String SR_BASE_URI = getProp().getProperty("sr_base_uri", "http://127.0.0.1:8442/");
  private static final String SR_BASE_URI_SECURED = getProp().getProperty("sr_base_uri_secured", "https://127.0.0.1:8443/");
  private static final Logger log = Logger.getLogger(ArrowheadMain.class.getName());
  // The mandatory property fields
  private static final List<String> basicPropertyNames = Arrays
      .asList("db_user", "db_password", "db_address", "gateway_socket_timeout", "gatekeeper_base_uri", "min_port", "max_port", "orch_base_uri",
              "sr_base_uri");
  private static final List<String> securePropertyNames = Arrays
      .asList("auth_keystore", "auth_keystorepass", "gatekeeper_base_uri_secured", "master_arrowhead_cert", "gateway_keystore",
              "gateway_keystore_pass", "orch_base_uri_secured", "sr_base_uri_secured", "cloud_keystore", "cloud_keystore_pass", "cloud_keypass");

  private enum CoreSystemType {GATEKEEPER, ORCHESTRATOR, SERVICE_REGISTRY}

  //TODO ADD SR pingprovidertask
  public static void main(String[] args) throws IOException {
    PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
    System.out.println("Working directory: " + System.getProperty("user.dir"));
    Utility.isUrlValid(GK_BASE_URI, false);
    Utility.isUrlValid(GK_BASE_URI_SECURED, true);
    Utility.isUrlValid(ORCH_BASE_URI, false);
    Utility.isUrlValid(ORCH_BASE_URI_SECURED, true);
    Utility.isUrlValid(SR_BASE_URI, false);
    Utility.isUrlValid(SR_BASE_URI_SECURED, true);

    boolean daemon = false;
    boolean serverModeSet = false;
    for (int i = 0; i < args.length; ++i) {
      switch (args[i]) {
        case "-daemon":
          daemon = true;
          System.out.println("Starting servers as daemon!");
          break;
        case "-d":
          DEBUG_MODE = true;
          System.out.println("Starting servers in debug mode!");
          break;
        case "-m":
          serverModeSet = true;
          ++i;
          switch (args[i]) {
            case "insecure":
              Utility.checkProperties(getProp().stringPropertyNames(), basicPropertyNames, securePropertyNames, false);
              gkServer = startServer(GK_BASE_URI, CoreSystemType.GATEKEEPER);
              orchServer = startServer(ORCH_BASE_URI, CoreSystemType.ORCHESTRATOR);
              srServer = startServer(SR_BASE_URI, CoreSystemType.SERVICE_REGISTRY);
              break;
            case "secure":
              Utility.checkProperties(getProp().stringPropertyNames(), basicPropertyNames, securePropertyNames, true);
              gkSecureServer = startSecureServer(GK_BASE_URI_SECURED, CoreSystemType.GATEKEEPER);
              orchSecureServer = startSecureServer(ORCH_BASE_URI_SECURED, CoreSystemType.ORCHESTRATOR);
              srSecureServer = startSecureServer(SR_BASE_URI_SECURED, CoreSystemType.SERVICE_REGISTRY);
              break;
            case "both":
              Utility.checkProperties(getProp().stringPropertyNames(), basicPropertyNames, securePropertyNames, true);
              gkServer = startServer(GK_BASE_URI, CoreSystemType.GATEKEEPER);
              orchServer = startServer(ORCH_BASE_URI, CoreSystemType.ORCHESTRATOR);
              srServer = startServer(SR_BASE_URI, CoreSystemType.SERVICE_REGISTRY);
              gkSecureServer = startSecureServer(GK_BASE_URI_SECURED, CoreSystemType.GATEKEEPER);
              orchSecureServer = startSecureServer(ORCH_BASE_URI_SECURED, CoreSystemType.ORCHESTRATOR);
              srSecureServer = startSecureServer(SR_BASE_URI_SECURED, CoreSystemType.SERVICE_REGISTRY);
              break;
            default:
              log.fatal("Unknown server mode: " + args[i]);
              throw new ServiceConfigurationError("Unknown server mode: " + args[i]);
          }
      }
    }
    if (!serverModeSet) {
      gkServer = startServer(GK_BASE_URI, CoreSystemType.GATEKEEPER);
      orchServer = startServer(ORCH_BASE_URI, CoreSystemType.ORCHESTRATOR);
      srServer = startServer(SR_BASE_URI, CoreSystemType.SERVICE_REGISTRY);
    }

    if (daemon) {
      System.out.println("In daemon mode, process will terminate for TERM signal...");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Received TERM signal, shutting down...");
        shutdown();
      }));
    } else {
      System.out.println("Type \"stop\" to shutdown Core System Server(s)...");
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String input = "";
      while (!input.equals("stop")) {
        input = br.readLine();
      }
      br.close();
      shutdown();
    }
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
    if (serverAddress == null) {
      serverAddress = uri.getHost();
    }

    try {
      final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);
      server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
      server.start();

      log.info("Started " + type.toString() + " server at: " + url);
      System.out.println("Started " + type.toString() + " server at: " + url);
      return server;
    } catch (ProcessingException e) {
      throw new ServiceConfigurationError(
          type.toString() + " server failed to start. Make sure you gave a valid address in the app.properties file! (Assignable to this JVM and "
              + "not in use already)", e);
    }
  }

  private static HttpServer startSecureServer(final String url, final CoreSystemType type) throws IOException {
    final ResourceConfig config = new ResourceConfig();

    String cloudKeystorePath = getProp().getProperty("cloud_keystore");
    String cloudKeystorePass = getProp().getProperty("cloud_keystore_pass");
    String cloudKeyPass = getProp().getProperty("cloud_keypass");
    String masterArrowheadCertPath = getProp().getProperty("master_arrowhead_cert");
    config.property("server_common_name", getServerCN(cloudKeystorePath, cloudKeystorePass));
    config.packages("eu.arrowhead.common");

    SSLContext serverContext = null;
    switch (type) {
      case GATEKEEPER:
        config.registerClasses(GatekeeperApi.class, GatekeeperResource.class);
        serverContext = SecurityUtils.createMasterSSLContext(cloudKeystorePath, cloudKeystorePass, cloudKeyPass, masterArrowheadCertPath);
        Utility.setSSLContext(serverContext);
        break;

      case ORCHESTRATOR:
        config.registerClasses(AuthorizationApi.class, GatewayApi.class, CommonApi.class, OrchestratorResource.class, StoreApi.class);
        break;

      case SERVICE_REGISTRY:
        config.registerClasses(ServiceRegistryApi.class, ServiceRegistryResource.class);
        break;
    }
    if (type != CoreSystemType.GATEKEEPER) {
      SSLContextConfigurator serverConfig = new SSLContextConfigurator();
      serverConfig.setTrustStoreFile(cloudKeystorePath);
      serverConfig.setTrustStorePass(cloudKeystorePass);
      if (!serverConfig.validateConfiguration(false)) {
        log.fatal("Server SSL Context is not valid, check the certificate files or app.properties!");
        throw new AuthenticationException("Server SSL Context is not valid, check the certificate files or app.properties!");
      }
      serverContext = serverConfig.createSSLContext();
    }

    URI uri = UriBuilder.fromUri(url).build();
    if (serverAddress == null) {
      serverAddress = uri.getHost();
    }

    try {
      final HttpServer server = GrizzlyHttpServerFactory
          .createHttpServer(uri, config, true, new SSLEngineConfigurator(serverContext).setClientMode(false).setNeedClientAuth(true));
      server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
      server.start();
      log.info("Started secure " + type.toString() + " server at: " + url);
      System.out.println("Started secure " + type.toString() + " server at: " + url);
      return server;
    } catch (ProcessingException e) {
      throw new ServiceConfigurationError(
          type.toString() + " server failed to start. Make sure you gave a valid address in the app.properties file! (Assignable to this JVM and "
              + "not in use already)", e);
    }
  }

  private static void shutdown() {
    if (gkServer != null) {
      gkServer.shutdownNow();
    }
    if (orchServer != null) {
      orchServer.shutdownNow();
    }
    if (srServer != null) {
      srServer.shutdownNow();
    }
    if (gkSecureServer != null) {
      gkSecureServer.shutdownNow();
    }
    if (orchSecureServer != null) {
      orchSecureServer.shutdownNow();
    }
    if (srSecureServer != null) {
      srSecureServer.shutdownNow();
    }
    log.info("Core System Server(s) stopped");
    System.out.println("Core System Server(s) stopped");
  }

  private static String getServerCN(String certPath, String certPass) {
    KeyStore keyStore = SecurityUtils.loadKeyStore(certPath, certPass);
    X509Certificate serverCert = SecurityUtils.getFirstCertFromKeyStore(keyStore);
    String serverCN = SecurityUtils.getCertCNFromSubject(serverCert.getSubjectDN().getName());
    if (!SecurityUtils.isTrustStoreCNArrowheadValid(serverCN)) {
      log.fatal("Server CN is not compliant with the Arrowhead cert structure.");
      throw new AuthenticationException(
          "Server CN ( " + serverCN + ") is not compliant with the Arrowhead cert structure, since it does not have 4 parts, or does not "
              + "end with arrowhead.eu.");
    }

    log.info("Certificate of the secure server: " + serverCN);
    return serverCN;
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
