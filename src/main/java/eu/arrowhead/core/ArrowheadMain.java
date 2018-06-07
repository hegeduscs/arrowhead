package eu.arrowhead.core;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.misc.SecurityUtils;
import eu.arrowhead.common.misc.TypeSafeProperties;
import eu.arrowhead.core.eventhandler.DeleteExpiredFiltersTask;
import eu.arrowhead.core.gatekeeper.GatekeeperApi;
import eu.arrowhead.core.gatekeeper.GatekeeperResource;
import eu.arrowhead.core.serviceregistry.PingProvidersTask;
import eu.arrowhead.core.serviceregistry.RemoveExpiredServicesTask;
import java.io.BufferedReader;
import java.io.File;
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

  public static TypeSafeProperties props = Utility.getProp("app.properties");

  public static final boolean USE_GATEWAY = props.getBooleanProperty("use_gateway", false);
  public static final int PUBLISH_EVENTS_DELAY = props.getIntProperty("event_publishing_delay", 60);

  private static HttpServer server;
  private static HttpServer gkServer;

  private static final String SERVER_ADDRESS = props.getProperty("server_address", "0.0.0.0");
  private static final Logger log = Logger.getLogger(ArrowheadMain.class.getName());

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
          System.setProperty("debug_mode", "true");
          System.out.println("Starting servers in debug mode!");
          break;
        case "-tls":
          List<String> allMandatoryProperties = new ArrayList<>(alwaysMandatoryProperties);
          allMandatoryProperties.addAll(Arrays.asList("cloud_keystore", "cloud_keystore_pass", "cloud_keypass", "auth_keystore", "auth_keystorepass",
                                                      "master_arrowhead_cert", "gateway_keystore", "gateway_keystore_pass"));
          Utility.checkProperties(props.stringPropertyNames(), allMandatoryProperties);

          final String SERVER_BASE_URI = Utility.getUri(SERVER_ADDRESS, 8441, "", true, true);
          final String GK_BASE_URI = Utility.getUri(SERVER_ADDRESS, 8447, "", true, true);
          server = startSecureServer(SERVER_BASE_URI, false);
          gkServer = startSecureServer(GK_BASE_URI, true);
      }
    }
    if (server == null) {
      Utility.checkProperties(props.stringPropertyNames(), alwaysMandatoryProperties);
      final String SERVER_BASE_URI = Utility.getUri(SERVER_ADDRESS, 8440, "", false, true);
      server = startServer(SERVER_BASE_URI);
    }

    if (props.getBooleanProperty("ping_scheduled", false)) {
      TimerTask pingTask = new PingProvidersTask();
      Timer pingTimer = new Timer();
      int interval = props.getIntProperty("ping_interval", 60);
      pingTimer.schedule(pingTask, 60000L, (interval * 60L * 1000L));
    }
    if (props.getBooleanProperty("ttl_scheduled", false)) {
      TimerTask ttlTask = new RemoveExpiredServicesTask();
      Timer ttlTimer = new Timer();
      int interval = props.getIntProperty("ttl_interval", 10);
      ttlTimer.schedule(ttlTask, 60000L, (interval * 60L * 1000L));
    }
    if (props.getBooleanProperty("remove_old_filters", false)) {
      TimerTask filterTask = new DeleteExpiredFiltersTask();
      Timer filterTimer = new Timer();
      int interval = props.getIntProperty("filter_check_interval", 60);
      filterTimer.schedule(filterTask, 60000L, (interval * 60L * 1000L));
    }

    if (daemon) {
      System.out.println("In daemon mode, process will terminate for TERM signal...");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Received TERM signal, shutting down...");
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
      shutdown();
    }
  }

  private static HttpServer startServer(final String url) {
    final ResourceConfig config = new ResourceConfig().packages("eu.arrowhead");
    URI uri = UriBuilder.fromUri(url).build();
    try {
      final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config, false);
      server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
      server.start();

      log.info("Started arrowhead server at: " + url);
      System.out.println("Started arrowhead server at: " + url);
      return server;
    } catch (IOException | ProcessingException e) {
      throw new ServiceConfigurationError(
          "Arrowhead server failed to start at: " + url + ". Make sure you gave a valid address in the app.properties file! (Assignable to this JVM "
              + "and not in use already)", e);
    }
  }

  private static HttpServer startSecureServer(final String url, final boolean isGatekeeper) {
    final ResourceConfig config = new ResourceConfig();

    String cloudKeystorePath = props.getProperty("cloud_keystore");
    String cloudKeystorePass = props.getProperty("cloud_keystore_pass");
    String cloudKeyPass = props.getProperty("cloud_keypass");
    String masterArrowheadCertPath = props.getProperty("master_arrowhead_cert");
    config.property("server_common_name", getServerCN(cloudKeystorePath, cloudKeystorePass));
    config.packages("eu.arrowhead.common");

    SSLContext serverContext;
    if (isGatekeeper) {
      config.registerClasses(GatekeeperApi.class, GatekeeperResource.class);
      serverContext = SecurityUtils.createMasterSSLContext(cloudKeystorePath, cloudKeystorePass, cloudKeyPass, masterArrowheadCertPath);
      Utility.setSSLContext(serverContext);
    } else {
      config.packages("eu.arrowhead.core.authorization", "eu.arrowhead.core.eventhandler", "eu.arrowhead.core.orchestrator",
                      "eu.arrowhead.core.serviceregistry");

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
          .createHttpServer(uri, config, true, new SSLEngineConfigurator(serverContext).setClientMode(false).setNeedClientAuth(true), false);
      server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
      server.start();
      log.info("Started secure arrowhead server at: " + url);
      System.out.println("Started secure arrowhead server at: " + url);
      return server;
    } catch (IOException | ProcessingException e) {
      throw new ServiceConfigurationError(
          "Arrowhead server failed to start at " + url + ". Make sure you gave a valid address in the app.properties file! (Assignable to this "
              + "JVM and not in use already)", e);
    }
  }

  private static void shutdown() {
    DatabaseManager.closeSessionFactory();
    if (server != null) {
      server.shutdownNow();
    }
    if (gkServer != null) {
      gkServer.shutdownNow();
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

}
