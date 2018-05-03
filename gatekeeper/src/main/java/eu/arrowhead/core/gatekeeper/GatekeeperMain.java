/*
 * Copyright (c) 2018 AITIA International Inc.
 *
 * This work is part of the Productive 4.0 innovation project, which receives grants from the
 * European Commissions H2020 research and innovation programme, ECSEL Joint Undertaking
 * (project no. 737459), the free state of Saxony, the German Federal Ministry of Education and
 * national funding authorities from involved countries.
 */

package eu.arrowhead.core.gatekeeper;

import eu.arrowhead.common.ArrowheadMain;
import eu.arrowhead.common.DatabaseManager;
import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.ExceptionType;
import eu.arrowhead.common.misc.CoreSystemService;
import eu.arrowhead.common.misc.SecurityUtils;
import eu.arrowhead.common.misc.TypeSafeProperties;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
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

  static boolean IS_SECURE;
  static boolean USE_GATEWAY;
  static String ORCHESTRATOR_URI;
  static String SERVICE_REGISTRY_URI;
  static String AUTH_CONTROL_URI;
  static String[] GATEWAY_CONSUMER_URI;
  static String[] GATEWAY_PROVIDER_URI;
  static SSLContext outboundClientContext;
  static SSLContext outboundServerContext;
  static final int TIMEOUT;

  private static boolean FIRST_SR_QUERY = true;
  private static String INBOUND_BASE_URI;
  private static String OUTBOUND_BASE_URI;
  private static String BASE64_PUBLIC_KEY;
  private static HttpServer inboundServer;
  private static HttpServer outboundServer;

  private static final TypeSafeProperties props;
  private static final Logger log = Logger.getLogger(GatekeeperMain.class.getName());

  static {
    PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
    props = Utility.getProp("app.properties");
    USE_GATEWAY = props.getBooleanProperty("use_gateway", false);
    TIMEOUT = props.getIntProperty("timeout", 30000);
  }

  public static void main(String[] args) throws IOException {
    System.out.println("Working directory: " + System.getProperty("user.dir"));

    String address = props.getProperty("address", "0.0.0.0");
    int internalInsecurePort = props.getIntProperty("internal_insecure_port", 8446);
    int internalSecurePort = props.getIntProperty("internal_secure_port", 8447);
    int externalInsecurePort = props.getIntProperty("external_insecure_port", 8448);
    int externalSecurePort = props.getIntProperty("external_secure_port", 8449);

    String srAddress = props.getProperty("sr_address", "0.0.0.0");
    int srInsecurePort = props.getIntProperty("sr_insecure_port", 8442);
    int srSecurePort = props.getIntProperty("sr_secure_port", 8443);

    String orchAddress = props.getProperty("orch_address", "0.0.0.0");
    int orchInsecurePort = props.getIntProperty("orch_insecure_port", 8440);
    int orchSecurePort = props.getIntProperty("orch_secure_port", 8441);

    boolean daemon = false;
    List<String> alwaysMandatoryProperties = Arrays.asList("db_user", "db_password", "db_address");
    for (String arg : args) {
      switch (arg) {
        case "-daemon":
          daemon = true;
          System.out.println("Starting server as daemon!");
          break;
        case "-d":
          System.setProperty("debug_mode", "true");
          System.out.println("Starting server in debug mode!");
          break;
        case "-tls":
          List<String> allMandatoryProperties = new ArrayList<>(alwaysMandatoryProperties);
          allMandatoryProperties.addAll(Arrays.asList("gatekeeper_keystore", "gatekeeper_keystore_pass", "gatekeeper_keypass", "cloud_keystore",
                                                      "cloud_keystore_pass", "cloud_keypass", "master_arrowhead_cert"));
          Utility.checkProperties(props.stringPropertyNames(), allMandatoryProperties);
          INBOUND_BASE_URI = Utility.getUri(address, internalSecurePort, null, true, true);
          OUTBOUND_BASE_URI = Utility.getUri(address, externalSecurePort, null, true, true);
          SERVICE_REGISTRY_URI = Utility.getUri(srAddress, srSecurePort, "serviceregistry", true, true);
          ORCHESTRATOR_URI = Utility.getUri(orchAddress, orchSecurePort, "orchestrator/orchestration", true, true);
          inboundServer = startSecureServer(INBOUND_BASE_URI, true);
          outboundServer = startSecureServer(OUTBOUND_BASE_URI, false);
          useSRService(true);
          IS_SECURE = true;
          break;
      }
    }
    if (inboundServer == null) {
      Utility.checkProperties(props.stringPropertyNames(), alwaysMandatoryProperties);
      INBOUND_BASE_URI = Utility.getUri(address, internalInsecurePort, null, false, true);
      OUTBOUND_BASE_URI = Utility.getUri(address, externalInsecurePort, null, false, true);
      SERVICE_REGISTRY_URI = Utility.getUri(srAddress, srInsecurePort, "serviceregistry", false, true);
      ORCHESTRATOR_URI = Utility.getUri(orchAddress, orchInsecurePort, "orchestrator/orchestration", false, true);
      inboundServer = startServer(INBOUND_BASE_URI, true);
      outboundServer = startServer(OUTBOUND_BASE_URI, false);
      useSRService(true);
    }
    Utility.setServiceRegistryUri(SERVICE_REGISTRY_URI);
    getCoreSystemServiceUris();

    DatabaseManager.init();
    if (daemon) {
      System.out.println("In daemon mode, process will terminate for TERM signal...");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Received TERM signal, shutting down...");
        shutdown();
      }));
    } else {
      System.out.println("Type \"stop\" to shutdown Gatekeeper Server...");
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String input = "";
      while (!input.equals("stop")) {
        input = br.readLine();
      }
      br.close();
      shutdown();
    }
  }

  private static HttpServer startServer(final String url, final boolean inbound) {
    final ResourceConfig config = new ResourceConfig();
    if (inbound) {
      config.registerClasses(GatekeeperApi.class, GatekeeperInboundResource.class);
    } else {
      config.registerClasses(GatekeeperOutboundResource.class);
    }
    config.packages("eu.arrowhead.common", "eu.arrowhead.core.gatekeeper.filter");

    URI uri = UriBuilder.fromUri(url).build();
    try {
      final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config, false);
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
    } catch (IOException | ProcessingException e) {
      throw new ServiceConfigurationError(
          "Make sure you gave a valid address in the app.properties file! (Assignable to this JVM and not in use already)", e);
    }
  }

  private static HttpServer startSecureServer(final String url, final boolean inbound) {
    final ResourceConfig config = new ResourceConfig();
    if (inbound) {
      config.registerClasses(GatekeeperInboundResource.class);
    } else {
      config.registerClasses(GatekeeperApi.class, GatekeeperOutboundResource.class);
    }
    config.packages("eu.arrowhead.common", "eu.arrowhead.core.gatekeeper.filter");

    String gatekeeperKeystorePath = props.getProperty("gatekeeper_keystore");
    String gatekeeperKeystorePass = props.getProperty("gatekeeper_keystore_pass");
    String gatekeeperKeyPass = props.getProperty("gatekeeper_keypass");
    String cloudKeystorePath = props.getProperty("cloud_keystore");
    String cloudKeystorePass = props.getProperty("cloud_keystore_pass");
    String cloudKeyPass = props.getProperty("cloud_keypass");
    String masterArrowheadCertPath = props.getProperty("master_arrowhead_cert");

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
        throw new AuthException("Internal client SSL Context is not valid, check the certificate files or app.properties!");
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
        throw new AuthException("External server SSL Context is not valid, check the certificate files or app.properties!");
      }
      serverContext = serverConfig.createSSLContext();
      outboundServerContext = serverContext;
      config.property("server_common_name", getServerCN(gatekeeperKeystorePath, gatekeeperKeystorePass, false));

      outboundClientContext = SecurityUtils.createMasterSSLContext(cloudKeystorePath, cloudKeystorePass, cloudKeyPass, masterArrowheadCertPath);
    }

    URI uri = UriBuilder.fromUri(url).build();
    try {
      final HttpServer server = GrizzlyHttpServerFactory
          .createHttpServer(uri, config, true, new SSLEngineConfigurator(serverContext).setClientMode(false).setNeedClientAuth(true), false);
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
    } catch (IOException | ProcessingException e) {
      throw new ServiceConfigurationError(
          "Make sure you gave a valid address in the app.properties file! (Assignable to this JVM and not in use already)", e);
    }
  }

  private static void useSRService(boolean registering) {
    URI uri = UriBuilder.fromUri(OUTBOUND_BASE_URI).build();
    boolean isSecure = uri.getScheme().equals("https");
    ArrowheadSystem gkSystem = new ArrowheadSystem("gatekeeper", uri.getHost(), uri.getPort(), BASE64_PUBLIC_KEY);
    ArrowheadService gsdService = new ArrowheadService(Utility.createSD(CoreSystemService.GSD_SERVICE.getServiceDef(), isSecure),
                                                       Collections.singletonList("JSON"), null);
    ArrowheadService icnService = new ArrowheadService(Utility.createSD(CoreSystemService.ICN_SERVICE.getServiceDef(), isSecure),
                                                       Collections.singletonList("JSON"), null);
    if (isSecure) {
      gsdService.setServiceMetadata(ArrowheadMain.secureServerMetadata);
      icnService.setServiceMetadata(ArrowheadMain.secureServerMetadata);
    }

    //Preparing the payload
    ServiceRegistryEntry gsdEntry = new ServiceRegistryEntry(gsdService, gkSystem, "gatekeeper/init_gsd");
    ServiceRegistryEntry icnEntry = new ServiceRegistryEntry(icnService, gkSystem, "gatekeeper/init_icn");

    if (registering) {
      try {
        Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("register").build().toString(), "POST", gsdEntry);
      } catch (ArrowheadException e) {
        if (e.getExceptionType() == ExceptionType.DUPLICATE_ENTRY) {
          Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("remove").build().toString(), "PUT", gsdEntry);
          Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("register").build().toString(), "POST", gsdEntry);
        } else {
          throw new ArrowheadException("GSD service registration failed.", e);
        }
      }
      try {
        Utility.sendRequest(UriBuilder.fromUri(SERVICE_REGISTRY_URI).path("register").build().toString(), "POST", icnEntry);
      } catch (ArrowheadException e) {
        if (e.getExceptionType() == ExceptionType.DUPLICATE_ENTRY) {
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
    AUTH_CONTROL_URI = Utility.getServiceInfo(CoreSystemService.AUTH_CONTROL_SERVICE.getServiceDef())[0];
    if (USE_GATEWAY) {
      GATEWAY_CONSUMER_URI = Utility.getServiceInfo(CoreSystemService.GW_CONSUMER_SERVICE.getServiceDef());
      GATEWAY_PROVIDER_URI = Utility.getServiceInfo(CoreSystemService.GW_PROVIDER_SERVICE.getServiceDef());
    }
    if (!FIRST_SR_QUERY) {
      ORCHESTRATOR_URI = Utility.getServiceInfo(CoreSystemService.ORCH_SERVICE.getServiceDef())[0];
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
      throw new AuthException(
          "Server CN ( " + serverCN + ") is not compliant with the Arrowhead cert structure, since it does not have 4 parts, or does not "
              + "end with arrowhead.eu.");
    } else if (!inbound && !SecurityUtils.isKeyStoreCNArrowheadValid(serverCN)) {
      log.fatal("Server CN is not compliant with the Arrowhead cert structure");
      throw new AuthException(
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
    if (outboundServer != null) {
      log.info("Stopping server at: " + OUTBOUND_BASE_URI);
      outboundServer.shutdown();
      useSRService(false);
    }
    DatabaseManager.closeSessionFactory();
    System.out.println("Gatekeeper Server stopped");
    System.exit(0);
  }

}
