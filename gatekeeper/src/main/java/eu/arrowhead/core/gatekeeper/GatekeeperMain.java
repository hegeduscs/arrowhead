/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.gatekeeper;

import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.security.SecurityUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
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

public class GatekeeperMain {

  public static boolean DEBUG_MODE;
  public static SSLContext outboundClientContext;
  public static SSLContext outboundServerContext;

  static String AUTH_CONTROL_URI;
  static String[] GATEWAY_CONSUMER_URI;
  static String[] GATEWAY_PROVIDER_URI;
  static String ORCHESTRATOR_URI = getProp().getProperty("orch_base_uri");
  static String SERVICE_REGISTRY_URI = getProp().getProperty("sr_base_uri");
  static boolean USE_GATEWAY = Boolean.valueOf(getProp().getProperty("use_gateway", "false"));
  static final int timeout = Integer.valueOf(getProp().getProperty("timeout", "30000"));

  private static String BASE64_PUBLIC_KEY;
  private static boolean FIRST_SR_QUERY = true;
  private static HttpServer inboundServer;
  private static HttpServer inboundSecureServer;
  private static HttpServer outboundServer;
  private static HttpServer outboundSecureServer;
  private static Properties prop;

  private static final String INBOUND_BASE_URI = getProp().getProperty("internal_base_uri", "http://127.0.0.1:8446/");
  private static final String INBOUND_BASE_URI_SECURED = getProp().getProperty("internal_base_uri_secured", "https://127.0.0.1:8447/");
  private static final String OUTBOUND_BASE_URI = getProp().getProperty("external_base_uri", "http://127.0.0.1:8448/");
  private static final String OUTBOUND_BASE_URI_SECURED = getProp().getProperty("external_base_uri_secured", "https://127.0.0.1:8449/");
  private static final Logger log = Logger.getLogger(GatekeeperMain.class.getName());
  private static final List<String> basicPropertyNames = Arrays
      .asList("internal_base_uri", "external_base_uri", "sr_base_uri", "orch_base_uri", "db_user", "db_password", "db_address", "timeout",
              "use_gateway");
  private static final List<String> securePropertyNames = Arrays
      .asList("internal_base_uri_secured", "external_base_uri_secured", "gatekeeper_keystore", "gatekeeper_keystore_pass", "gatekeeper_keypass",
              "cloud_keystore", "cloud_keystore_pass", "cloud_keypass", "master_arrowhead_cert");

  public static void main(String[] args) throws IOException {
    PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
    System.out.println("Working directory: " + System.getProperty("user.dir"));
    Utility.isUrlValid(INBOUND_BASE_URI, false);
    Utility.isUrlValid(INBOUND_BASE_URI_SECURED, true);
    Utility.isUrlValid(OUTBOUND_BASE_URI, false);
    Utility.isUrlValid(OUTBOUND_BASE_URI_SECURED, true);
    if (SERVICE_REGISTRY_URI.startsWith("https")) {
      Utility.isUrlValid(SERVICE_REGISTRY_URI, true);
    } else {
      Utility.isUrlValid(SERVICE_REGISTRY_URI, false);
    }
    if (!SERVICE_REGISTRY_URI.contains("serviceregistry")) {
      SERVICE_REGISTRY_URI = UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("serviceregistry").build().toString();
    }
    ORCHESTRATOR_URI = Utility.stripEndSlash(ORCHESTRATOR_URI);
    if (!ORCHESTRATOR_URI.endsWith("orchestrator/orchestration")) {
      ORCHESTRATOR_URI = UriBuilder.fromUri(ORCHESTRATOR_URI).path("orchestrator").path("orchestration").build().toString();
    }

    boolean daemon = false;
    boolean serverModeSet = false;
    for (int i = 0; i < args.length; ++i) {
      switch (args[i]) {
        case "-daemon":
          daemon = true;
          System.out.println("Starting server as daemon!");
          break;
        case "-d":
          DEBUG_MODE = true;
          System.out.println("Starting server in debug mode!");
          break;
        case "-m":
          serverModeSet = true;
          ++i;
          switch (args[i]) {
            case "insecure":
              Utility.checkProperties(getProp().stringPropertyNames(), basicPropertyNames, securePropertyNames, false);
              inboundServer = startServer(INBOUND_BASE_URI, true);
              outboundServer = startServer(OUTBOUND_BASE_URI, false);
              useSRService(false, true);
              break;
            case "secure":
              Utility.checkProperties(getProp().stringPropertyNames(), basicPropertyNames, securePropertyNames, true);
              inboundSecureServer = startSecureServer(INBOUND_BASE_URI_SECURED, true);
              outboundSecureServer = startSecureServer(OUTBOUND_BASE_URI_SECURED, false);
              useSRService(true, true);
              break;
            case "both":
              Utility.checkProperties(getProp().stringPropertyNames(), basicPropertyNames, securePropertyNames, true);
              inboundServer = startServer(INBOUND_BASE_URI, true);
              outboundServer = startServer(OUTBOUND_BASE_URI, false);
              inboundSecureServer = startSecureServer(INBOUND_BASE_URI_SECURED, true);
              outboundSecureServer = startSecureServer(OUTBOUND_BASE_URI_SECURED, false);
              useSRService(false, true);
              useSRService(true, true);
              break;
            default:
              log.fatal("Unknown server mode: " + args[i]);
              throw new AssertionError("Unknown server mode: " + args[i]);
          }
      }
    }
    if (!serverModeSet) {
      inboundServer = startServer(INBOUND_BASE_URI, true);
      outboundServer = startServer(OUTBOUND_BASE_URI, false);
      useSRService(false, true);
    }
    Utility.setServiceRegistryUri(SERVICE_REGISTRY_URI);
    getCoreSystemServiceUris();

    //This is here to initialize the database connection before the REST resources are initiated
    DatabaseManager dm = DatabaseManager.getInstance();
    if (daemon) {
      System.out.println("In daemon mode, process will terminate for TERM signal...");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Received TERM signal, shutting down...");
        shutdown();
      }));
    } else {
      System.out.println("Type \"stop\" to shutdown Gatekeeper Server(s)...");
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String input = "";
      while (!input.equals("stop")) {
        input = br.readLine();
      }
      br.close();
      shutdown();
    }
  }

  private static HttpServer startServer(final String url, final boolean inbound) throws IOException {
    final ResourceConfig config = new ResourceConfig();
    if (inbound) {
      config.registerClasses(GatekeeperApi.class, GatekeeperInboundResource.class);
    } else {
      config.registerClasses(GatekeeperOutboundResource.class);
    }
    config.packages("eu.arrowhead.common", "eu.arrowhead.core.gatekeeper.filter");

    URI uri = UriBuilder.fromUri(url).build();
    try {
      final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);
      server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
      server.start();
      if (inbound) {
        log.info("Started inbound server at: " + url);
        System.out.println("Started insecure inbound server at: " + url);
      } else {
        log.info("Started outbound server at: " + url);
        System.out.println("Started insecure outbound server at: " + url);
      }
      return server;
    } catch (ProcessingException e) {
      throw new ServiceConfigurationError(
          "Make sure you gave a valid address in the app.properties file! (Assignable to this JVM and not in use already)", e);
    }
  }

  private static HttpServer startSecureServer(final String url, final boolean inbound) throws IOException {
    final ResourceConfig config = new ResourceConfig();
    if (inbound) {
      config.registerClasses(GatekeeperInboundResource.class);
    } else {
      config.registerClasses(GatekeeperApi.class, GatekeeperOutboundResource.class);
    }
    config.packages("eu.arrowhead.common", "eu.arrowhead.core.gatekeeper.filter");

    String gatekeeperKeystorePath = getProp().getProperty("gatekeeper_keystore");
    String gatekeeperKeystorePass = getProp().getProperty("gatekeeper_keystore_pass");
    String gatekeeperKeyPass = getProp().getProperty("gatekeeper_keypass");
    String cloudKeystorePath = getProp().getProperty("cloud_keystore");
    String cloudKeystorePass = getProp().getProperty("cloud_keystore_pass");
    String cloudKeyPass = getProp().getProperty("cloud_keypass");
    String masterArrowheadCertPath = getProp().getProperty("master_arrowhead_cert");

    SSLContext serverContext;
    if (inbound) {
      serverContext = SecurityUtils.createMasterSSLContext(cloudKeystorePath, cloudKeystorePass, cloudKeyPass, masterArrowheadCertPath);
      config.property("server_common_name", getServerCN(cloudKeystorePath, cloudKeystorePass, true));

      SSLContextConfigurator clientConfig = new SSLContextConfigurator();
      clientConfig.setKeyStoreFile(gatekeeperKeystorePath);
      clientConfig.setKeyStorePass(gatekeeperKeystorePass);
      clientConfig.setKeyPass(gatekeeperKeyPass);
      clientConfig.setTrustStoreFile(cloudKeystorePath);
      clientConfig.setTrustStorePass(cloudKeystorePass);
      if (!clientConfig.validateConfiguration(true)) {
        log.fatal("Internal client SSL Context is not valid, check the certificate files or app.properties!");
        throw new AuthenticationException("Internal client SSL Context is not valid, check the certificate files or app.properties!");
      }
      SSLContext clientContext = clientConfig.createSSLContext();
      Utility.setSSLContext(clientContext);
    } else {
      SSLContextConfigurator serverConfig = new SSLContextConfigurator();
      serverConfig.setKeyStoreFile(gatekeeperKeystorePath);
      serverConfig.setKeyStorePass(gatekeeperKeystorePass);
      serverConfig.setKeyPass(gatekeeperKeyPass);
      serverConfig.setTrustStoreFile(cloudKeystorePath);
      serverConfig.setTrustStorePass(cloudKeystorePass);
      if (!serverConfig.validateConfiguration(true)) {
        log.fatal("External server SSL Context is not valid, check the certificate files or app.properties!");
        throw new AuthenticationException("External server SSL Context is not valid, check the certificate files or app.properties!");
      }
      serverContext = serverConfig.createSSLContext();
      outboundServerContext = serverContext;
      config.property("server_common_name", getServerCN(gatekeeperKeystorePath, gatekeeperKeystorePass, false));

      outboundClientContext = SecurityUtils.createMasterSSLContext(cloudKeystorePath, cloudKeystorePass, cloudKeyPass, masterArrowheadCertPath);
    }

    URI uri = UriBuilder.fromUri(url).build();
    try {
      final HttpServer server = GrizzlyHttpServerFactory
          .createHttpServer(uri, config, true, new SSLEngineConfigurator(serverContext).setClientMode(false).setNeedClientAuth(true));
      server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
      server.start();
      if (inbound) {
        log.info("Started inbound server at: " + url);
        System.out.println("Started secure inbound server at: " + url);
      } else {
        log.info("Started outbound server at: " + url);
        System.out.println("Started secure outbound server at: " + url);
      }
      return server;
    } catch (ProcessingException e) {
      throw new ServiceConfigurationError(
          "Make sure you gave a valid address in the app.properties file! (Assignable to this JVM and not in use already)", e);
    }
  }

  private static void useSRService(boolean isSecure, boolean registering) {
    URI uri = isSecure ? UriBuilder.fromUri(OUTBOUND_BASE_URI_SECURED).build() : UriBuilder.fromUri(OUTBOUND_BASE_URI).build();
    ArrowheadSystem gkSystem = new ArrowheadSystem("gatekeeper", uri.getHost(), uri.getPort(), BASE64_PUBLIC_KEY);
    ArrowheadService gsdService = new ArrowheadService(Utility.createSD(Utility.GSD_SERVICE, isSecure), Collections.singletonList("JSON"), null);
    ArrowheadService icnService = new ArrowheadService(Utility.createSD(Utility.ICN_SERVICE, isSecure), Collections.singletonList("JSON"), null);
    if (isSecure) {
      gsdService.setServiceMetadata(Utility.secureServerMetadata);
      icnService.setServiceMetadata(Utility.secureServerMetadata);
    }

    //Preparing the payload
    ServiceRegistryEntry gsdEntry = new ServiceRegistryEntry(gsdService, gkSystem, "gatekeeper/init_gsd");
    ServiceRegistryEntry icnEntry = new ServiceRegistryEntry(icnService, gkSystem, "gatekeeper/init_icn");

    if (registering) {
      try {
        Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("register").build().toString(), "POST", gsdEntry);
      } catch (ArrowheadException e) {
        if (e.getExceptionType().contains("DuplicateEntryException")) {
          Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("remove").build().toString(), "PUT", gsdEntry);
          Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("register").build().toString(), "POST", gsdEntry);
        } else {
          throw new ArrowheadException("GSD service registration failed.", e);
        }
      }
      try {
        Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("register").build().toString(), "POST", icnEntry);
      } catch (ArrowheadException e) {
        if (e.getExceptionType().contains("DuplicateEntryException")) {
          Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("remove").build().toString(), "PUT", icnEntry);
          Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("register").build().toString(), "POST", icnEntry);
        } else {
          throw new ArrowheadException("ICN service registration failed.", e);
        }
      }
    } else {
      Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("remove").build().toString(), "PUT", gsdEntry);
      Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("remove").build().toString(), "PUT", icnEntry);
      System.out.println("Gatekeeper services deregistered.");
    }
  }

  public static void getCoreSystemServiceUris() {
    AUTH_CONTROL_URI = Utility.getServiceInfo(Utility.AUTH_CONTROL_SERVICE)[0];
    if (USE_GATEWAY) {
      GATEWAY_CONSUMER_URI = Utility.getServiceInfo(Utility.GW_CONSUMER_SERVICE);
      GATEWAY_PROVIDER_URI = Utility.getServiceInfo(Utility.GW_PROVIDER_SERVICE);
    }
    if (!FIRST_SR_QUERY) {
      ORCHESTRATOR_URI = Utility.getServiceInfo(Utility.ORCH_SERVICE)[0];
    }
    System.out.println("Core system URLs acquired.");
    FIRST_SR_QUERY = false;
  }

  private static String getServerCN(String certPath, String certPass, boolean inbound) {
    KeyStore keyStore = SecurityUtils.loadKeyStore(certPath, certPass);
    X509Certificate serverCert = SecurityUtils.getFirstCertFromKeyStore(keyStore);
    BASE64_PUBLIC_KEY = Base64.getEncoder().encodeToString(serverCert.getPublicKey().getEncoded());
    String serverCN = SecurityUtils.getCertCNFromSubject(serverCert.getSubjectDN().getName());
    if (inbound && !SecurityUtils.isTrustStoreCNArrowheadValid(serverCN)) {
      log.fatal("Server CN is not compliant with the Arrowhead cert structure.");
      throw new AuthenticationException(
          "Server CN ( " + serverCN + ") is not compliant with the Arrowhead cert structure, since it does not have 4 parts, or does not "
              + "end with arrowhead.eu.");
    } else if (!inbound && !SecurityUtils.isKeyStoreCNArrowheadValid(serverCN)) {
      log.fatal("Server CN is not compliant with the Arrowhead cert structure");
      throw new AuthenticationException(
          "Server CN ( " + serverCN + ") is not compliant with the Arrowhead cert structure, since it does not have 5 parts, or does not "
              + "end with arrowhead.eu.");
    }

    log.info("Certificate of the secure server: " + serverCN);
    return serverCN;
  }

  private static void shutdown() {
    if (inboundServer != null) {
      log.info("Stopping server at: " + INBOUND_BASE_URI);
      inboundServer.shutdownNow();
    }
    if (inboundSecureServer != null) {
      log.info("Stopping server at: " + INBOUND_BASE_URI_SECURED);
      inboundSecureServer.shutdownNow();
    }
    if (outboundServer != null) {
      log.info("Stopping server at: " + OUTBOUND_BASE_URI);
      outboundServer.shutdown();
      useSRService(false, false);
    }
    if (outboundSecureServer != null) {
      log.info("Stopping server at: " + OUTBOUND_BASE_URI_SECURED);
      outboundSecureServer.shutdown();
      useSRService(true, false);
    }
    System.out.println("Gatekeeper Servers stopped");
  }

  private static synchronized Properties getProp() {
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
