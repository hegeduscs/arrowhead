package eu.arrowhead.core.orchestrator;

import eu.arrowhead.common.Utility;
import eu.arrowhead.common.database.ArrowheadService;
import eu.arrowhead.common.database.ArrowheadSystem;
import eu.arrowhead.common.database.ServiceRegistryEntry;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthenticationException;
import eu.arrowhead.common.security.SecurityUtils;
import eu.arrowhead.core.orchestrator.api.CommonApi;
import eu.arrowhead.core.orchestrator.api.StoreApi;
import eu.arrowhead.core.orchestrator.support.OldOrchResource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Properties;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;


public class OrchestratorMain {

  public static boolean DEBUG_MODE;

  private static HttpServer server;
  private static HttpServer secureServer;
  private static Properties prop;

  private static final String BASE_URI = getProp().getProperty("base_uri", "http://0.0.0.0:8440/orchestrator/");
  private static final String BASE_URI_SECURED = getProp().getProperty("base_uri_secured", "https://0.0.0.0:8441/orchestrator/");
  private static final Logger log = Logger.getLogger(OrchestratorMain.class.getName());

  public static void main(String[] args) throws IOException {
    PropertyConfigurator.configure("config" + File.separator + "log4j.properties");
    System.out.println("Working directory: " + System.getProperty("user.dir"));
    Utility.isUrlValid(BASE_URI, false);
    Utility.isUrlValid(BASE_URI_SECURED, true);

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
              server = startServer();
              useSRService(false, true);
              break;
            case "secure":
              secureServer = startSecureServer();
              useSRService(true, true);
              break;
            case "both":
              server = startServer();
              secureServer = startSecureServer();
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
      server = startServer();
      useSRService(false, true);
    }

    if (daemon) {
      System.out.println("In daemon mode, process will terminate for TERM signal...");
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        System.out.println("Received TERM signal, shutting down...");
        shutdown();
      }));
    } else {
      System.out.println("Type \"stop\" to shutdown Orchestrator Server(s)...");
      BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
      String input = "";
      while (!input.equals("stop")) {
        input = br.readLine();
      }
      br.close();
      shutdown();
    }
  }

  private static HttpServer startServer() throws IOException {
    log.info("Starting server at: " + BASE_URI);
    System.out.println("Starting insecure server at: " + BASE_URI);

    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(OrchestratorResource.class, CommonApi.class, StoreApi.class, OldOrchResource.class);
    config.packages("eu.arrowhead.common", "eu.arrowhead.core.orchestrator.filter");

    URI uri = UriBuilder.fromUri(BASE_URI).build();
    final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(uri, config);
    server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
    server.start();
    return server;
  }

  private static HttpServer startSecureServer() throws IOException {
    log.info("Starting server at: " + BASE_URI_SECURED);
    System.out.println("Starting secure server at: " + BASE_URI_SECURED);

    final ResourceConfig config = new ResourceConfig();
    config.registerClasses(OrchestratorResource.class, CommonApi.class, StoreApi.class, OldOrchResource.class);
    config.packages("eu.arrowhead.common", "eu.arrowhead.core.orchestrator.filter");

    String keystorePath = getProp().getProperty("keystore");
    String keystorePass = getProp().getProperty("keystorepass");
    String keyPass = getProp().getProperty("keypass");
    String truststorePath = getProp().getProperty("truststore");
    String truststorePass = getProp().getProperty("truststorepass");

    SSLContextConfigurator sslCon = new SSLContextConfigurator();
    sslCon.setKeyStoreFile(keystorePath);
    sslCon.setKeyStorePass(keystorePass);
    sslCon.setKeyPass(keyPass);
    sslCon.setTrustStoreFile(truststorePath);
    sslCon.setTrustStorePass(truststorePass);
    if (!sslCon.validateConfiguration(true)) {
      log.fatal("SSL Context is not valid, check the certificate files or app.properties!");
      throw new AuthenticationException("SSL Context is not valid, check the certificate files or app.properties!",
                                        Status.UNAUTHORIZED.getStatusCode(), AuthenticationException.class.getName(), BASE_URI_SECURED);
    }

    SSLContext sslContext = sslCon.createSSLContext();
    Utility.setSSLContext(sslContext);

    KeyStore keyStore = SecurityUtils.loadKeyStore(keystorePath, keystorePass);
    X509Certificate serverCert = SecurityUtils.getFirstCertFromKeyStore(keyStore);
    String serverCN = SecurityUtils.getCertCNFromSubject(serverCert.getSubjectDN().getName());
    if (!SecurityUtils.isKeyStoreCNArrowheadValid(serverCN)) {
      log.fatal("Server CN is not compliant with the Arrowhead cert structure, since it does not have 6 parts.");
      throw new AuthenticationException(
          "Server CN ( " + serverCN + ") is not compliant with the Arrowhead cert structure, since it does not have 6 parts.",
          Status.UNAUTHORIZED.getStatusCode(), AuthenticationException.class.getName(), BASE_URI_SECURED);
    }
    log.info("Certificate of the secure server: " + serverCN);
    config.property("server_common_name", serverCN);

    URI uri = UriBuilder.fromUri(BASE_URI_SECURED).build();
    final HttpServer server = GrizzlyHttpServerFactory
        .createHttpServer(uri, config, true, new SSLEngineConfigurator(sslCon).setClientMode(false).setNeedClientAuth(true));
    server.getServerConfiguration().setAllowPayloadForUndefinedHttpMethods(true);
    server.start();
    return server;
  }

  private static void useSRService(boolean isSecure, boolean registering) {
    URI uri;
    ArrowheadService orchService;
    if (isSecure) {
      uri = UriBuilder.fromUri(BASE_URI_SECURED).build();
      orchService = new ArrowheadService("SecureOrchestrationService", Collections.singletonList("JSON"), null);
    } else {
      uri = UriBuilder.fromUri(BASE_URI).build();
      orchService = new ArrowheadService("InsecureOrchestrationService", Collections.singletonList("JSON"), null);
    }

    //Preparing the payload
    ArrowheadSystem orchSystem = new ArrowheadSystem("orchestrator", uri.getHost(), uri.getPort(), null);
    ServiceRegistryEntry orchEntry = new ServiceRegistryEntry(orchService, orchSystem, "orchestrator/orchestration");

    String baseUri = Utility.getServiceRegistryUri();
    if (registering) {
      try {
        Utility.sendRequest(UriBuilder.fromUri(baseUri).path("register").build().toString(), "POST", orchEntry);
      } catch (ArrowheadException e) {
        if (e.getExceptionType().contains("DuplicateEntryException")) {
          Utility.sendRequest(UriBuilder.fromUri(baseUri).path("remove").build().toString(), "PUT", orchEntry);
          Utility.sendRequest(UriBuilder.fromUri(baseUri).path("register").build().toString(), "POST", orchEntry);
        } else {
          System.out.println("Orchestration service registration failed.");
        }
      }
    } else {
      Utility.sendRequest(UriBuilder.fromUri(baseUri).path("remove").build().toString(), "PUT", orchEntry);
    }
  }

  private static void shutdown() {
    if (server != null) {
      log.info("Stopping server at: " + BASE_URI);
      server.shutdownNow();
      useSRService(false, false);
    }
    if (secureServer != null) {
      log.info("Stopping server at: " + BASE_URI_SECURED);
      secureServer.shutdownNow();
      useSRService(true, false);
    }
    System.out.println("Orchestrator Server(s) stopped");
  }

  private static synchronized Properties getProp() {
    try {
      if (prop == null) {
        prop = new Properties();
        File file = new File("config" + File.separator + "app.properties");
        FileInputStream inputStream = new FileInputStream(file);
        prop.load(inputStream);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return prop;
  }

}
