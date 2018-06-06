package eu.arrowhead.core;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.misc.SecurityUtils;
import eu.arrowhead.common.misc.TypeSafeProperties;
import eu.arrowhead.core.authorization.AuthorizationApi;
import eu.arrowhead.core.gatekeeper.GatekeeperApi;
import eu.arrowhead.core.gatekeeper.GatekeeperResource;
import eu.arrowhead.core.gateway.GatewayApi;
import eu.arrowhead.core.orchestrator.CommonApi;
import eu.arrowhead.core.orchestrator.OrchestratorResource;
import eu.arrowhead.core.orchestrator.StoreApi;
import eu.arrowhead.core.serviceregistry.PingProvidersTask;
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
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.Timer;
import java.util.TimerTask;
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

  public static final boolean USE_GATEWAY = Boolean.valueOf(getProp().getProperty("use_gateway", "false"));

  private static HttpServer gkServer;
  private static HttpServer orchServer;
  private static HttpServer srServer;
  private static TypeSafeProperties prop;
  private static Timer timer;

  private static final String SERVER_ADDRESS = getProp().getProperty("server_address", "0.0.0.0");
  private static final Logger log = Logger.getLogger(ArrowheadMain.class.getName());

  // Types of core systems enum
  private enum CoreSystemType {
    GATEKEEPER, ORCHESTRATOR, SERVICE_REGISTRY
  }

  public static void main(String[] args) throws IOException {
    System.out.println("Working directory: " + System.getProperty("user.dir"));
    PropertyConfigurator.configure("config" + File.separator + "log4j.properties");

    boolean daemon = false;
    List<String> alwaysMandatoryProperties = Arrays.asList("gateway_address", "db_user", "db_password", "db_address");
    for (String arg : args) {
      switch (arg) {
        case "-daemon":
          daemon = true;
          System.out.println("Starting servers as daemon!");
          break;
        case "-d":
          DEBUG_MODE = true;
          System.out.println("Starting servers in debug mode!");
          break;
        case "-tls":
          List<String> allMandatoryProperties = new ArrayList<>(alwaysMandatoryProperties);
          allMandatoryProperties.addAll(Arrays.asList("cloud_keystore", "cloud_keystore_pass", "cloud_keypass", "auth_keystore", "auth_keystorepass",
                                                      "master_arrowhead_cert", "gateway_keystore", "gateway_keystore_pass"));
          Utility.checkProperties(getProp().stringPropertyNames(), allMandatoryProperties);

          final String GK_BASE_URI = Utility.getUri(SERVER_ADDRESS, 8447, "", true, true);
          final String ORCH_BASE_URI = Utility.getUri(SERVER_ADDRESS, 8441, "", true, true);
          final String SR_BASE_URI = Utility.getUri(SERVER_ADDRESS, 8443, "", true, true);

          gkServer = startSecureServer(GK_BASE_URI, CoreSystemType.GATEKEEPER);
          orchServer = startSecureServer(ORCH_BASE_URI, CoreSystemType.ORCHESTRATOR);
          srServer = startSecureServer(SR_BASE_URI, CoreSystemType.SERVICE_REGISTRY);
      }
    }
    if (srServer == null) {
      Utility.checkProperties(getProp().stringPropertyNames(), alwaysMandatoryProperties);

      final String GK_BASE_URI = Utility.getUri(SERVER_ADDRESS, 8446, "", false, true);
      final String ORCH_BASE_URI = Utility.getUri(SERVER_ADDRESS, 8440, "", false, true);
      final String SR_BASE_URI = Utility.getUri(SERVER_ADDRESS, 8442, "", false, true);

      gkServer = startServer(GK_BASE_URI, CoreSystemType.GATEKEEPER);
      orchServer = startServer(ORCH_BASE_URI, CoreSystemType.ORCHESTRATOR);
      srServer = startServer(SR_BASE_URI, CoreSystemType.SERVICE_REGISTRY);
    }

    if (Boolean.valueOf(getProp().getProperty("ping_scheduled", "false"))) {
      TimerTask pingTask = new PingProvidersTask();
      timer = new Timer();
      int interval = getProp().getIntProperty("ping_interval", 10);
      timer.schedule(pingTask, 60000L, (interval * 60L * 1000L));
    }

    if (daemon) {
      System.out.println("In daemon mode, process will terminate for TERM signal...");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Received TERM signal, shutting down...");
        if (timer != null) {
          timer.cancel();
        }
        shutdown();
      }));
    } else {
      System.out.println("Type \"stop\" to shutdown Core System Servers...");
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String input = "";
      while (!input.equals("stop")) {
        input = br.readLine();
      }
      br.close();
      if (timer != null) {
        timer.cancel();
      }
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
      serverConfig.setKeyStoreFile(cloudKeystorePath);
      serverConfig.setKeyStorePass(cloudKeystorePass);
      serverConfig.setTrustStoreFile(cloudKeystorePath);
      serverConfig.setTrustStorePass(cloudKeystorePass);
      if (!serverConfig.validateConfiguration(false)) {
        log.fatal("Server SSL Context is not valid, check the certificate files or app.properties!");
        throw new AuthException("Server SSL Context is not valid, check the certificate files or app.properties!");
      }
      serverContext = serverConfig.createSSLContext();
    }

    URI uri = UriBuilder.fromUri(url).build();
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

    log.info("Core System Servers stopped");
    System.out.println("Core System Servers stopped");
    System.exit(0);
  }

  private static String getServerCN(String certPath, String certPass) {
    KeyStore keyStore = SecurityUtils.loadKeyStore(certPath, certPass);
    X509Certificate serverCert = SecurityUtils.getFirstCertFromKeyStore(keyStore);
    String serverCN = SecurityUtils.getCertCNFromSubject(serverCert.getSubjectDN().getName());
    if (!SecurityUtils.isTrustStoreCNArrowheadValid(serverCN)) {
      log.fatal("Server CN is not compliant with the Arrowhead cert structure.");
      throw new AuthException(
          "Server CN ( " + serverCN + ") is not compliant with the Arrowhead cert structure, since it does not have 4 parts, or does not "
              + "end with arrowhead.eu.");
    }

    log.info("Certificate of the secure server: " + serverCN);
    return serverCN;
  }

  public static synchronized TypeSafeProperties getProp() {
    try {
      if (prop == null) {
        prop = new TypeSafeProperties();
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
